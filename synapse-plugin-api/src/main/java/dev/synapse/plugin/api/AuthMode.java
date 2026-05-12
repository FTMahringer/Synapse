package dev.synapse.plugin.api;

/**
 * Authentication mode active for a plugin instance.
 * Check via {@link PluginContext#authMode()} before accessing credentials.
 */
public enum AuthMode {

    /** Standard API key auth — use {@code config().getSecret("api_key")}. */
    API_KEY,

    /** Anthropic Claude Platform subscription login — use {@code config().getSecret("acp_subscription_id")}. */
    ACP,

    /** No authentication required (e.g. local Ollama). */
    NONE
}
