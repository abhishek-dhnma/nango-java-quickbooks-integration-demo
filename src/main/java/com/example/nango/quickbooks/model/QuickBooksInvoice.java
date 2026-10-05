package com.example.nango.quickbooks.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuickBooksInvoice(
    @JsonProperty("Id") String id,
    @JsonProperty("DocNumber") String docNumber,
    @JsonProperty("TxnDate") String txnDate,
    @JsonProperty("DueDate") String dueDate,
    @JsonProperty("TotalAmt") BigDecimal totalAmt,
    @JsonProperty("Balance") BigDecimal balance,
    @JsonProperty("CustomerRef") Reference customerRef,
    @JsonProperty("BillEmail") Email billEmail,
    @JsonProperty("Line") List<LineItem> lineItems
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Reference(
        @JsonProperty("value") String value,
        @JsonProperty("name") String name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Email(
        @JsonProperty("Address") String address
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LineItem(
        @JsonProperty("Id") String id,
        @JsonProperty("LineNum") Integer lineNum,
        @JsonProperty("Description") String description,
        @JsonProperty("Amount") BigDecimal amount,
        @JsonProperty("DetailType") String detailType
    ) {}
}
