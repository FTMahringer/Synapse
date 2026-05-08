package dev.synapse.core.dto;

import dev.synapse.core.domain.Conversation;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
    UUID id,
    String agentId,
    UUID userId,
    UUID channelId,
    Instant startedAt,
    String status
) {
    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(
            conversation.getId(),
            conversation.getAgentId(),
            conversation.getUserId(),
            conversation.getChannelId(),
            conversation.getStartedAt(),
            conversation.getStatus().name()
        );
    }
}
