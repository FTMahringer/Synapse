package dev.synapse.core.dto;

import java.time.Instant;
import java.util.UUID;

public record SharedContextEntryDTO(
    UUID id,
    UUID sessionId,
    String contextKey,
    String contextValue,
    String updatedByAgentId,
    Integer version,
    Instant createdAt,
    Instant updatedAt
) {}
