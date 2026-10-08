package com.example.nango.quickbooks.service;

import com.example.nango.client.NangoClient;
import com.example.nango.client.exception.NangoConnectionNotFoundException;
import com.example.nango.client.model.NangoConnectSessionResponse;
import com.example.nango.client.model.NangoConnection;
import com.example.nango.quickbooks.model.*;
import com.example.nango.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuickBooksAccountingServiceImpl implements QuickBooksAccountingService {

    private static final Logger log = LoggerFactory.getLogger(QuickBooksAccountingServiceImpl.class);

    private final NangoClient nangoClient;
    private final TenantRepository tenantRepository;
    private final String integrationKey;

    // Cache realmIds by connectionId to avoid redundant GET /connections calls
    private final Map<String, String> realmIdCache = new ConcurrentHashMap<>();

    public QuickBooksAccountingServiceImpl(
            NangoClient nangoClient,
            TenantRepository tenantRepository,
            @Value("${nango.integration-key:quickbooks-sandbox}") String integrationKey) {
        this.nangoClient = nangoClient;
        this.tenantRepository = tenantRepository;
        this.integrationKey = integrationKey;
    }

    @Override
    public NangoConnectSessionResponse initiateAccountingConnection(String tenantId) {
        var endUser = new com.example.nango.client.model.NangoEndUser(tenantId);
        Map<String, String> tags = Map.of("organization_id", tenantId);
        return nangoClient.createConnectSession(List.of(integrationKey), endUser, tags);
    }

    @Override
    public QuickBooksCompanyInfo getCompanyInfo(String tenantId) {
        String connectionId = resolveConnectionId(tenantId);
        String realmId = resolveRealmId(connectionId);

        String path = "v3/company/" + realmId + "/companyinfo/" + realmId;
        QuickBooksCompanyInfoResponse response = nangoClient.proxyGet(
                connectionId,
                integrationKey,
                path,
                null,
                QuickBooksCompanyInfoResponse.class
        );

        return response != null ? response.companyInfo() : null;
    }

    @Override
    public List<QuickBooksInvoice> getInvoices(String tenantId, int maxResults) {
        String connectionId = resolveConnectionId(tenantId);
        String realmId = resolveRealmId(connectionId);

        int limit = maxResults > 0 ? maxResults : 10;
        String query = "select * from Invoice maxresults " + limit;
        String path = "v3/company/" + realmId + "/query";

        QuickBooksQueryResponse response = nangoClient.proxyGet(
                connectionId,
                integrationKey,
                path,
                Map.of("query", query),
                QuickBooksQueryResponse.class
        );

        return response != null && response.queryResponse() != null
                ? response.queryResponse().safeInvoices()
                : Collections.emptyList();
    }

    @Override
    public QuickBooksInvoice getInvoice(String tenantId, String invoiceId) {
        String connectionId = resolveConnectionId(tenantId);
        String realmId = resolveRealmId(connectionId);

        String path = "v3/company/" + realmId + "/invoice/" + invoiceId;
        QuickBooksInvoiceResponse response = nangoClient.proxyGet(
                connectionId,
                integrationKey,
                path,
                null,
                QuickBooksInvoiceResponse.class
        );

        return response != null ? response.invoice() : null;
    }

    @Override
    public List<QuickBooksCustomer> getCustomers(String tenantId, int maxResults) {
        String connectionId = resolveConnectionId(tenantId);
        String realmId = resolveRealmId(connectionId);

        int limit = maxResults > 0 ? maxResults : 10;
        String query = "select * from Customer maxresults " + limit;
        String path = "v3/company/" + realmId + "/query";

        QuickBooksQueryResponse response = nangoClient.proxyGet(
                connectionId,
                integrationKey,
                path,
                Map.of("query", query),
                QuickBooksQueryResponse.class
        );

        return response != null && response.queryResponse() != null
                ? response.queryResponse().safeCustomers()
                : Collections.emptyList();
    }

    private String resolveConnectionId(String tenantId) {
        // 1. Check in-memory repository (populated via webhooks or previous fallback bindings)
        var connectionIdOpt = tenantRepository.findConnectionIdByTenantId(tenantId);
        if (connectionIdOpt.isPresent()) {
            return connectionIdOpt.get();
        }

        // 2. Fallback: check if connection exists in Nango using tenantId directly as connectionId
        try {
            log.info("No webhook mapping in memory for tenant '{}'. Attempting fallback check on Nango for connectionId='{}'", tenantId, tenantId);
            NangoConnection connection = nangoClient.getConnection(tenantId, integrationKey);
            if (connection != null) {
                String resolvedId = connection.connectionId() != null 
                        ? connection.connectionId() 
                        : (connection.id() != null ? String.valueOf(connection.id()) : tenantId);
                log.info("Auto-bound tenant '{}' to Nango connection '{}' via fallback lookup", tenantId, resolvedId);
                tenantRepository.saveTenantConnection(tenantId, resolvedId);
                return resolvedId;
            }
        } catch (Exception e) {
            log.debug("Direct connection lookup for connectionId='{}' failed: {}", tenantId, e.getMessage());
        }

        // 3. Fallback: list active connections in Nango for this integration (handles local dev without webhooks)
        try {
            log.info("Querying Nango for active connections for integration '{}'...", integrationKey);
            List<NangoConnection> activeConnections = nangoClient.listConnections(integrationKey);
            if (!activeConnections.isEmpty()) {
                String resolvedId = activeConnections.stream()
                        .filter(c -> (c.tags() != null && tenantId.equals(c.tags().get("organization_id")))
                                  || (c.endUser() != null && tenantId.equals(c.endUser().id())))
                        .map(c -> c.connectionId() != null ? c.connectionId() : String.valueOf(c.id()))
                        .findFirst()
                        .orElseGet(() -> {
                            NangoConnection first = activeConnections.get(0);
                            return first.connectionId() != null ? first.connectionId() : String.valueOf(first.id());
                        });

                if (resolvedId != null) {
                    log.info("Auto-discovered active Nango connection '{}' for tenant '{}'", resolvedId, tenantId);
                    tenantRepository.saveTenantConnection(tenantId, resolvedId);
                    return resolvedId;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to auto-discover active connections from Nango: {}", e.getMessage(), e);
        }

        // 4. Fallback: Return tenantId directly
        log.info("Using tenantId '{}' directly as fallback connection ID", tenantId);
        return tenantId;
    }

    private String resolveRealmId(String connectionId) {
        return realmIdCache.computeIfAbsent(connectionId, connId -> {
            log.info("Fetching connection metadata from Nango to extract QuickBooks realmId for connectionId={}", connId);
            NangoConnection connection = nangoClient.getConnection(connId, integrationKey);
            if (connection.connectionConfig() != null && connection.connectionConfig().containsKey("realmId")) {
                return String.valueOf(connection.connectionConfig().get("realmId"));
            }
            throw new IllegalStateException("Nango connection " + connId + " does not have a 'realmId' configured.");
        });
    }
}
