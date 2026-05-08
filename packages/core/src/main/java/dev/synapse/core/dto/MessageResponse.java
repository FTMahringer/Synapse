package dev.synapse.core.dto;

import dev.synapse.core.domain.Message;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID conversationId,
    String role,
    String content,
    Integer tokens,
    OffsetDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getConversation().getId(),
            message.getRole().name(),
            message.getContent(),
            message.getTokens(),
            message.getCreatedAt()
        );
    }
}
