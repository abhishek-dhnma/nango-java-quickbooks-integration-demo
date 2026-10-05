package com.example.nango.quickbooks.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuickBooksInvoiceResponse(
    @JsonProperty("Invoice") QuickBooksInvoice invoice
) {}
