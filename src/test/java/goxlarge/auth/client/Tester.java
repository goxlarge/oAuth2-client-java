package goxlarge.auth.client;

import goxlarge.auth.client.ports.cache.AuthorizationCache;
import goxlarge.auth.client.ports.cache.preemptive.PreemptiveAuthorizationCache;
import goxlarge.auth.client.ports.cache.preemptive.PreemptiveSchedulingAlgorithm;
import goxlarge.auth.client.ports.mechanism.AuthorizationMechanism;
import goxlarge.auth.client.ports.utils.AuthorizationGrant;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Tester {
    private static final Logger log = LoggerFactory.getLogger(Tester.class);

    public static void main(String[] args) throws Exception {
        final AuthorizationCache<String> cache = localTest(10);
        final Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String cmd = scanner.nextLine();
            switch (cmd) {
                case "a": {
                    log.info("Authorization: " + cache.cachedAuthorization().get());
                    continue;
                }
                case "i": {
                    cache.invalidateCache();
                    continue;
                }
                case "": {
                    continue;
                }
                case "q": {
                    System.exit(0);
                }
                default: {
                    log.info("enter a, i, or q");
                }
            }
        }
    }

    private static AuthorizationCache<String> localTest(float failureChance) {
        AtomicInteger id = new AtomicInteger(0);
        return new PreemptiveAuthorizationCache<>(
                (p) -> {
//                    if(Math.random() < failureChance) {
//                        throw new RuntimeException("Fake authorization retrieval error!");
//                    }
                    try {
                        log.debug("in local test, sleeping before returning authorization");
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    String authorization = "Authorization: " + id.incrementAndGet();
                    log.debug("local test returning " + authorization);
                    return CompletableFuture.completedFuture(new AuthorizationGrant<>(authorization, Instant.now().plus(30, ChronoUnit.SECONDS)));
                },
                PreemptiveSchedulingAlgorithm.variableBuffer(0.75),
                null,
                 new RetryPolicy().withBackoff(1, 30, ChronoUnit.SECONDS)
        );
    }

    private static AuthorizationCache<String> completeOverrideExample() {
        // refresh every 30 seconds
        PreemptiveSchedulingAlgorithm refreshAlgorithm = expiration -> 30 * 1000;

        // increase delay by 5s every retry

        // we like to just use random strings as authorizations
        AuthorizationMechanism<String> authorizationMechanism = (p) -> CompletableFuture.completedFuture(
                new AuthorizationGrant<>(
                UUID.randomUUID().toString(),
                Instant.now().plus(10, ChronoUnit.MINUTES))
        );

        return new PreemptiveAuthorizationCache<>(
                authorizationMechanism,
                refreshAlgorithm,
                null,
                new RetryPolicy().withBackoff(1, 30, ChronoUnit.SECONDS)
        );
    }
}
