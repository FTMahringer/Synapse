package dev.synapse.core.infrastructure.exception;

import java.util.UUID;

public abstract class ApiException extends RuntimeException {
    private final String code;
    private final int httpStatus;
    private final UUID correlationId;

    protected ApiException(String code, String message, int httpStatus, UUID correlationId) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.correlationId = correlationId;
    }

    protected ApiException(String code, String message, int httpStatus) {
        this(code, message, httpStatus, null);
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }
}
