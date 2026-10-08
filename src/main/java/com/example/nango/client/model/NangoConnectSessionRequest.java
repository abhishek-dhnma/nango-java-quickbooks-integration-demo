package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Request payload for creating a Nango Connect Session (POST /connect/sessions).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NangoConnectSessionRequest(
    @JsonProperty("allowed_integrations") List<String> allowedIntegrations,
    @JsonProperty("end_user") NangoEndUser endUser,
    @JsonProperty("tags") Map<String, String> tags,
    @JsonProperty("webhook_url_override") String webhookUrlOverride
) {
    public NangoConnectSessionRequest(List<String> allowedIntegrations, NangoEndUser endUser) {
        this(allowedIntegrations, endUser, null, null);
    }

    public NangoConnectSessionRequest(List<String> allowedIntegrations, NangoEndUser endUser, Map<String, String> tags) {
        this(allowedIntegrations, endUser, tags, null);
    }
}
