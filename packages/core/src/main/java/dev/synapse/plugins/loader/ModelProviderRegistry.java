package dev.synapse.plugins.loader;

import dev.synapse.plugin.api.ModelProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for loaded {@link ModelProvider} plugins.
 *
 * <p>Each provider claims a unique {@code provider_id} slot.
 * Two plugins cannot claim the same provider_id simultaneously.
 */
@Component
public class ModelProviderRegistry {

    private final Map<String, LoadedProvider> providers = new ConcurrentHashMap<>();

    /**
     * Registers a loaded model provider.
     *
     * @param loaded the loaded plugin record
     * @param provider the model provider implementation
     * @throws IllegalStateException if the provider_id is already claimed
     */
    public void register(LoadedPlugin loaded, ModelProvider provider) {
        String providerId = provider.getProviderId();
        if (providers.containsKey(providerId)) {
            throw new IllegalStateException(
                "Provider slot '" + providerId + "' is already claimed by plugin " +
                providers.get(providerId).pluginId());
        }
        providers.put(providerId, new LoadedProvider(loaded.pluginId(), provider));
    }

    /** Unregisters a provider by plugin id. */
    public void unregisterByPluginId(String pluginId) {
        providers.values().removeIf(p -> p.pluginId().equals(pluginId));
    }

    /** Returns a provider by its provider_id. */
    public Optional<ModelProvider> getProvider(String providerId) {
        return Optional.ofNullable(providers.get(providerId)).map(LoadedProvider::provider);
    }

    /** Returns all registered providers. */
    public Collection<ModelProvider> getAllProviders() {
        return providers.values().stream().map(LoadedProvider::provider).toList();
    }

    /** Returns true if the provider_id is already claimed. */
    public boolean isClaimed(String providerId) {
        return providers.containsKey(providerId);
    }

    /** Returns the plugin id that owns a provider_id, or empty. */
    public Optional<String> getOwnerPluginId(String providerId) {
        return Optional.ofNullable(providers.get(providerId)).map(LoadedProvider::pluginId);
    }

    private record LoadedProvider(String pluginId, ModelProvider provider) {}
}
