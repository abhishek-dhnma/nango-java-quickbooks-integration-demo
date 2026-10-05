package com.example.nango.quickbooks.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuickBooksQueryResponse(
    @JsonProperty("QueryResponse") QueryData queryResponse
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QueryData(
        @JsonProperty("Invoice") List<QuickBooksInvoice> invoices,
        @JsonProperty("Customer") List<QuickBooksCustomer> customers,
        @JsonProperty("startPosition") Integer startPosition,
        @JsonProperty("maxResults") Integer maxResults,
        @JsonProperty("totalCount") Integer totalCount
    ) {
        public List<QuickBooksInvoice> safeInvoices() {
            return invoices != null ? invoices : Collections.emptyList();
        }

        public List<QuickBooksCustomer> safeCustomers() {
            return customers != null ? customers : Collections.emptyList();
        }
    }
}
