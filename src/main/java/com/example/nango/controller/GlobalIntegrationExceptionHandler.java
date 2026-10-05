package com.example.nango.controller;

import com.example.nango.client.exception.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Translates Nango integration errors into standardized RFC 7807 ProblemDetail responses.
 */
@RestControllerAdvice
public class GlobalIntegrationExceptionHandler {

    @ExceptionHandler(NangoConnectionNotFoundException.class)
    public ProblemDetail handleConnectionNotFound(NangoConnectionNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Accounting Connection Not Found");
        problem.setType(URI.create("https://example.com/errors/connection-not-found"));
        return problem;
    }

    @ExceptionHandler(NangoAuthenticationException.class)
    public ProblemDetail handleNangoAuthException(NangoAuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Integration platform authentication failed.");
        problem.setTitle("Integration Provider Auth Error");
        problem.setType(URI.create("https://example.com/errors/integration-auth-error"));
        return problem;
    }

    @ExceptionHandler(NangoActionException.class)
    public ProblemDetail handleActionException(NangoActionException ex) {
        int upstreamStatus = 502;
        if (ex.getUpstream() != null && ex.getUpstream().status() != null) {
            upstreamStatus = ex.getUpstream().status();
        }

        HttpStatus status = HttpStatus.resolve(upstreamStatus);
        if (status == null || status.is5xxServerError()) {
            status = HttpStatus.BAD_GATEWAY;
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle("Integration Action Execution Error");
        problem.setType(URI.create("https://example.com/errors/action-execution-error"));

        if (ex.getUpstream() != null) {
            problem.setProperty("upstreamStatus", ex.getUpstream().status());
            problem.setProperty("upstreamError", ex.getUpstream().body());
        }
        return problem;
    }

    @ExceptionHandler(NangoRateLimitException.class)
    public ResponseEntity<ProblemDetail> handleRateLimit(NangoRateLimitException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problem.setTitle("Integration Rate Limit Exceeded");
        problem.setType(URI.create("https://example.com/errors/rate-limit-exceeded"));

        HttpHeaders headers = new HttpHeaders();
        if (ex.getRetryAfterSeconds() != null && !ex.getRetryAfterSeconds().isBlank()) {
            headers.set("Retry-After", ex.getRetryAfterSeconds());
        }
        return new ResponseEntity<>(problem, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(NangoApiException.class)
    public ProblemDetail handleGeneralNangoException(NangoApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) status = HttpStatus.BAD_GATEWAY;

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle("Integration API Error");
        problem.setProperty("errorCode", ex.getErrorCode());
        return problem;
    }
}
