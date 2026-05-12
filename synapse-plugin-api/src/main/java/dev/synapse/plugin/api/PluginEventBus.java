package dev.synapse.plugin.api;

import java.util.function.Consumer;

/**
 * Plugin-to-core and plugin-to-plugin event bus.
 *
 * Plugins publish events to notify core of state changes.
 * Plugins subscribe to platform events to react to system changes
 * (e.g. a soft dependency becoming available).
 *
 * Event delivery is asynchronous. Handlers run on the plugin's executor.
 */
public interface PluginEventBus {

    /** Publish a plugin event to the platform event stream. */
    void publish(PluginEvent event);

    /**
     * Subscribe to platform events of a given topic.
     * Handler is invoked on the plugin's bounded executor.
     * Subscription is active until the plugin is unloaded.
     *
     * @param topic   event topic string, e.g. {@code "plugin.loaded"} or {@code "agent.message"}
     * @param handler consumer called for each matching event
     */
    void subscribe(String topic, Consumer<PluginEvent> handler);
}
