package dev.synapse.core.infrastructure.exception;

import java.time.Instant;
import java.util.UUID;

public record ErrorResponse(
    String code,
    String message,
    int status,
    Instant timestamp,
    UUID correlationId,
    UUID traceId
) {
    public static ErrorResponse from(ApiException ex, UUID traceId) {
        return new ErrorResponse(
            ex.getCode(),
            ex.getMessage(),
            ex.getHttpStatus(),
            Instant.now(),
            ex.getCorrelationId(),
            traceId
        );
    }
}
