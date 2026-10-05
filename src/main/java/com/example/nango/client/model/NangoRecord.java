package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Wraps a synced model record with Nango metadata.
 */
public record NangoRecord<T>(
    @JsonUnwrapped
    T data,

    @JsonProperty("_nango_metadata")
    NangoMetadata metadata
) {}
