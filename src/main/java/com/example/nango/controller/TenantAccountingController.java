package com.example.nango.controller;

import com.example.nango.client.model.NangoConnectSessionResponse;
import com.example.nango.quickbooks.model.QuickBooksCompanyInfo;
import com.example.nango.quickbooks.model.QuickBooksCustomer;
import com.example.nango.quickbooks.model.QuickBooksInvoice;
import com.example.nango.quickbooks.service.QuickBooksAccountingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for multi-tenant accounting and invoicing via QuickBooks Online & Nango.
 */
@RestController
@RequestMapping("/api/tenants/{tenantId}/accounting")
public class TenantAccountingController {

    private final QuickBooksAccountingService accountingService;

    public TenantAccountingController(QuickBooksAccountingService accountingService) {
        this.accountingService = accountingService;
    }

    /**
     * Step 1: Generates a hosted Nango Connect session link for the tenant.
     */
    @PostMapping("/connect")
    public ResponseEntity<NangoConnectSessionResponse.SessionData> initiateConnection(@PathVariable String tenantId) {
        NangoConnectSessionResponse response = accountingService.initiateAccountingConnection(tenantId);
        return ResponseEntity.ok(response.data());
    }

    /**
     * Step 2: Retrieves live company information from QuickBooks Online via Nango Proxy.
     */
    @GetMapping("/company")
    public ResponseEntity<QuickBooksCompanyInfo> getCompanyInfo(@PathVariable String tenantId) {
        QuickBooksCompanyInfo companyInfo = accountingService.getCompanyInfo(tenantId);
        return ResponseEntity.ok(companyInfo);
    }

    /**
     * Step 3: Retrieves live invoices from QuickBooks Online via Nango Proxy.
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<QuickBooksInvoice>> getInvoices(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "10") int limit) {
        List<QuickBooksInvoice> invoices = accountingService.getInvoices(tenantId, limit);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Step 4: Retrieves a single invoice from QuickBooks Online by ID.
     */
    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<QuickBooksInvoice> getInvoice(
            @PathVariable String tenantId,
            @PathVariable String invoiceId) {
        QuickBooksInvoice invoice = accountingService.getInvoice(tenantId, invoiceId);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Step 5: Retrieves live customer records from QuickBooks Online via Nango Proxy.
     */
    @GetMapping("/customers")
    public ResponseEntity<List<QuickBooksCustomer>> getCustomers(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "10") int limit) {
        List<QuickBooksCustomer> customers = accountingService.getCustomers(tenantId, limit);
        return ResponseEntity.ok(customers);
    }
}
