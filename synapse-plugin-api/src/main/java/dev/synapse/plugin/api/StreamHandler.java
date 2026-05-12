package dev.synapse.plugin.api;

/**
 * Callback interface for streaming completions from a ModelProvider.
 *
 * Implementations are provided by core and must not be held beyond the stream lifecycle.
 * All methods are called on the plugin's executor thread.
 */
public interface StreamHandler {

    /**
     * Called for each token or chunk as it arrives from the provider.
     * @param chunk partial text token or chunk
     */
    void onChunk(String chunk);

    /**
     * Called once when the stream ends successfully.
     * @param finishReason provider-specific finish reason (e.g. {@code "stop"})
     * @param totalTokens  total tokens used if reported by provider, or -1 if unknown
     */
    void onComplete(String finishReason, int totalTokens);

    /**
     * Called if the stream fails. No further calls will be made after this.
     * @param cause the exception that caused the failure
     */
    void onError(Throwable cause);
}
