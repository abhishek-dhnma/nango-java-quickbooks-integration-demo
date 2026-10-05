package com.example.nango.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory store mapping tenant IDs to Nango Connection IDs.
 */
@Repository
public class InMemoryTenantRepository implements TenantRepository {

    private final Map<String, String> tenantToConnectionMap = new ConcurrentHashMap<>();
    private final Map<String, String> connectionToTenantMap = new ConcurrentHashMap<>();

    public InMemoryTenantRepository() {
        // Pre-seed test tenants mapped to the live QuickBooks Sandbox connection
        saveTenantConnection("tenant-1", "c5a55682-b9f5-4798-a1a0-55dbdb7f7e4b");
        saveTenantConnection("tenant-default", "c5a55682-b9f5-4798-a1a0-55dbdb7f7e4b");
    }

    @Override
    public Optional<String> findConnectionIdByTenantId(String tenantId) {
        return Optional.ofNullable(tenantToConnectionMap.get(tenantId));
    }

    @Override
    public void saveTenantConnection(String tenantId, String connectionId) {
        tenantToConnectionMap.put(tenantId, connectionId);
        connectionToTenantMap.put(connectionId, tenantId);
    }

    @Override
    public Optional<String> findTenantIdByConnectionId(String connectionId) {
        return Optional.ofNullable(connectionToTenantMap.get(connectionId));
    }
}
