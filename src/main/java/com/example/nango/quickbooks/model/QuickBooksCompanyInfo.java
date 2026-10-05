package com.example.nango.quickbooks.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuickBooksCompanyInfo(
    @JsonProperty("CompanyName") String companyName,
    @JsonProperty("LegalName") String legalName,
    @JsonProperty("CompanyAddr") Address companyAddr,
    @JsonProperty("Country") String country,
    @JsonProperty("FiscalYearStartMonth") String fiscalYearStartMonth,
    @JsonProperty("OfferingSku") String offeringSku
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
        @JsonProperty("Line1") String line1,
        @JsonProperty("City") String city,
        @JsonProperty("CountrySubDivisionCode") String state,
        @JsonProperty("PostalCode") String postalCode
    ) {}
}
