package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Metadata attached to each synced record in Nango's cache.
 */
public record NangoMetadata(
    @JsonProperty("deleted_at") Instant deletedAt,
    @JsonProperty("last_action") String lastAction,
    @JsonProperty("first_seen_at") Instant firstSeenAt,
    @JsonProperty("last_modified_at") Instant lastModifiedAt,
    @JsonProperty("cursor") String cursor
) {}
