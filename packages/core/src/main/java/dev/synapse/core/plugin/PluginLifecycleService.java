package dev.synapse.core.plugin;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.infrastructure.event.EventPublisher;
import dev.synapse.core.infrastructure.event.SynapseEvent;
import dev.synapse.core.infrastructure.event.SynapseEventType;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.common.repository.PluginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Plugin lifecycle: install, enable, disable, uninstall.
 * Validates manifest before install.
 */
@Service
public class PluginLifecycleService {

    private final PluginRepository pluginRepository;
    private final ManifestValidator validator;
    private final SystemLogService logService;
    private final EventPublisher eventPublisher;
    private final PluginStatsService statsService;

    public PluginLifecycleService(
        PluginRepository pluginRepository,
        ManifestValidator validator,
        SystemLogService logService,
        EventPublisher eventPublisher,
        PluginStatsService statsService
    ) {
        this.pluginRepository = pluginRepository;
        this.validator = validator;
        this.logService = logService;
        this.eventPublisher = eventPublisher;
        this.statsService = statsService;
    }

    @Transactional
    public Plugin install(Map<String, Object> rawManifest) {
        PluginManifest manifest = PluginManifest.fromMap(rawManifest);

        ValidationResult result = validator.validate(manifest);
        if (!result.valid()) {
            throw new IllegalArgumentException("Invalid manifest: " + String.join("; ", result.errors()));
        }

        Plugin plugin = new Plugin();
        plugin.setId(manifest.id());
        plugin.setName(manifest.name());
        plugin.setType(manifest.type());
        plugin.setVersion(manifest.version());
        plugin.setStatus(Plugin.PluginStatus.INSTALLED);
        plugin.setManifest(rawManifest);

        Plugin saved = pluginRepository.save(plugin);

        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "PluginLifecycleService"),
            "PLUGIN_INSTALLED",
            Map.of("id", saved.getId(), "type", saved.getType().name(), "version", saved.getVersion()),
            null, null);

        eventPublisher.publish(SynapseEvent.of(SynapseEventType.LOG_WRITTEN, "PluginLifecycleService",
            Map.of("event", "PLUGIN_INSTALLED", "pluginId", saved.getId())));

        statsService.recordInstall(saved.getId());

        return saved;
    }

    @Transactional
    public Plugin enable(String id) {
        Plugin plugin = findById(id);
        plugin.setStatus(Plugin.PluginStatus.INSTALLED);
        Plugin saved = pluginRepository.save(plugin);

        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "PluginLifecycleService"),
            "PLUGIN_ENABLED",
            Map.of("id", id),
            null, null);

        statsService.recordEnable(id);

        return saved;
    }

    @Transactional
    public Plugin disable(String id) {
        Plugin plugin = findById(id);
        plugin.setStatus(Plugin.PluginStatus.DISABLED);
        Plugin saved = pluginRepository.save(plugin);

        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "PluginLifecycleService"),
            "PLUGIN_DISABLED",
            Map.of("id", id),
            null, null);

        statsService.recordDisable(id);

        return saved;
    }

    @Transactional
    public void uninstall(String id) {
        if (!pluginRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plugin", id);
        }
        pluginRepository.deleteById(id);

        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "PluginLifecycleService"),
            "PLUGIN_UNINSTALLED",
            Map.of("id", id),
            null, null);
    }

    @Transactional(readOnly = true)
    public List<Plugin> findAll() {
        return pluginRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Plugin findById(String id) {
        return pluginRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plugin", id));
    }
}
