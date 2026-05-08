package dev.synapse.core.exception;

public class RequestTimeoutException extends RuntimeException {
    public RequestTimeoutException(String message) {
        super(message);
    }
    
    public RequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
