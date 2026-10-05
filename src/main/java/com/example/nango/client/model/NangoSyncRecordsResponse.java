package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response from GET /records.
 */
public record NangoSyncRecordsResponse<T>(
    @JsonProperty("records") List<NangoRecord<T>> records,
    @JsonProperty("next_cursor") String nextCursor
) {}
