package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Models a Nango Connection entity as returned by GET /connection/{id}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NangoConnection(
    @JsonProperty("id") Long id,
    @JsonProperty("connection_id") String connectionId,
    @JsonProperty("provider_config_key") String providerConfigKey,
    @JsonProperty("provider") String provider,
    @JsonProperty("end_user") NangoEndUser endUser,
    @JsonProperty("connection_config") Map<String, Object> connectionConfig,
    @JsonProperty("metadata") Map<String, Object> metadata,
    @JsonProperty("tags") Map<String, String> tags,
    @JsonProperty("errors") List<Map<String, Object>> errors,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt
) {}
