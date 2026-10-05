package com.example.nango.client.exception;

/**
 * Thrown when Nango API key authentication fails (HTTP 401).
 */
public class NangoAuthenticationException extends NangoApiException {

    public NangoAuthenticationException(String message) {
        super(message, 401, "UNAUTHORIZED");
    }
}
