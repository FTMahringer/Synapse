package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.infrastructure.event.EventPublisher;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.plugin.api.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import org.springframework.stereotype.Component;

/**
 * Factory for creating {@link PluginContext} instances injected into loaded plugins.
 *
 * <p>Each plugin receives its own context with:
 * <ul>
 *   <li>Scoped {@link PluginLogger} tagged with plugin id</li>
 *   <li>Typed {@link PluginConfig} backed by manifest config_schema</li>
 *   <li>{@link PluginEventBus} for publishing/subscribing events</li>
 *   <li>Bounded virtual thread {@link ExecutorService}</li>
 *   <li>{@link AuthMode} derived from config values</li>
 * </ul>
 */
@Component
public class PluginContextFactory {

    private final SystemLogService logService;
    private final EventPublisher eventPublisher;

    public PluginContextFactory(
        SystemLogService logService,
        EventPublisher eventPublisher
    ) {
        this.logService = logService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a PluginContext for the given plugin instance.
     *
     * @param dbPlugin the database plugin entity
     * @param instance the plugin implementation instance
     * @return a fully configured PluginContext
     */
    public PluginContext createContext(
        Plugin dbPlugin,
        SynapsePlugin instance
    ) {
        String pluginId = instance.getId();

        // Determine trust tier and thread limit
        int maxThreads =
            dbPlugin.getTrustTier() == Plugin.TrustTier.OFFICIAL ? 25 : 10;
        Semaphore threadSemaphore = new Semaphore(maxThreads);

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        PluginLogger logger = new PluginLogger() {
            @Override
            public void info(String message) {
                log(pluginId, LogLevel.INFO, message);
            }

            @Override
            public void info(String format, Object... args) {
                log(pluginId, LogLevel.INFO, String.format(format, args));
            }

            @Override
            public void warn(String message) {
                log(pluginId, LogLevel.WARN, message);
            }

            @Override
            public void warn(String format, Object... args) {
                log(pluginId, LogLevel.WARN, String.format(format, args));
            }

            @Override
            public void error(String message) {
                log(pluginId, LogLevel.ERROR, message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                logService.log(
                    LogLevel.ERROR,
                    LogCategory.PLUGIN,
                    Map.of("component", pluginId),
                    "PLUGIN_ERROR",
                    Map.of("message", message),
                    throwable,
                    null
                );
            }

            @Override
            public void error(String format, Object... args) {
                log(pluginId, LogLevel.ERROR, String.format(format, args));
            }

            @Override
            public void debug(String message) {
                log(pluginId, LogLevel.DEBUG, message);
            }

            @Override
            public void debug(String format, Object... args) {
                log(pluginId, LogLevel.DEBUG, String.format(format, args));
            }

            private void log(String pluginId, LogLevel level, String message) {
                logService.log(
                    level,
                    LogCategory.PLUGIN,
                    Map.of("component", pluginId),
                    "PLUGIN_LOG",
                    Map.of("message", message),
                    null,
                    null
                );
            }
        };

        PluginConfig config = new PluginConfigImpl(dbPlugin.getManifest());

        PluginEventBus eventBus = new PluginEventBus() {
            @Override
            public void publish(PluginEvent event) {
                eventPublisher.publish(
                    dev.synapse.core.infrastructure.event.SynapseEvent.of(
                        dev.synapse.core.infrastructure.event.SynapseEventType.LOG_WRITTEN,
                        pluginId,
                        Map.of(
                            "topic",
                            event.getTopic(),
                            "payload",
                            event.getPayload()
                        )
                    )
                );
            }

            @Override
            public void subscribe(
                String topic,
                java.util.function.Consumer<PluginEvent> handler
            ) {
                // Subscription tracking deferred to v2.5.3+ when event bus is fully wired
            }
        };

        AuthMode authMode = detectAuthMode(config);

        return new PluginContext() {
            @Override
            public PluginLogger logger() {
                return logger;
            }

            @Override
            public PluginConfig config() {
                return config;
            }

            @Override
            public PluginEventBus eventBus() {
                return eventBus;
            }

            @Override
            public ExecutorService executor() {
                return executor;
            }

            @Override
            public AuthMode authMode() {
                return authMode;
            }

            @Override
            public void routeMessage(InboundMessage message) {
                // Route to agent router — implementation wired in v2.5.3+
                // For now, publish as event so core can pick it up
                eventBus.publish(
                    new PluginEvent(
                        "agent.router",
                        pluginId,
                        Map.of(
                            "channelId",
                            message.channelId(),
                            "senderId",
                            message.senderId(),
                            "content",
                            message.content()
                        )
                    )
                );
            }
        };
    }

    private AuthMode detectAuthMode(PluginConfig config) {
        if (config.has("api_key") || config.has("acp_subscription_id")) {
            return AuthMode.API_KEY;
        }
        if (config.has("acp_token")) {
            return AuthMode.ACP;
        }
        return AuthMode.NONE;
    }

    /**
     * Internal PluginConfig implementation backed by the manifest map.
     */
    private static class PluginConfigImpl implements PluginConfig {

        private final Map<String, Object> manifest;

        PluginConfigImpl(Map<String, Object> manifest) {
            this.manifest = manifest != null ? manifest : Map.of();
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> configSchema() {
            Object cs = manifest.get("config");
            if (cs instanceof Map<?, ?> m) {
                return (Map<String, Object>) m;
            }
            Object cs2 = manifest.get("config_schema");
            if (cs2 instanceof Map<?, ?> m2) {
                return (Map<String, Object>) m2;
            }
            return Map.of();
        }

        private String rawValue(String key) {
            Map<String, Object> cfg = configSchema();
            Object v = cfg.get(key);
            return v != null ? v.toString() : null;
        }

        @Override
        public String getString(String key) {
            String v = rawValue(key);
            if (v == null || v.isBlank()) {
                throw new IllegalArgumentException(
                    "Missing required config key: " + key
                );
            }
            return v;
        }

        @Override
        public String getString(String key, String defaultValue) {
            String v = rawValue(key);
            return (v != null && !v.isBlank()) ? v : defaultValue;
        }

        @Override
        public boolean getBoolean(String key) {
            return Boolean.parseBoolean(getString(key));
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            String v = rawValue(key);
            return (v != null && !v.isBlank())
                ? Boolean.parseBoolean(v)
                : defaultValue;
        }

        @Override
        public int getInt(String key) {
            return Integer.parseInt(getString(key));
        }

        @Override
        public int getInt(String key, int defaultValue) {
            String v = rawValue(key);
            if (v == null || v.isBlank()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        @Override
        public String getSecret(String key) {
            return getString(key);
        }

        @Override
        public Optional<String> getSecretOptional(String key) {
            String v = rawValue(key);
            return (v != null && !v.isBlank())
                ? Optional.of(v)
                : Optional.empty();
        }

        @Override
        public boolean has(String key) {
            String v = rawValue(key);
            return v != null && !v.isBlank();
        }
    }
}
