package dev.synapse.core.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CollaborationMessageDTO(
    UUID id,
    UUID sessionId,
    String fromAgentId,
    String toAgentId,
    String messageType,
    String content,
    Map<String, Object> metadata,
    Instant createdAt
) {}
