package dev.synapse.plugin.api;

import java.util.Map;

/**
 * A message to be delivered to an external platform via a Channel plugin.
 */
public final class OutboundMessage {

    private final String channelId;
    private final String externalUserId;
    private final String conversationId;
    private final String text;
    private final Map<String, Object> metadata;

    public OutboundMessage(
            String channelId,
            String externalUserId,
            String conversationId,
            String text,
            Map<String, Object> metadata) {
        this.channelId = channelId;
        this.externalUserId = externalUserId;
        this.conversationId = conversationId;
        this.text = text;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public String getChannelId() { return channelId; }

    public String getExternalUserId() { return externalUserId; }

    public String getConversationId() { return conversationId; }

    public String getText() { return text; }

    /** Optional metadata for platform-specific features (reply-to, formatting, etc.). */
    public Map<String, Object> getMetadata() { return metadata; }
}
