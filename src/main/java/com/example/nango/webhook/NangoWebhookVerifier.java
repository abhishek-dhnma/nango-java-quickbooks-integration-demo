package com.example.nango.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Validates HMAC-SHA256 signatures for incoming webhooks from Nango.
 */
@Component
public class NangoWebhookVerifier {

    private final String webhookSigningKey;

    public NangoWebhookVerifier(@Value("${nango.webhook.signing-key:}") String webhookSigningKey) {
        this.webhookSigningKey = webhookSigningKey;
    }

    public boolean isValidSignature(byte[] rawBody, String signatureHeader) {
        if (webhookSigningKey == null || webhookSigningKey.isBlank()) {
            return false;
        }
        if (signatureHeader == null || signatureHeader.isBlank() || rawBody == null) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSigningKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hashBytes = mac.doFinal(rawBody);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            String computedSignature = hexString.toString();

            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    computedSignature.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.trim().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }
}
