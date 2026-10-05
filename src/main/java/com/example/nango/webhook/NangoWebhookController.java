package com.example.nango.webhook;

import com.example.nango.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * Controller receiving and validating signed webhooks from Nango.
 */
@RestController
@RequestMapping("/api/webhooks/nango")
public class NangoWebhookController {

    private static final Logger log = LoggerFactory.getLogger(NangoWebhookController.class);

    private final NangoWebhookVerifier verifier;
    private final TenantRepository tenantRepository;

    public NangoWebhookController(NangoWebhookVerifier verifier, TenantRepository tenantRepository) {
        this.verifier = verifier;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
            @RequestHeader(value = "X-Nango-Hmac-Sha256", required = false) String signature,
            HttpServletRequest request,
            @RequestBody NangoWebhookPayload payload) throws IOException {

        byte[] rawBody;
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            rawBody = wrapper.getContentAsByteArray();
        } else {
            rawBody = request.getInputStream().readAllBytes();
        }

        if (!verifier.isValidSignature(rawBody, signature)) {
            log.warn("Rejected Nango webhook due to invalid HMAC signature header: {}", signature);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Received verified Nango webhook: type={}, operation={}, connectionId={}",
                payload.type(), payload.operation(), payload.connectionId());

        if ("auth".equalsIgnoreCase(payload.type()) && "creation".equalsIgnoreCase(payload.operation())) {
            handleAuthCreation(payload);
        } else if ("sync".equalsIgnoreCase(payload.type())) {
            log.info("Nango background sync completed for model: {}, success: {}",
                    payload.model(), payload.success());
        }

        return ResponseEntity.ok().build();
    }

    private void handleAuthCreation(NangoWebhookPayload payload) {
        if (payload.tags() != null && payload.tags().containsKey("organization_id")) {
            String tenantId = payload.tags().get("organization_id");
            tenantRepository.saveTenantConnection(tenantId, payload.connectionId());
            log.info("Successfully bound tenant '{}' to Nango connection '{}'", tenantId, payload.connectionId());
        } else {
            log.warn("Auth webhook missing 'organization_id' tag for connection: {}", payload.connectionId());
        }
    }
}
