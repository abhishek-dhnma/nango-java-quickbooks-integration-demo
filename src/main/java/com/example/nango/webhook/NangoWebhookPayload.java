package com.example.nango.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Models incoming webhooks dispatched by Nango (auth, sync, action).
 */
public record NangoWebhookPayload(
    @JsonProperty("type") String type,
    @JsonProperty("operation") String operation,
    @JsonProperty("connectionId") String connectionId,
    @JsonProperty("providerConfigKey") String providerConfigKey,
    @JsonProperty("provider") String provider,
    @JsonProperty("environment") String environment,
    @JsonProperty("success") Boolean success,
    @JsonProperty("syncName") String syncName,
    @JsonProperty("model") String model,
    @JsonProperty("tags") Map<String, String> tags,
    @JsonProperty("error") Map<String, Object> error,
    @JsonProperty("responseResults") Map<String, Object> responseResults
) {}
