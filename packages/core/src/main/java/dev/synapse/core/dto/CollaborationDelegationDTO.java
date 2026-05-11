package dev.synapse.core.dto;

import java.time.Instant;
import java.util.UUID;

public record CollaborationDelegationDTO(
    UUID id,
    UUID sessionId,
    UUID taskId,
    String fromAgentId,
    String toAgentId,
    String status,
    String delegationNote,
    Instant createdAt,
    Instant updatedAt
) {}
