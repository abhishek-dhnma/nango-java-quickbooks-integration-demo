package com.example.nango.quickbooks.service;

import com.example.nango.client.model.NangoConnectSessionResponse;
import com.example.nango.quickbooks.model.QuickBooksCompanyInfo;
import com.example.nango.quickbooks.model.QuickBooksCustomer;
import com.example.nango.quickbooks.model.QuickBooksInvoice;

import java.util.List;

/**
 * Domain service providing multi-tenant access to QuickBooks Online data via Nango.
 */
public interface QuickBooksAccountingService {

    /**
     * Generates a hosted Nango Connect link for a tenant to authorize their QuickBooks account.
     */
    NangoConnectSessionResponse initiateAccountingConnection(String tenantId);

    /**
     * Retrieves QuickBooks company metadata via Nango Proxy.
     */
    QuickBooksCompanyInfo getCompanyInfo(String tenantId);

    /**
     * Retrieves invoices for a tenant via Nango Proxy.
     */
    List<QuickBooksInvoice> getInvoices(String tenantId, int maxResults);

    /**
     * Retrieves a single invoice by its QuickBooks ID via Nango Proxy.
     */
    QuickBooksInvoice getInvoice(String tenantId, String invoiceId);

    /**
     * Retrieves customers for a tenant via Nango Proxy.
     */
    List<QuickBooksCustomer> getCustomers(String tenantId, int maxResults);
}
