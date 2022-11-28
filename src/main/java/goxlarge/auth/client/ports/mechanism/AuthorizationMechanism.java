package goxlarge.auth.client.ports.mechanism;

import goxlarge.auth.client.ports.utils.AuthorizationFailure;
import goxlarge.auth.client.ports.utils.AuthorizationGrant;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import java.util.concurrent.CompletableFuture;

/**
 * A simple interface that supplies fresh access tokens.
 */
public interface AuthorizationMechanism<T> {
    /**
     * @param previousAuthorization the previous authorization sourced before this one. Null means that this is the first authorization
     * @return a fresh authorization
     * @throws AuthorizationFailure if there are any network or HTTP failures. Cause should be wrapped.
     */
    default CompletableFuture<AuthorizationGrant<T>> authorizeWithRetry(T previousAuthorization, RetryPolicy retryPolicy){
        return Failsafe.with(retryPolicy).getStageAsync(() -> authorize(previousAuthorization));
    }

    CompletableFuture<AuthorizationGrant<T>> authorize(T previousAuthorization) throws AuthorizationFailure;
}
