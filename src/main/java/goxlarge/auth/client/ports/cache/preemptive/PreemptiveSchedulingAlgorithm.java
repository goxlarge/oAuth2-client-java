package goxlarge.auth.client.ports.cache.preemptive;

import java.time.Instant;

/**
 * Used to calculate preemptive authz mechanism's to prevent any expired cached authorizations.
 */
public interface PreemptiveSchedulingAlgorithm {

    long calculateDelay(Instant expiration);

    static PreemptiveSchedulingAlgorithm staticBuffer(long bufferMillis) {

        return (e) -> Math.max(0, e.toEpochMilli() - bufferMillis - Instant.now().toEpochMilli());
    }

    static PreemptiveSchedulingAlgorithm periodicRefresh(long frequencyMillis) {
        return (e) -> Math.max(0, frequencyMillis);
    }

    static PreemptiveSchedulingAlgorithm variableBuffer(double lifecyclePercentage) {
        return (e) -> (long) Math.max(0, ((e.toEpochMilli() - Instant.now().toEpochMilli())*lifecyclePercentage));
    }
}
