package com.example.nango.client.exception;

import com.example.nango.client.model.NangoActionErrorResponse.UpstreamError;

/**
 * Thrown when an Action function execution fails with HTTP 424.
 * Contains details about the downstream integration provider response.
 */
public class NangoActionException extends NangoApiException {

    private final UpstreamError upstream;

    public NangoActionException(String message, UpstreamError upstream) {
        super(message, 424, "ACTION_HTTP_ERROR");
        this.upstream = upstream;
    }

    public UpstreamError getUpstream() {
        return upstream;
    }
}
