package goxlarge.auth.client.ports.cache.preemptive;

import goxlarge.auth.client.ports.mechanism.AuthorizationMechanism;
import goxlarge.auth.client.ports.utils.AuthorizationGrant;
import net.jodah.failsafe.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PreemptiveAuthorizationCacheTest {
    private static final Logger log = LoggerFactory.getLogger(PreemptiveAuthorizationCacheTest.class);

    private static final long refreshDelay = 10;
    private static final long retryDelay = 5;
    private AtomicInteger tid;
    private PreemptiveAuthorizationCache<Integer> cache;
    private AuthorizationMechanism<Integer> authorizationMechanism;
    @BeforeEach
    public void setup() {
        RetryPolicy retryPolicy = new RetryPolicy().withDelay(retryDelay, 10, ChronoUnit.MILLIS);
        tid = new AtomicInteger(0);
        authorizationMechanism = previousAuthorization -> {
                int next = tid.incrementAndGet();
                // using multi-threads get value from cache the previous and next doesn't have predictable value
                // simulator the token fetch exception
                if(previousAuthorization != null && previousAuthorization + 1 != next) {
                    throw new RuntimeException("random issues from Authorization server!");
                }
                return CompletableFuture.completedFuture(new AuthorizationGrant<>(next, Instant.now().plus(refreshDelay*2, ChronoUnit.MILLIS)));
        };

        cache = new PreemptiveAuthorizationCache<>(
                authorizationMechanism,
                e -> refreshDelay,
                null,
                retryPolicy
        );
    }

    @Test
    public void test() throws InterruptedException {
        ExecutorService e = Executors.newFixedThreadPool(5);
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicReference<Integer> lastAuthz = new AtomicReference<>(-1000);
        for(int id = 0; id < 1; id++) {
            e.submit(() -> {
                while (running.get()) {
                    try {
                        //Thread.sleep((long) (Math.random() * 3));
                        CompletableFuture<Integer> intToken = cache.cachedAuthorization();
                        Integer integer = intToken.get(refreshDelay * 5, TimeUnit.SECONDS);
                        lastAuthz.set(integer);
                       // log.info("Current thread: " + Thread.currentThread() + " " + " got authz - " + lastAuthz.get());
                    } catch (Exception x) {
                        log.error("Current thread: " + Thread.currentThread() + " " + " failed to get authz: " + x);
                        failed.set(true);
                        running.set(false);
                        throw new RuntimeException(x);
                    }
                }
            });
        }
        e.submit(() -> {
            for(int count = 0; count < 500 && running.get(); count++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException x) {
                    log.error("Failed to sleep", x);
                }
                cache.invalidateCache();
            }
            log.info("Stop the test!");
            running.set(false);
        });
        e.shutdown();
        e.awaitTermination(1, TimeUnit.SECONDS);
        if(failed.get()) {
            throw new RuntimeException("Failed to get a authz at one point!");
        }
        if(Math.abs(tid.get() - lastAuthz.get()) > 1) {
            throw new RuntimeException("Authz isn't caching correctly. Most recently fetched (" + tid.get() + ") is too far off from cached(" + lastAuthz.get() + ").");
        }
    }

}
