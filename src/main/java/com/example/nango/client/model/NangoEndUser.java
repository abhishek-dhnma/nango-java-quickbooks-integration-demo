package com.example.nango.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record NangoEndUser(
    @JsonProperty("id") String id,
    @JsonProperty("email") String email,
    @JsonProperty("display_name") String displayName
) {
    public NangoEndUser(String id) {
        this(id, null, null);
    }
}
