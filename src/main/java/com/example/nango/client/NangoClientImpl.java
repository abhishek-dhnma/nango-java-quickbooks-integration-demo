package com.example.nango.client;

import com.example.nango.client.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Spring RestClient-based implementation of NangoClient.
 */
@Service
public class NangoClientImpl implements NangoClient {

    private final RestClient restClient;

    public NangoClientImpl(
            @Value("${nango.base-url:https://api.nango.dev}") String baseUrl,
            @Value("${nango.secret-key}") String secretKey,
            NangoResponseErrorHandler errorHandler) {
        
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(errorHandler)
                .build();
    }

    @Override
    public NangoConnectSessionResponse createConnectSession(List<String> allowedIntegrations, NangoEndUser endUser) {
        return createConnectSession(allowedIntegrations, endUser, null);
    }

    @Override
    public NangoConnectSessionResponse createConnectSession(
            List<String> allowedIntegrations,
            NangoEndUser endUser,
            Map<String, String> tags) {
        NangoConnectSessionRequest request = new NangoConnectSessionRequest(allowedIntegrations, endUser, tags);

        return restClient.post()
                .uri("/connect/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(NangoConnectSessionResponse.class);
    }

    @Override
    public NangoConnection getConnection(String connectionId, String integrationKey) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/connection/{connectionId}")
                        .queryParam("provider_config_key", integrationKey)
                        .build(connectionId))
                .retrieve()
                .body(NangoConnection.class);
    }

    @Override
    public List<NangoConnection> listConnections(String integrationKey) {
        NangoConnectionsResponse response = restClient.get()
                .uri("/connection")
                .retrieve()
                .body(NangoConnectionsResponse.class);

        if (response != null && response.connections() != null) {
            if (integrationKey != null && !integrationKey.isBlank()) {
                return response.connections().stream()
                        .filter(c -> integrationKey.equalsIgnoreCase(c.providerConfigKey()))
                        .toList();
            }
            return response.connections();
        }
        return List.of();
    }

    @Override
    public <T> T proxyGet(String connectionId, String integrationKey, String path, Map<String, String> queryParams, Class<T> responseType) {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;

        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/proxy/" + cleanPath);
                    if (queryParams != null) {
                        queryParams.forEach(uriBuilder::queryParam);
                    }
                    return uriBuilder.build();
                })
                .header("Connection-Id", connectionId)
                .header("Provider-Config-Key", integrationKey)
                .retrieve()
                .body(responseType);
    }

    @Override
    public <T> T proxyPost(String connectionId, String integrationKey, String path, Object requestBody, Class<T> responseType) {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;

        return restClient.post()
                .uri("/proxy/" + cleanPath)
                .header("Connection-Id", connectionId)
                .header("Provider-Config-Key", integrationKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(responseType);
    }

    @Override
    public <T> T triggerAction(String connectionId, String integrationKey, String actionName, Object input, Class<T> responseType) {
        NangoActionTriggerRequest request = new NangoActionTriggerRequest(actionName, input);

        return restClient.post()
                .uri("/action/trigger")
                .header("Connection-Id", connectionId)
                .header("Provider-Config-Key", integrationKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(responseType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> NangoSyncRecordsResponse<T> getSyncRecords(
            String connectionId,
            String integrationKey,
            String model,
            String cursor,
            Integer limit,
            Class<T> recordType) {

        ParameterizedTypeReference<NangoSyncRecordsResponse<T>> typeRef = new ParameterizedTypeReference<>() {};

        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/records")
                            .queryParam("model", model);
                    if (cursor != null && !cursor.isBlank()) {
                        uriBuilder.queryParam("cursor", cursor);
                    }
                    if (limit != null && limit > 0) {
                        uriBuilder.queryParam("limit", limit);
                    }
                    return uriBuilder.build();
                })
                .header("Connection-Id", connectionId)
                .header("Provider-Config-Key", integrationKey)
                .retrieve()
                .body(typeRef);
    }
}
