package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Model representing response from GET /connection.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NangoConnectionsResponse(
    @JsonProperty("connections") List<NangoConnection> connections
) {}
