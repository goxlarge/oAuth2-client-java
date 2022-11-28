package goxlarge.auth.client.ports.utils;

import java.time.Instant;

public class AuthorizationFailure extends RuntimeException {
    public AuthorizationFailure(String message, Throwable cause) {
        super(message, cause);
    }
}

