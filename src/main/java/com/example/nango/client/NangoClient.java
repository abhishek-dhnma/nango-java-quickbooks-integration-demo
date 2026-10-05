package com.example.nango.client;

import com.example.nango.client.model.NangoConnectSessionResponse;
import com.example.nango.client.model.NangoConnection;
import com.example.nango.client.model.NangoSyncRecordsResponse;

import java.util.List;
import java.util.Map;

/**
 * Idiomatic Java client for Nango's official REST API.
 */
public interface NangoClient {

    /**
     * Creates a connect session for frontend user authorization (POST /connect/sessions).
     */
    NangoConnectSessionResponse createConnectSession(List<String> allowedIntegrations, com.example.nango.client.model.NangoEndUser endUser);

    /**
     * Creates a connect session with custom tags and webhook override.
     */
    NangoConnectSessionResponse createConnectSession(List<String> allowedIntegrations, com.example.nango.client.model.NangoEndUser endUser, Map<String, String> tags);

    /**
     * Retrieves connection details and credentials status (GET /connections/{connectionId}).
     */
    NangoConnection getConnection(String connectionId, String integrationKey);

    /**
     * Executes an authenticated GET request through Nango's requests proxy (GET /proxy/{path}).
     */
    <T> T proxyGet(String connectionId, String integrationKey, String path, Map<String, String> queryParams, Class<T> responseType);

    /**
     * Executes an authenticated POST request through Nango's requests proxy (POST /proxy/{path}).
     */
    <T> T proxyPost(String connectionId, String integrationKey, String path, Object requestBody, Class<T> responseType);

    /**
     * Triggers a Nango Action function synchronously (POST /action/trigger).
     */
    <T> T triggerAction(String connectionId, String integrationKey, String actionName, Object input, Class<T> responseType);

    /**
     * Fetches synced records from Nango's records cache (GET /records).
     */
    <T> NangoSyncRecordsResponse<T> getSyncRecords(String connectionId, String integrationKey, String model, String cursor, Integer limit, Class<T> recordType);
}
