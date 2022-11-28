package goxlarge.auth.client.ports.cache.preemptive;

import goxlarge.auth.client.ports.cache.AuthorizationCache;
import goxlarge.auth.client.ports.mechanism.AuthorizationMechanism;
import goxlarge.auth.client.ports.utils.SingleJobScheduleExecutorService;
import goxlarge.auth.client.ports.utils.Formater;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class PreemptiveAuthorizationCache<T> implements AuthorizationCache<T> {
    private static final Logger log = LoggerFactory.getLogger(PreemptiveAuthorizationCache.class);
    private static final String DUMMY_KEY = "key";
    private final AuthorizationMechanism<T> authorizationMechanism;
    private final RetryPolicy retryPolicy;
    private final PreemptiveSchedulingAlgorithm preemptiveSchedulingAlgorithm;
    private final Consumer<T> authorizationConsumer;
    private final ConcurrentHashMap<String, CompletableFuture<T>> authorizMemoizer = new ConcurrentHashMap<>(1);
    private final SingleJobScheduleExecutorService<T> singleJobScheduleExecutorService;

    private final Lock lock = new ReentrantLock();

    public PreemptiveAuthorizationCache(AuthorizationMechanism<T> authorizationMechanism,
                                        PreemptiveSchedulingAlgorithm preemptiveSchedulingAlgorithm,
                                        Consumer<T> authorizationConsumer,
                                        RetryPolicy retryPolicy) {
        this.authorizationMechanism = authorizationMechanism;
        this.preemptiveSchedulingAlgorithm = preemptiveSchedulingAlgorithm;
        this.authorizationConsumer = authorizationConsumer;
        this.retryPolicy = retryPolicy;
        this.singleJobScheduleExecutorService = new SingleJobScheduleExecutorService<T>();
    }

    @Override
    public CompletableFuture<T> cachedAuthorization() {
        return authorizMemoizer.computeIfAbsent(DUMMY_KEY,  (x_x) -> sourceAndScheduleAsync(null));
    }

    private CompletableFuture<T> sourceAndScheduleAsync(T previousAuthorization) {
        log.info("[1] submitting mechanism - AuthorizationMechanism: " + authorizationMechanism.toString());
        return authorizationMechanism
                .authorizeWithRetry(previousAuthorization, retryPolicy)
                .thenApplyAsync(authorization -> {
                    T value = null;
                    if (authorization != null) {
                        scheduleRefresh(authorization.expires(), authorization.value());
                        value = authorization.value();
                    }
                    return value;
                }).thenApplyAsync(authorization -> {
                    if(authorizationConsumer != null){
                        log.info("[3] Consume authorization ...");
                        authorizationConsumer.accept(authorization);
                    }else{
                        log.info("[3] Skip Consume authorization ...");
                    }
                    return authorization;
                }).whenComplete((authorization, exp) -> {
                    if(exp != null){
                        log.error("[4] Source, schedule or consume authorization error:", exp);
                    }else{
                        // publish event
                        log.info("[4] Success!");
                    }
                });
    }

    @Override
    public void invalidateCache() {
        log.info("invalidating cached authorization ; AuthorizationMechanism: " + authorizationMechanism.toString());
        lock.lock();
        try {
            CompletableFuture<T> existingAuthz = authorizMemoizer.remove(DUMMY_KEY);
            if (existingAuthz == null) {
                log.debug("invalidateCache was called, but there was no cached authorization" + "; AuthorizationMechanism: " + authorizationMechanism.toString());
            }
            singleJobScheduleExecutorService.cancelScheduledJob();
        } finally {
            lock.unlock();
        }
    }

    private ScheduledFuture scheduleRefresh(Instant expiration, T previousAuthorization) {
        long delay = preemptiveSchedulingAlgorithm.calculateDelay(expiration);
        log.info("[2] Scheduling next authorization, " + Formater.getHumanTime(delay) + " from now" + "; AuthorizationMechanism: " + authorizationMechanism.toString());

        ScheduledFuture scheduledFuture = singleJobScheduleExecutorService.schedule(() -> {
            sourceAndScheduleAsync(previousAuthorization)
                    .whenComplete((token, exp) -> {
                        if(exp == null){
                            log.info("[2.1] Completed scheduled authorization refresh.");
                            authorizMemoizer.put(DUMMY_KEY, CompletableFuture.completedFuture(token));
                        }else {
                            log.error("[2.2] Failed to resolve scheduled authorization." + "; AuthorizationMechanism: " + authorizationMechanism.toString(), exp);
                        }
                    });
        },delay);

        return scheduledFuture;
    }
}
