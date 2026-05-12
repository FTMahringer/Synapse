package dev.synapse.plugin.api;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable event payload for the plugin event bus.
 */
public final class PluginEvent {

    private final String topic;
    private final String sourcePluginId;
    private final Instant timestamp;
    private final Map<String, Object> payload;

    public PluginEvent(String topic, String sourcePluginId, Map<String, Object> payload) {
        this.topic = topic;
        this.sourcePluginId = sourcePluginId;
        this.timestamp = Instant.now();
        this.payload = Map.copyOf(payload);
    }

    public String getTopic() { return topic; }

    public String getSourcePluginId() { return sourcePluginId; }

    public Instant getTimestamp() { return timestamp; }

    /** Unmodifiable payload map. */
    public Map<String, Object> getPayload() { return payload; }

    @Override
    public String toString() {
        return "PluginEvent{topic='" + topic + "', source='" + sourcePluginId + "', ts=" + timestamp + "}";
    }
}
