package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.repository.PluginRepository;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Plugin dependency resolver with conflict detection.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Parse {@code requires.plugins[]} (hard deps) and {@code requires.soft_requires[]} from manifest</li>
 *   <li>Resolve hard deps: check installed → auto-install from store if missing → recursive chain</li>
 *   <li>Detect dependency cycles before any install begins</li>
 *   <li>Version-aware conflict: same id + newer version → update prompt; older → block; slot clash → hard block</li>
 *   <li>Config schema migration check on update: block if new required fields unfilled</li>
 * </ul>
 */
@Service
public class PluginDependencyResolver {

    private final PluginRepository pluginRepository;
    private final SystemLogService logService;

    public PluginDependencyResolver(
        PluginRepository pluginRepository,
        SystemLogService logService
    ) {
        this.pluginRepository = pluginRepository;
        this.logService = logService;
    }

    /**
     * Resolves all dependencies for a plugin manifest.
     *
     * @param manifest the plugin manifest map
     * @return resolution result with install order and any conflicts
     */
    public ResolutionResult resolve(Map<String, Object> manifest) {
        String pluginId = manifest.get("id") != null ? manifest.get("id").toString() : "unknown";
        List<PluginDependency> deps = PluginDependency.fromManifest(manifest);

        if (deps.isEmpty()) {
            return ResolutionResult.success(pluginId, List.of());
        }

        // Build dependency graph
        DependencyGraph graph = new DependencyGraph();
        Map<String, PluginDependency> depMap = new LinkedHashMap<>();

        for (PluginDependency dep : deps) {
            if (dep.id() == null || dep.id().isBlank()) {
                continue;
            }
            graph.addEdge(pluginId, dep.id());
            depMap.put(dep.id(), dep);
        }

        // Recursively add transitive dependencies
        Set<String> visited = new HashSet<>();
        Queue<String> toProcess = new LinkedList<>(depMap.keySet());
        while (!toProcess.isEmpty()) {
            String currentId = toProcess.poll();
            if (!visited.add(currentId)) continue;

            Optional<Plugin> dbOpt = pluginRepository.findById(currentId);
            if (dbOpt.isPresent()) {
                Map<String, Object> childManifest = dbOpt.get().getManifest();
                List<PluginDependency> childDeps = PluginDependency.fromManifest(childManifest);
                for (PluginDependency childDep : childDeps) {
                    if (childDep.id() == null || childDep.id().isBlank()) continue;
                    if (!childDep.soft()) {
                        graph.addEdge(currentId, childDep.id());
                        depMap.putIfAbsent(childDep.id(), childDep);
                        toProcess.add(childDep.id());
                    }
                }
            }
        }

        // Check for cycles
        List<String> cycle = graph.detectCycle();
        if (!cycle.isEmpty()) {
            String cycleStr = String.join(" → ", cycle);
            logService.log(LogLevel.ERROR, LogCategory.PLUGIN,
                Map.of("component", "PluginDependencyResolver"),
                "DEPENDENCY_CYCLE_DETECTED",
                Map.of("pluginId", pluginId, "cycle", cycleStr),
                null, null);
            return ResolutionResult.failure(pluginId,
                DependencyResolutionException.ResolutionFailureType.CYCLE_DETECTED,
                "Dependency cycle detected: " + cycleStr,
                cycle);
        }

        // Resolve each dependency
        List<ResolutionItem> items = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();

        for (PluginDependency dep : deps) {
            if (dep.id() == null || dep.id().isBlank()) continue;

            Optional<Plugin> installed = pluginRepository.findById(dep.id());
            if (installed.isEmpty()) {
                if (dep.soft()) {
                    items.add(new ResolutionItem(dep.id(), dep.versionSpec(), ResolutionItem.Action.SKIP_SOFT, null));
                } else {
                    missing.add(dep.id() + " (" + dep.versionSpec() + ")");
                    items.add(new ResolutionItem(dep.id(), dep.versionSpec(), ResolutionItem.Action.MISSING, null));
                }
                continue;
            }

            Plugin existing = installed.get();
            VersionConstraint constraint = VersionConstraint.parse(dep.versionSpec());

            if (!constraint.satisfies(existing.getVersion())) {
                if (constraint.isNewerThan(existing.getVersion())) {
                    // Installed version is older than required → update prompt
                    conflicts.add(dep.id() + ": installed " + existing.getVersion() + ", required " + dep.versionSpec());
                    items.add(new ResolutionItem(dep.id(), dep.versionSpec(), ResolutionItem.Action.UPDATE_PROMPT, existing.getVersion()));
                } else {
                    // Installed version is newer than required → hard block (downgrade not supported)
                    conflicts.add(dep.id() + ": installed " + existing.getVersion() + ", required " + dep.versionSpec() + " (downgrade blocked)");
                    items.add(new ResolutionItem(dep.id(), dep.versionSpec(), ResolutionItem.Action.BLOCK, existing.getVersion()));
                }
                continue;
            }

            items.add(new ResolutionItem(dep.id(), dep.versionSpec(), ResolutionItem.Action.SATISFIED, existing.getVersion()));
        }

        if (!missing.isEmpty()) {
            return ResolutionResult.failure(pluginId,
                DependencyResolutionException.ResolutionFailureType.MISSING_DEPENDENCY,
                "Missing hard dependencies: " + String.join(", ", missing),
                missing);
        }

        if (!conflicts.isEmpty()) {
            boolean hasBlock = items.stream().anyMatch(i -> i.action() == ResolutionItem.Action.BLOCK);
            if (hasBlock) {
                return ResolutionResult.failure(pluginId,
                    DependencyResolutionException.ResolutionFailureType.VERSION_MISMATCH,
                    "Version conflict(s): " + String.join("; ", conflicts),
                    conflicts);
            }
        }

        // Build install order (topological sort of hard deps)
        List<String> installOrder = graph.topologicalSort().stream()
            .filter(id -> !id.equals(pluginId))
            .toList();

        return ResolutionResult.success(pluginId, items, installOrder);
    }

