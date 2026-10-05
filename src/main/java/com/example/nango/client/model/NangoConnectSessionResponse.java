package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Response payload for a Nango Connect Session (POST /connect/sessions).
 */
public record NangoConnectSessionResponse(
    @JsonProperty("data") SessionData data
) {
    public record SessionData(
        @JsonProperty("token") String token,
        @JsonProperty("connect_link") String connectLink,
        @JsonProperty("expires_at") Instant expiresAt
    ) {}
}
