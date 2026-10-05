package com.example.nango.client.exception;

/**
 * Base exception for Nango API communication failures.
 */
public class NangoApiException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    public NangoApiException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public NangoApiException(String message, int statusCode, String errorCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
