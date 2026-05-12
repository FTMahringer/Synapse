package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.repository.PluginRepository;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.PluginDTO;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
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

    public PluginLoaderController(
        PluginLoaderService loaderService,
        PluginStorageService storageService,
        PluginLifecycleService lifecycleService,
        PluginRepository pluginRepository,
        ChannelRegistry channelRegistry,
        ModelProviderRegistry providerRegistry
    ) {
        this.loaderService = loaderService;
        this.storageService = storageService;
        this.lifecycleService = lifecycleService;
        this.pluginRepository = pluginRepository;
        this.channelRegistry = channelRegistry;
        this.providerRegistry = providerRegistry;
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
        java.nio.file.Path jarPath = resolveJarPath(dbPlugin);
        LoadedPlugin loaded = loaderService.loadPlugin(jarPath, dbPlugin);
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
        java.nio.file.Path jarPath = resolveJarPath(dbPlugin);
        loaderService.unloadPlugin(id);
        channelRegistry.unregisterByPluginId(id);
        providerRegistry.unregisterByPluginId(id);
        LoadedPlugin loaded = loaderService.loadPlugin(jarPath, dbPlugin);
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

    private java.nio.file.Path resolveJarPath(Plugin dbPlugin) {
        String jarName = dbPlugin.getId() + ".jar";
        java.nio.file.Path systemJar = storageService
            .getSystemDir()
            .resolve(jarName);
        if (java.nio.file.Files.exists(systemJar)) {
            return systemJar;
        }
        java.nio.file.Path stagingJar = storageService
            .getStagingDir()
            .resolve(jarName);
        if (java.nio.file.Files.exists(stagingJar)) {
            return stagingJar;
        }
        // Try to find any matching JAR
        List<java.nio.file.Path> candidates = storageService
            .listSystemJars()
            .stream()
            .filter(p ->
                p.getFileName().toString().startsWith(dbPlugin.getId() + "-")
            )
            .toList();
        if (!candidates.isEmpty()) {
            return candidates.get(0);
        }
        throw new ResourceNotFoundException("Plugin JAR", dbPlugin.getId());
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
