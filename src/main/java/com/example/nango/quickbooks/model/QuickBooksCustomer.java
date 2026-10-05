package com.example.nango.quickbooks.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuickBooksCustomer(
    @JsonProperty("Id") String id,
    @JsonProperty("DisplayName") String displayName,
    @JsonProperty("GivenName") String givenName,
    @JsonProperty("FamilyName") String familyName,
    @JsonProperty("CompanyName") String companyName,
    @JsonProperty("Active") Boolean active,
    @JsonProperty("Balance") BigDecimal balance,
    @JsonProperty("PrimaryEmailAddr") Email primaryEmailAddr,
    @JsonProperty("PrimaryPhone") Phone primaryPhone
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Email(@JsonProperty("Address") String address) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Phone(@JsonProperty("FreeFormNumber") String freeFormNumber) {}
}
