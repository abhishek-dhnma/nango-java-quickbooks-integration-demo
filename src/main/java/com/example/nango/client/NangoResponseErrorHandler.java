package com.example.nango.client;

import com.example.nango.client.exception.*;
import com.example.nango.client.model.NangoActionErrorResponse;
import com.example.nango.client.model.NangoStandardErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * Maps Nango HTTP error status codes and JSON payloads to typed Java exceptions.
 */
@Component
public class NangoResponseErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper;

    public NangoResponseErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        byte[] body = response.getBody().readAllBytes();
        HttpStatusCode statusCode = response.getStatusCode();
        int status = statusCode.value();

        if (status == 424) {
            try {
                NangoActionErrorResponse actionError = objectMapper.readValue(body, NangoActionErrorResponse.class);
                throw new NangoActionException(
                    actionError.error() != null ? actionError.error().message() : "Action execution failed",
                    actionError.error() != null ? actionError.error().upstream() : null
                );
            } catch (NangoActionException e) {
                throw e;
            } catch (Exception e) {
                throw new NangoActionException("Action HTTP error (status 424)", null);
            }
        }

        if (status == 404) {
            throw new NangoConnectionNotFoundException("Nango connection or resource not found (status 404)");
        }

        if (status == 401) {
            throw new NangoAuthenticationException("Invalid or missing Nango API key (status 401)");
        }

        if (status == 429) {
            String retryAfter = response.getHeaders().getFirst("Retry-After");
            throw new NangoRateLimitException("Nango rate limit exceeded", retryAfter);
        }

        // Standard error parsing
        try {
            NangoStandardErrorResponse stdError = objectMapper.readValue(body, NangoStandardErrorResponse.class);
            if (stdError.error() != null) {
                throw new NangoApiException(stdError.error().message(), status, stdError.error().code());
            }
        } catch (NangoApiException e) {
            throw e;
        } catch (Exception ignored) {
        }

        throw new NangoApiException("Nango API error with status " + status, status, "UNKNOWN_ERROR");
    }
}
