package goxlarge.auth.client.ports.mechanism.store_wrapper;

import goxlarge.auth.client.ports.mechanism.AuthorizationMechanism;
import goxlarge.auth.client.ports.store.AuthorizationStore;
import goxlarge.auth.client.ports.utils.AuthorizationFailure;
import goxlarge.auth.client.ports.utils.AuthorizationGrant;
import net.jodah.failsafe.RetryPolicy;

import java.util.concurrent.CompletableFuture;

public class StoreWrapperMechanism<T> implements AuthorizationMechanism<T> {

    private final AuthorizationStore<T> store;

    public StoreWrapperMechanism(AuthorizationStore<T> store , RetryPolicy retryPolicy) {
        this.store = store;
    }

    public AuthorizationStore<T> getStore() {
        return store;
    }

    @Override
    public CompletableFuture<AuthorizationGrant<T>> authorize(T previousAuthorization) throws AuthorizationFailure {
        try {
            return CompletableFuture.completedFuture(store.get());
        } catch (Exception e) {
            throw new AuthorizationFailure("Failed to get authorization from backing put", e);
        }
    }

    @Override
    public String toString() {
        return "StoreWrapperMechanism{" +
                "store=" + store +
                '}';
    }
}
