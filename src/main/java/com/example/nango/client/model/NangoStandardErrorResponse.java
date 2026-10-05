package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Standard Nango API error payload (400, 401, 404, 500).
 */
public record NangoStandardErrorResponse(
    @JsonProperty("error") ErrorDetails error
) {
    public record ErrorDetails(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("errors") List<Map<String, Object>> errors
    ) {}
}
