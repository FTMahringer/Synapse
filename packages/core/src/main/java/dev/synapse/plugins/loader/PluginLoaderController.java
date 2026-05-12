package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.repository.PluginRepository;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.PluginDTO;
import dev.synapse.plugins.PluginLifecycleService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for plugin loader operations.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /api/plugins/loader/status} — list all plugins with loader state</li>
 *   <li>{@code POST /api/plugins/{id}/load} — load a plugin from its stored JAR</li>
 *   <li>{@code POST /api/plugins/{id}/unload} — unload a plugin</li>
 *   <li>{@code POST /api/plugins/{id}/reload} — reload a plugin</li>
 *   <li>{@code GET /api/plugins/loader/orphans} — list orphaned staging JARs</li>
 *   <li>{@code POST /api/plugins/loader/promote} — promote all staging JARs to system</li>
 *   <li>{@code POST /api/plugins/{id}/resolve-deps} — resolve dependencies for a plugin</li>
 *   <li>{@code POST /api/plugins/{id}/update} — update plugin to new JAR</li>
 *   <li>{@code POST /api/plugins/check-slot-clash} — check for slot conflicts</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/plugins")
public class PluginLoaderController {

    private final PluginLoaderService loaderService;
    private final PluginStorageService storageService;
    private final PluginLifecycleService lifecycleService;
    private final PluginRepository pluginRepository;
    private final ChannelRegistry channelRegistry;
    private final ModelProviderRegistry providerRegistry;
    private final PluginDependencyResolver dependencyResolver;
    private final PluginUpdateService updateService;

    public PluginLoaderController(
        PluginLoaderService loaderService,
        PluginStorageService storageService,
        PluginLifecycleService lifecycleService,
        PluginRepository pluginRepository,
        ChannelRegistry channelRegistry,
        ModelProviderRegistry providerRegistry,
        PluginDependencyResolver dependencyResolver,
        PluginUpdateService updateService
    ) {
        this.loaderService = loaderService;
        this.storageService = storageService;
        this.lifecycleService = lifecycleService;
        this.pluginRepository = pluginRepository;
        this.channelRegistry = channelRegistry;
        this.providerRegistry = providerRegistry;
        this.dependencyResolver = dependencyResolver;
        this.updateService = updateService;
    }

    @GetMapping("/loader/status")
    public List<Map<String, Object>> loaderStatus() {
        return loaderService
            .getAllLoaded()
            .stream()
            .map(lp ->
                Map.<String, Object>of(
                    "pluginId",
                    lp.pluginId(),
                    "version",
                    lp.version(),
                    "jarPath",
                    lp.jarPath().toString(),
                    "loadedAt",
                    lp.loadedAt(),
                    "isChannel",
                    lp.instance() instanceof dev.synapse.plugin.api.Channel,
                    "isModelProvider",
                    lp.instance() instanceof
                        dev.synapse.plugin.api.ModelProvider
                )
            )
            .toList();
    }

    @PostMapping("/{id}/load")
    @ResponseStatus(HttpStatus.OK)
    public PluginDTO loadPlugin(@PathVariable String id)
        throws PluginLoadException {
        Plugin dbPlugin = lifecycleService.findById(id);
        LoadedPlugin loaded = loaderService.loadPlugin(dbPlugin);
        registerInRegistry(loaded, dbPlugin);
        return DtoMapper.toDTO(dbPlugin);
    }

    @PostMapping("/{id}/unload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unloadPlugin(@PathVariable String id) {
        loaderService.unloadPlugin(id);
        channelRegistry.unregisterByPluginId(id);
        providerRegistry.unregisterByPluginId(id);
    }

    @PostMapping("/{id}/reload")
    @ResponseStatus(HttpStatus.OK)
    public PluginDTO reloadPlugin(@PathVariable String id)
        throws PluginLoadException {
        Plugin dbPlugin = lifecycleService.findById(id);
        loaderService.unloadPlugin(id);
        channelRegistry.unregisterByPluginId(id);
        providerRegistry.unregisterByPluginId(id);
        LoadedPlugin loaded = loaderService.loadPlugin(dbPlugin);
        registerInRegistry(loaded, dbPlugin);
        return DtoMapper.toDTO(dbPlugin);
    }

    @GetMapping("/loader/orphans")
    public Map<String, Object> orphanedJars() {
        List<String> orphans = storageService
            .listStagingJars()
            .stream()
            .map(p -> p.getFileName().toString())
            .toList();
        return Map.of(
            "hasOrphans",
            !orphans.isEmpty(),
            "count",
            orphans.size(),
            "jars",
            orphans
        );
    }

    @PostMapping("/loader/promote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void promoteStaging() {
        storageService.promoteAllStaging();
    }

    /**
     * Resolves dependencies for a plugin manifest.
     */
    @PostMapping("/{id}/resolve-deps")
    public Map<String, Object> resolveDependencies(@PathVariable String id) {
        Plugin plugin = lifecycleService.findById(id);
        PluginDependencyResolver.ResolutionResult result =
            dependencyResolver.resolve(plugin.getManifest());
        return Map.of(
            "pluginId",
            result.pluginId(),
            "success",
            result.success(),
            "items",
            result.items().stream().map(this::itemToMap).toList(),
            "installOrder",
            result.installOrder(),
            "hasUpdatePrompts",
            result.hasUpdatePrompts(),
            "message",
            result.message() != null ? result.message() : ""
        );
    }

    /**
     * Updates a plugin to a new JAR version.
     */
    @PostMapping("/{id}/update")
    @ResponseStatus(HttpStatus.OK)
    public PluginDTO updatePlugin(
        @PathVariable String id,
        @RequestBody Map<String, String> body
    ) throws PluginLoadException {
        String jarPath = body.get("jarPath");
        if (jarPath == null || jarPath.isBlank()) {
            throw new IllegalArgumentException("jarPath is required");
        }
        String trimmed = jarPath.trim();
        if (
            trimmed.contains("..") ||
            trimmed.contains("/") ||
            trimmed.contains("\\") ||
            !trimmed.endsWith(".jar")
        ) {
            throw new IllegalArgumentException(
                "jarPath must be a safe .jar filename"
            );
        }
        LoadedPlugin loaded = updateService.updatePlugin(
            id,
            Path.of(trimmed).getFileName()
        );
        Plugin dbPlugin = lifecycleService.findById(id);
        return DtoMapper.toDTO(dbPlugin);
    }

    /**
     * Checks for slot clashes given a manifest.
     */
    @PostMapping("/check-slot-clash")
    public Map<String, Object> checkSlotClash(
        @RequestBody Map<String, Object> manifest
    ) {
        var clash = dependencyResolver.checkSlotClash(
            manifest,
            channelRegistry,
            providerRegistry
        );
        return Map.of(
            "hasClash",
            clash.isPresent(),
            "message",
            clash.orElse("")
        );
    }

    private Map<String, Object> itemToMap(
        PluginDependencyResolver.ResolutionItem item
    ) {
        return Map.of(
            "dependencyId",
            item.dependencyId(),
            "versionSpec",
            item.versionSpec(),
            "action",
            item.action().name(),
            "installedVersion",
            item.installedVersion() != null ? item.installedVersion() : ""
        );
    }

    private void registerInRegistry(LoadedPlugin loaded, Plugin dbPlugin) {
        var instance = loaded.instance();
        if (instance instanceof dev.synapse.plugin.api.Channel channel) {
            channelRegistry.register(loaded, channel);
        } else if (
            instance instanceof dev.synapse.plugin.api.ModelProvider provider
        ) {
            providerRegistry.register(loaded, provider);
        }
    }
}
