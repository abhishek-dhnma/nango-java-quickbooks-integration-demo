package com.example.nango;

import com.example.nango.webhook.NangoWebhookVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class NangoWebhookVerificationTest {

    private static final String SIGNING_KEY = "test-secret-signing-key-123";
    private NangoWebhookVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new NangoWebhookVerifier(SIGNING_KEY);
    }

    @Test
    @DisplayName("Valid HMAC-SHA256 signature returns true")
    void testValidSignature() throws Exception {
        String payload = "{\"type\":\"auth\",\"operation\":\"creation\",\"connectionId\":\"conn-123\"}";
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        String expectedSignature = calculateHmac(payloadBytes, SIGNING_KEY);

        boolean isValid = verifier.isValidSignature(payloadBytes, expectedSignature);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Tampered payload returns false")
    void testTamperedPayload() throws Exception {
        String originalPayload = "{\"type\":\"auth\",\"operation\":\"creation\",\"connectionId\":\"conn-123\"}";
        String tamperedPayload = "{\"type\":\"auth\",\"operation\":\"creation\",\"connectionId\":\"conn-999\"}";

        String signature = calculateHmac(originalPayload.getBytes(StandardCharsets.UTF_8), SIGNING_KEY);

        boolean isValid = verifier.isValidSignature(tamperedPayload.getBytes(StandardCharsets.UTF_8), signature);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Invalid signature string returns false")
    void testInvalidSignature() {
        String payload = "{\"type\":\"sync\",\"success\":true}";
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        boolean isValid = verifier.isValidSignature(payloadBytes, "bad_hex_signature");
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Empty or null parameters return false gracefully")
    void testNullParameters() {
        assertThat(verifier.isValidSignature(null, "some_sig")).isFalse();
        assertThat(verifier.isValidSignature("{}".getBytes(StandardCharsets.UTF_8), null)).isFalse();
        assertThat(verifier.isValidSignature("{}".getBytes(StandardCharsets.UTF_8), "")).isFalse();
    }

    private String calculateHmac(byte[] data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
