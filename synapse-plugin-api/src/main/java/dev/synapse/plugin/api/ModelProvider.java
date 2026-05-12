package dev.synapse.plugin.api;

import java.util.List;

/**
 * Plugin type: LLM backend provider.
 *
 * Model providers supply agents with completion and streaming capabilities.
 * Each installed provider claims a unique provider_id slot.
 *
 * Extends {@link SynapsePlugin} — implement onLoad/onUnload for lifecycle.
 */
public interface ModelProvider extends SynapsePlugin {

    /**
     * Stable provider identifier, e.g. {@code "openai"} or {@code "anthropic"}.
     * Must match {@code provider_id} in the plugin manifest. Unique across all loaded plugins.
     */
    String getProviderId();

    /**
     * Perform a blocking completion request. Returns when the full response is ready.
     * Use {@link PluginContext#executor()} to avoid blocking the calling thread for long-running requests.
     */
    CompletionResponse complete(CompletionRequest request) throws Exception;

    /**
     * Perform a streaming completion. Calls {@code handler} for each token/chunk as it arrives.
     * Must call {@link StreamHandler#onComplete()} or {@link StreamHandler#onError(Throwable)} when done.
     */
    void stream(CompletionRequest request, StreamHandler handler) throws Exception;

    /**
     * Return declared capabilities for this provider (e.g. streaming, tool calling, vision).
     */
    ModelCapabilities getCapabilities();

    /**
     * Return available models from the provider. Called when user lists models in the dashboard.
     * May make a network call — implement with appropriate timeout handling.
     */
    List<ModelInfo> listModels() throws Exception;

    /**
     * Called by core to inject auth credentials before onLoad completes.
     * Check {@link PluginContext#authMode()} to determine which credential was supplied.
     */
    void configure(PluginContext context) throws Exception;
}
