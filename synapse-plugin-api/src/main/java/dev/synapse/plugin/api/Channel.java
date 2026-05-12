package dev.synapse.plugin.api;

/**
 * Plugin type: bidirectional messaging channel.
 *
 * Channels bridge external messaging platforms (Telegram, Discord, WhatsApp, etc.)
 * to SYNAPSE agents. Each installed channel claims a unique channel_id slot.
 *
 * Extends {@link SynapsePlugin} — implement onLoad/onUnload for lifecycle.
 */
public interface Channel extends SynapsePlugin {

    /**
     * Stable identifier for the channel platform, e.g. {@code "telegram"}.
     * Must match {@code channel_id} in the plugin manifest. Unique across all loaded plugins.
     */
    String getChannelId();

    /**
     * Called by core when an inbound message arrives from this channel's webhook or polling loop.
     * Route the message to an agent by calling {@link PluginContext#routeMessage(InboundMessage)}.
     *
     * Must not block indefinitely — use {@link PluginContext#executor()} for async work.
     */
    void onMessage(InboundMessage message) throws Exception;

    /**
     * Called by core to deliver an outbound message back to the channel.
     * Send the message to the external platform.
     *
     * Must complete within the message handler timeout.
     */
    void sendMessage(OutboundMessage message) throws Exception;

    /**
     * Called once by core during onLoad after the channel is registered.
     * Use this to set up webhooks, start polling loops, etc.
     */
    default void onInstall() throws Exception {}

    /**
     * Called once by core during onUnload before the channel is removed.
     * Use this to tear down webhooks, stop polling loops, etc.
     */
    default void onUninstall() throws Exception {}
}
