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
