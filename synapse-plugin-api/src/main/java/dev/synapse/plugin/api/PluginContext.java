package dev.synapse.plugin.api;

import java.util.concurrent.ExecutorService;

/**
 * Injected into every plugin at load time.
 *
 * Provides scoped access to platform services. The only way for a plugin
 * to interact with SYNAPSE core — no direct Spring bean or service access.
 */
public interface PluginContext {

    /** Scoped logger — all output tagged with plugin id, routed to system log. */
    PluginLogger logger();

    /** Typed config wrapper backed by manifest config_schema values. */
    PluginConfig config();

    /** Event bus for publishing events to core and subscribing to platform events. */
    PluginEventBus eventBus();

    /**
     * Bounded virtual thread pool for this plugin.
     * All plugin async work MUST use this executor.
     * Thread limits are enforced per trust tier (Community: 10, Official: 25).
     */
    ExecutorService executor();

    /**
     * Active authentication mode for this plugin instance.
     * Check before accessing config credentials — avoids raw config key branching.
     */
    AuthMode authMode();

    /**
     * Route an inbound message to the SYNAPSE agent router.
     * Used by Channel plugins to deliver received messages to agents.
     */
    void routeMessage(InboundMessage message);
}
