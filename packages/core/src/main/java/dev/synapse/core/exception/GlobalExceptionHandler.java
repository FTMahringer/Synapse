package dev.synapse.core.exception;

import dev.synapse.core.logging.LogCategory;
import dev.synapse.core.logging.LogLevel;
import dev.synapse.core.logging.SystemLogService;
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
            ex.getCorrelationId(),
            traceId
        );

        ErrorResponse response = ErrorResponse.from(ex, traceId);
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        org.springframework.web.bind.MethodArgumentNotValidException ex) {
        
        UUID traceId = UUID.randomUUID();
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
            null,
            traceId
        );

        ErrorResponse response = new ErrorResponse(
            "VALIDATION_ERROR",
            message,
            400,
            Instant.now(),
            null,
            traceId
        );
        return ResponseEntity.status(400).body(response);
    }
}
