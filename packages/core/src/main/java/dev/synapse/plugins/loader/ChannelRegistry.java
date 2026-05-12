package dev.synapse.plugins.loader;

import dev.synapse.plugin.api.Channel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for loaded {@link Channel} plugins.
 *
 * <p>Each channel claims a unique {@code channel_id} slot.
 * Two plugins cannot claim the same channel_id simultaneously.
 */
@Component
public class ChannelRegistry {

    private final Map<String, LoadedChannel> channels = new ConcurrentHashMap<>();

    /**
     * Registers a loaded channel.
     *
     * @param loaded the loaded plugin record
     * @param channel the channel implementation
     * @throws IllegalStateException if the channel_id is already claimed
     */
    public void register(LoadedPlugin loaded, Channel channel) {
        String channelId = channel.getChannelId();
        if (channels.containsKey(channelId)) {
            throw new IllegalStateException(
                "Channel slot '" + channelId + "' is already claimed by plugin " +
                channels.get(channelId).pluginId());
        }
        channels.put(channelId, new LoadedChannel(loaded.pluginId(), channel));
    }

    /** Unregisters a channel by plugin id. */
    public void unregisterByPluginId(String pluginId) {
        channels.values().removeIf(c -> c.pluginId().equals(pluginId));
    }

    /** Returns a channel by its channel_id. */
    public Optional<Channel> getChannel(String channelId) {
        return Optional.ofNullable(channels.get(channelId)).map(LoadedChannel::channel);
    }

    /** Returns all registered channels. */
    public Collection<Channel> getAllChannels() {
        return channels.values().stream().map(LoadedChannel::channel).toList();
    }

    /** Returns true if the channel_id is already claimed. */
    public boolean isClaimed(String channelId) {
        return channels.containsKey(channelId);
    }

    /** Returns the plugin id that owns a channel_id, or empty. */
    public Optional<String> getOwnerPluginId(String channelId) {
        return Optional.ofNullable(channels.get(channelId)).map(LoadedChannel::pluginId);
    }

    private record LoadedChannel(String pluginId, Channel channel) {}
}
