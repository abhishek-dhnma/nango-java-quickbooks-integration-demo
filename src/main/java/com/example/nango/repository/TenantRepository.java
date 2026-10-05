package com.example.nango.repository;

import java.util.Optional;

/**
 * Repository interface mapping internal tenants to their Nango Connection IDs.
 */
public interface TenantRepository {

    Optional<String> findConnectionIdByTenantId(String tenantId);

    void saveTenantConnection(String tenantId, String connectionId);

    Optional<String> findTenantIdByConnectionId(String connectionId);
}
