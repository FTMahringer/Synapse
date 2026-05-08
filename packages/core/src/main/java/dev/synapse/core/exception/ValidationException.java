package dev.synapse.core.exception;

import java.util.UUID;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, 400);
    }

    public ValidationException(String message, UUID correlationId) {
        super("VALIDATION_ERROR", message, 400, correlationId);
    }
}
