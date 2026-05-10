package dev.synapse.core.dto;

import java.time.Instant;
import java.util.UUID;

public record AgentMemoryEntryDTO(
    UUID id,
    String agentId,
    String key,
    String value,
    String namespace,
    String tier,
    Instant promotedAt,
    String promotionReason,
    Instant lastAccessedAt,
    Integer accessCount,
    Instant retentionUntil,
    Instant createdAt,
    Instant updatedAt
) {}
