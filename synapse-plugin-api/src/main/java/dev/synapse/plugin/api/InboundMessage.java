package dev.synapse.plugin.api;

import java.time.Instant;
import java.util.Map;

/**
 * A message received from an external platform via a Channel plugin.
 */
public final class InboundMessage {

    private final String channelId;
    private final String externalUserId;
    private final String conversationId;
    private final String text;
    private final Instant receivedAt;
    private final Map<String, Object> metadata;

    public InboundMessage(
            String channelId,
            String externalUserId,
            String conversationId,
            String text,
            Map<String, Object> metadata) {
        this.channelId = channelId;
        this.externalUserId = externalUserId;
        this.conversationId = conversationId;
        this.text = text;
        this.receivedAt = Instant.now();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public String getChannelId() { return channelId; }

    public String getExternalUserId() { return externalUserId; }

    /** Platform-specific conversation/thread/chat id. */
    public String getConversationId() { return conversationId; }

    public String getText() { return text; }

    public Instant getReceivedAt() { return receivedAt; }

    /** Platform-specific metadata (attachments, message ids, etc.). */
    public Map<String, Object> getMetadata() { return metadata; }
}
