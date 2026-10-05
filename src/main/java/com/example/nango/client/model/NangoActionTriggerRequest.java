package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload sent to POST /action/trigger.
 */
public record NangoActionTriggerRequest(
    @JsonProperty("action_name") String actionName,
    @JsonProperty("input") Object input
) {}
