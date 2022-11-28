package goxlarge.auth.client.ports.utils;

import java.time.Instant;

public class AuthorizationGrant<T> {
    private final T value;
    private final Instant expires;

    public AuthorizationGrant(T value, Instant expires) {
        this.value = value;
        this.expires = expires;
    }

    public T value() {
        return value;
    }

    public Instant expires() {
        return expires;
    }
}