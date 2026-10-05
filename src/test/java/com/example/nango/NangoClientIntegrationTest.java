package com.example.nango;

import com.example.nango.client.NangoClient;
import com.example.nango.client.NangoClientImpl;
import com.example.nango.client.NangoResponseErrorHandler;
import com.example.nango.client.exception.NangoActionException;
import com.example.nango.client.exception.NangoConnectionNotFoundException;
import com.example.nango.client.exception.NangoRateLimitException;
import com.example.nango.client.model.NangoConnectSessionResponse;
import com.example.nango.quickbooks.model.QuickBooksCompanyInfoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NangoClientIntegrationTest {

    private MockWebServer mockWebServer;
    private NangoClient nangoClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        NangoResponseErrorHandler errorHandler = new NangoResponseErrorHandler(objectMapper);
        nangoClient = new NangoClientImpl(
                mockWebServer.url("/").toString(),
                "test-secret-key-12345",
                errorHandler
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("createConnectSession sends expected JSON and extracts token")
    void testCreateConnectSession() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                {
                  "data": {
                    "token": "nango_session_abc123",
                    "connect_link": "https://connect.nango.dev/?session_token=nango_session_abc123",
                    "expires_at": "2026-10-03T18:00:00.000Z"
                  }
                }
                """));

        NangoConnectSessionResponse response = nangoClient.createConnectSession(
                List.of("quickbooks-sandbox"),
                new com.example.nango.client.model.NangoEndUser("tenant-99")
        );

        assertThat(response).isNotNull();
        assertThat(response.data().token()).isEqualTo("nango_session_abc123");
        assertThat(response.data().connectLink()).contains("session_token=nango_session_abc123");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/connect/sessions");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-secret-key-12345");
        assertThat(request.getBody().readUtf8()).contains("tenant-99");
    }

    @Test
    @DisplayName("proxyGet sends Connection-Id & Provider-Config-Key and deserializes QuickBooksCompanyInfoResponse")
    void testProxyGetCompanyInfo() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                {
                  "CompanyInfo": {
                    "CompanyName": "Sandbox Company US c56a",
                    "LegalName": "Sandbox Company US c56a",
                    "Country": "US"
                  }
                }
                """));

        QuickBooksCompanyInfoResponse response = nangoClient.proxyGet(
                "conn-qb-1234",
                "quickbooks-sandbox",
                "v3/company/9341458413053036/companyinfo/9341458413053036",
                null,
                QuickBooksCompanyInfoResponse.class
        );

        assertThat(response).isNotNull();
        assertThat(response.companyInfo().companyName()).isEqualTo("Sandbox Company US c56a");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/proxy/v3/company/9341458413053036/companyinfo/9341458413053036");
        assertThat(request.getHeader("Connection-Id")).isEqualTo("conn-qb-1234");
        assertThat(request.getHeader("Provider-Config-Key")).isEqualTo("quickbooks-sandbox");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-secret-key-12345");
    }

    @Test
    @DisplayName("HTTP 404 throws NangoConnectionNotFoundException")
    void testConnectionNotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"Connection not found\"}}"));

        assertThatThrownBy(() -> nangoClient.getConnection("invalid-conn", "quickbooks-sandbox"))
                .isInstanceOf(NangoConnectionNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("HTTP 429 throws NangoRateLimitException with Retry-After header")
    void testRateLimitException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Content-Type", "application/json")
                .setHeader("Retry-After", "30")
                .setBody("{\"error\":{\"code\":\"RATE_LIMIT\",\"message\":\"Too many requests\"}}"));

        assertThatThrownBy(() -> nangoClient.proxyGet("conn-1", "quickbooks-sandbox", "v3/company/123", null, String.class))
                .isInstanceOf(NangoRateLimitException.class)
                .satisfies(ex -> {
                    NangoRateLimitException rateLimitEx = (NangoRateLimitException) ex;
                    assertThat(rateLimitEx.getRetryAfterSeconds()).isEqualTo("30");
                });
    }
}
