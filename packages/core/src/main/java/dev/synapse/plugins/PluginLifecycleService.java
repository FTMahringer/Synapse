package dev.synapse.plugins;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.repository.PluginRepository;
import dev.synapse.core.infrastructure.event.EventPublisher;
import dev.synapse.core.infrastructure.event.SynapseEvent;
import dev.synapse.core.infrastructure.event.SynapseEventType;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.plugins.loader.PluginDependencyResolver;
import dev.synapse.plugins.loader.PluginSandboxService;
import dev.synapse.plugins.loader.PluginStorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PluginStorageService storageService;
    private final PluginDependencyResolver dependencyResolver;
    private final PluginSandboxService sandboxService;

    public PluginLifecycleService(
        PluginRepository pluginRepository,
        ManifestValidator validator,
        SystemLogService logService,
        EventPublisher eventPublisher,
        PluginStatsService statsService,
        PluginStorageService storageService,
        PluginDependencyResolver dependencyResolver,
        PluginSandboxService sandboxService
    ) {
        this.pluginRepository = pluginRepository;
        this.validator = validator;
        this.logService = logService;
        this.eventPublisher = eventPublisher;
        this.statsService = statsService;
        this.storageService = storageService;
        this.dependencyResolver = dependencyResolver;
        this.sandboxService = sandboxService;
    }

    @Transactional
    @CacheEvict(value = "plugin-metadata", allEntries = true)
    public Plugin install(Map<String, Object> rawManifest) {
        PluginManifest manifest = PluginManifest.fromMap(rawManifest);

        ValidationResult result = validator.validate(manifest);
        if (!result.valid()) {
            throw new IllegalArgumentException(
                "Invalid manifest: " + String.join("; ", result.errors())
            );
        }

        Plugin plugin = new Plugin();
        plugin.setId(manifest.id());
        plugin.setName(manifest.name());
        plugin.setType(manifest.type());
        plugin.setVersion(manifest.version());
        plugin.setStatus(Plugin.PluginStatus.INSTALLED);
        plugin.setManifest(rawManifest);
        plugin.setStorageTier(Plugin.StorageTier.STAGING);
        plugin.setLoaderState(Plugin.LoaderState.UNLOADED);
        plugin.setTrustTier(detectTrustTier(rawManifest));

        Object apiVer = rawManifest.get("requires");
        if (apiVer instanceof Map<?, ?> reqMap) {
            Object av = reqMap.get("api_version");
            if (av != null) {
                plugin.setApiVersion(av.toString());
            }
        }

        // Resolve dependencies before saving
        PluginDependencyResolver.ResolutionResult depResult =
            dependencyResolver.resolve(rawManifest);
        if (!depResult.success()) {
            throw new IllegalArgumentException(
                "Dependency resolution failed: " + depResult.message()
            );
        }

        List<String> depIds = new ArrayList<>();
        for (var item : depResult.items()) {
            if (
                item.action() !=
                PluginDependencyResolver.ResolutionItem.Action.SKIP_SOFT
            ) {
                depIds.add(item.dependencyId());
            }
        }
        plugin.setDependencies(depIds);

        // Set sandbox defaults based on trust tier
        plugin.setLifecycleTimeoutMs(
            sandboxService.getLifecycleTimeoutMs(plugin)
        );
        plugin.setMessageTimeoutMs(
            sandboxService.getMessageHandlerTimeoutMs(plugin)
        );
        plugin.setMaxLogsPerMinute(sandboxService.getMaxLogsPerMinute(plugin));

        Plugin saved = pluginRepository.save(plugin);

        logService.log(
            LogLevel.INFO,
            LogCategory.PLUGIN,
            Map.of("component", "PluginLifecycleService"),
            "PLUGIN_INSTALLED",
            Map.of(
                "id",
                saved.getId(),
                "type",
                saved.getType().name(),
                "version",
                saved.getVersion()
            ),
            null,
            null
        );

        eventPublisher.publish(
            SynapseEvent.of(
                SynapseEventType.LOG_WRITTEN,
                "PluginLifecycleService",
                Map.of("event", "PLUGIN_INSTALLED", "pluginId", saved.getId())
            )
        );

        statsService.recordInstall(saved.getId());

        return saved;
    }

    @Transactional
    @CacheEvict(value = "plugin-metadata", allEntries = true)
    public Plugin enable(String id) {
        Plugin plugin = findById(id);
        plugin.setStatus(Plugin.PluginStatus.INSTALLED);
        Plugin saved = pluginRepository.save(plugin);

        logService.log(
            LogLevel.INFO,
            LogCategory.PLUGIN,
            Map.of("component", "PluginLifecycleService"),
            "PLUGIN_ENABLED",
            Map.of("id", id),
            null,
            null
        );

        statsService.recordEnable(id);

        return saved;
    }

    @Transactional
    @CacheEvict(value = "plugin-metadata", allEntries = true)
    public Plugin disable(String id) {
        Plugin plugin = findById(id);
        plugin.setStatus(Plugin.PluginStatus.DISABLED);
        Plugin saved = pluginRepository.save(plugin);

        logService.log(
            LogLevel.INFO,
            LogCategory.PLUGIN,
            Map.of("component", "PluginLifecycleService"),
            "PLUGIN_DISABLED",
            Map.of("id", id),
            null,
            null
        );

        statsService.recordDisable(id);

        return saved;
    }

    @Transactional
    @CacheEvict(value = "plugin-metadata", allEntries = true)
    public void uninstall(String id) {
        Plugin plugin = pluginRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plugin", id));

        // Delete JAR from storage
        storageService.deleteJar(id + ".jar");

        pluginRepository.deleteById(id);

        logService.log(
            LogLevel.INFO,
            LogCategory.PLUGIN,
            Map.of("component", "PluginLifecycleService"),
            "PLUGIN_UNINSTALLED",
            Map.of("id", id),
            null,
            null
        );
    }

    private Plugin.TrustTier detectTrustTier(Map<String, Object> rawManifest) {
        Object source = rawManifest.get("source");
        if (source instanceof String s) {
            if ("official".equalsIgnoreCase(s)) {
                return Plugin.TrustTier.OFFICIAL;
            }
        }
        Object author = rawManifest.get("author");
        if ("synapse-team".equals(author) || "FTMahringer".equals(author)) {
            return Plugin.TrustTier.OFFICIAL;
        }
        return Plugin.TrustTier.COMMUNITY;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "plugin-metadata", key = "'installed-plugins'")
    public List<Plugin> findAll() {
        return pluginRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(
        value = "plugin-metadata",
        key = "'installed-plugins:page:' + #page + ':' + #size"
    )
    public List<Plugin> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.ASC, "name")
        );
        return pluginRepository.findAll(pageRequest).getContent();
    }

    @Transactional(readOnly = true)
    public Plugin findById(String id) {
        return pluginRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plugin", id));
    }
}