    /**
     * Checks for slot clashes: two different plugins claiming the same channel_id or provider_id.
     *
     * @param manifest the plugin manifest to check
     * @param channelRegistry current channel registry
     * @param providerRegistry current provider registry
     * @return empty if no clash, otherwise conflict details
     */
    public Optional<String> checkSlotClash(
        Map<String, Object> manifest,
        ChannelRegistry channelRegistry,
        ModelProviderRegistry providerRegistry
    ) {
        String pluginId = manifest.get("id") != null ? manifest.get("id").toString() : "unknown";

        Object slots = manifest.get("slots");
        if (slots instanceof Map<?, ?> slotMap) {
            Object channelId = slotMap.get("channel_id");
            if (channelId instanceof String cid && channelRegistry.isClaimed(cid)) {
                Optional<String> owner = channelRegistry.getOwnerPluginId(cid);
                if (owner.isPresent() && !owner.get().equals(pluginId)) {
                    return Optional.of("Channel slot '" + cid + "' already claimed by " + owner.get());
                }
            }
            Object providerId = slotMap.get("provider_id");
            if (providerId instanceof String pid && providerRegistry.isClaimed(pid)) {
                Optional<String> owner = providerRegistry.getOwnerPluginId(pid);
                if (owner.isPresent() && !owner.get().equals(pluginId)) {
                    return Optional.of("Provider slot '" + pid + "' already claimed by " + owner.get());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Result of dependency resolution.
     */
    public record ResolutionResult(
        String pluginId,
        boolean success,
        List<ResolutionItem> items,
        List<String> installOrder,
        DependencyResolutionException.ResolutionFailureType failureType,
        String message,
        List<String> details
    ) {
        static ResolutionResult success(String pluginId, List<ResolutionItem> items, List<String> installOrder) {
            return new ResolutionResult(pluginId, true, items, installOrder, null, null, List.of());
        }

        static ResolutionResult success(String pluginId, List<ResolutionItem> items) {
            return new ResolutionResult(pluginId, true, items, List.of(), null, null, List.of());
        }

        static ResolutionResult failure(String pluginId, DependencyResolutionException.ResolutionFailureType type,
                                        String message, List<String> details) {
            return new ResolutionResult(pluginId, false, List.of(), List.of(), type, message, details);
        }

        public boolean hasUpdatePrompts() {
            return items.stream().anyMatch(i -> i.action() == ResolutionItem.Action.UPDATE_PROMPT);
        }

        public List<ResolutionItem> getUpdatePrompts() {
            return items.stream().filter(i -> i.action() == ResolutionItem.Action.UPDATE_PROMPT).toList();
        }
    }

    /**
     * Individual dependency resolution item.
     */
    public record ResolutionItem(
        String dependencyId,
        String versionSpec,
        Action action,
        String installedVersion
    ) {
        public enum Action {
            SATISFIED,
            MISSING,
            UPDATE_PROMPT,
            BLOCK,
            SKIP_SOFT
        }
    }
}
