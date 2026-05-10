package dev.synapse.core.infrastructure.exception;

import java.util.UUID;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s with identifier '%s' not found", resource, identifier), 
              404);
    }

    public ResourceNotFoundException(String resource, String identifier, UUID correlationId) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s with identifier '%s' not found", resource, identifier),
              404,
              correlationId);
    }
}
