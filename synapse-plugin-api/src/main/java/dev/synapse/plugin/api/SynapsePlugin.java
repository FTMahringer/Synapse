package dev.synapse.plugin.api;

/**
 * Base interface for all SYNAPSE plugin types.
 *
 * Implementations must be public, non-abstract, and have a no-arg constructor.
 * The plugin loader discovers implementations via ServiceLoader.
 *
 * Lifecycle: onLoad() → (active) → onUnload()
 */
public interface SynapsePlugin {

    /** Unique plugin id in the form {@code author/plugin-name}. */
    String getId();

    /** Human-readable plugin name for display in dashboard and CLI. */
    String getName();

    /** Semver string, e.g. {@code "1.0.0"}. Must match the manifest. */
    String getVersion();

    /**
     * Called once after the ClassLoader and ModuleLayer are set up.
     * Receives an injected context providing logger, config, event bus, and executor.
     *
     * Must complete within the lifecycle hook timeout (default: 30s Official / 10s Community).
     * Throwing here marks the plugin ERROR and disables it.
     */
    void onLoad(PluginContext context) throws Exception;

    /**
     * Called before the ClassLoader is torn down.
     * Release resources, flush state, deregister hooks.
     *
     * Must complete within the lifecycle hook timeout.
     * Throwing here is logged but does not block unload.
     */
    void onUnload() throws Exception;
}
