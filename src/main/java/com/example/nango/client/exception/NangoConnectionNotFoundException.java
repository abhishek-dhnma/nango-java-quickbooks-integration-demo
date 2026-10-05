package com.example.nango.client.exception;

/**
 * Thrown when a requested connection does not exist in Nango (HTTP 404).
 */
public class NangoConnectionNotFoundException extends NangoApiException {

    public NangoConnectionNotFoundException(String message) {
        super(message, 404, "NOT_FOUND");
    }
}
