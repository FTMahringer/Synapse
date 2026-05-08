package dev.synapse.core.dto;

import dev.synapse.core.domain.Message;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID conversationId,
    String role,
    String content,
    Integer tokens,
    Instant createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getConversationId(),
            message.getRole().name(),
            message.getContent(),
            message.getTokens(),
            message.getCreatedAt()
        );
    }
}
