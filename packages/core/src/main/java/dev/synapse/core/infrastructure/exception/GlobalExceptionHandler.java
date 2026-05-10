package dev.synapse.core.infrastructure.exception;

import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final SystemLogService logService;

    public GlobalExceptionHandler(SystemLogService logService) {
        this.logService = logService;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        UUID traceId = UUID.randomUUID();
        UUID correlationId = ex.getCorrelationId() != null ? 
            ex.getCorrelationId() : getCorrelationIdFromMdc();
        
        logService.log(
            ex.getHttpStatus() >= 500 ? LogLevel.ERROR : LogLevel.WARN,
            LogCategory.API,
            Map.of("component", "GlobalExceptionHandler"),
            "API_ERROR",
            Map.of(
                "code", ex.getCode(),
                "message", ex.getMessage(),
                "status", ex.getHttpStatus()
            ),
            correlationId,
            traceId
        );

        ErrorResponse response = new ErrorResponse(
            ex.getCode(),
            ex.getMessage(),
            ex.getHttpStatus(),
            Instant.now(),
            correlationId,
            traceId
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        org.springframework.web.bind.MethodArgumentNotValidException ex) {
        
        UUID traceId = UUID.randomUUID();
        UUID correlationId = getCorrelationIdFromMdc();
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");

        logService.log(
            LogLevel.WARN,
            LogCategory.API,
            Map.of("component", "GlobalExceptionHandler"),
            "VALIDATION_ERROR",
            Map.of("message", message),
            correlationId,
            traceId
        );

        ErrorResponse response = new ErrorResponse(
            "VALIDATION_ERROR",
            message,
            400,
            Instant.now(),
            correlationId,
            traceId
        );
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        UUID traceId = UUID.randomUUID();
        UUID correlationId = getCorrelationIdFromMdc();

        logService.log(
            LogLevel.ERROR,
            LogCategory.SYSTEM,
            Map.of("component", "GlobalExceptionHandler", "exceptionType", ex.getClass().getSimpleName()),
            "UNHANDLED_EXCEPTION",
            Map.of("message", ex.getMessage() != null ? ex.getMessage() : "No message"),
            correlationId,
            traceId
        );

        ErrorResponse response = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            500,
            Instant.now(),
            correlationId,
            traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private UUID getCorrelationIdFromMdc() {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            try {
                return UUID.fromString(correlationId);
            } catch (IllegalArgumentException e) {
                // Fall through
            }
        }
        return null;
    }
}

