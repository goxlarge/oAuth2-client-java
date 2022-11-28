package goxlarge.auth.client.ports.store;

import goxlarge.auth.client.ports.utils.AuthorizationGrant;

public interface AuthorizationStore<T> {
    void put(AuthorizationGrant<T> grant);

    AuthorizationGrant<T> get();

    void destroy();

    class StorageFailure extends RuntimeException {
        public StorageFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
