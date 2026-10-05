package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Action execution error payload (HTTP 424 Failed Dependency).
 */
public record NangoActionErrorResponse(
    @JsonProperty("error") ActionErrorDetails error
) {
    public record ActionErrorDetails(
        @JsonProperty("message") String message,
        @JsonProperty("code") String code,
        @JsonProperty("payload") Object payload,
        @JsonProperty("upstream") UpstreamError upstream
    ) {}

    public record UpstreamError(
        @JsonProperty("status") Integer status,
        @JsonProperty("headers") Map<String, Object> headers,
        @JsonProperty("body") Object body
    ) {}
}
