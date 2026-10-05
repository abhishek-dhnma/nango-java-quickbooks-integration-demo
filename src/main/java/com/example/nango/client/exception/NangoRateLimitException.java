package com.example.nango.client.exception;

/**
 * Thrown when Nango responds with HTTP 429 Too Many Requests.
 */
public class NangoRateLimitException extends NangoApiException {

    private final String retryAfterSeconds;

    public NangoRateLimitException(String message, String retryAfterSeconds) {
        super(message, 429, "RATE_LIMIT_EXCEEDED");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
