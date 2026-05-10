package dev.synapse.plugins;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.domain.StoreEntry;
import dev.synapse.core.common.repository.StoreEntryRepository;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.common.repository.PluginRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates and installs plugin bundles from store entries.
 */
@Service
public class BundleInstallService {

    private final StoreEntryRepository storeEntryRepository;
    private final PluginLifecycleService lifecycleService;
    private final PluginRepository pluginRepository;
    private final SystemLogService logService;

    public BundleInstallService(
        StoreEntryRepository storeEntryRepository,
        PluginLifecycleService lifecycleService,
        PluginRepository pluginRepository,
        SystemLogService logService
    ) {
        this.storeEntryRepository = storeEntryRepository;
        this.lifecycleService = lifecycleService;
        this.pluginRepository = pluginRepository;
        this.logService = logService;
    }

    /**
     * Validates a bundle: checks it exists, is type BUNDLE, and all referenced plugins exist.
     */
    public ValidationResult validateBundle(String bundleId) {
        StoreEntry bundle = storeEntryRepository.findById(bundleId).orElse(null);
        if (bundle == null) {
            return ValidationResult.fail(List.of("Bundle not found: " + bundleId));
        }
        if (bundle.getType() != StoreEntry.StoreEntryType.BUNDLE) {
            return ValidationResult.fail(List.of("Store entry is not a bundle: " + bundleId));
        }

        List<String> errors = new ArrayList<>();
        List<String> pluginIds = extractPluginIds(bundle);
        for (String pluginId : pluginIds) {
            if (storeEntryRepository.findById(pluginId).isEmpty()) {
                errors.add("Bundle references unknown plugin: " + pluginId);
            }
        }

        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    /**
     * Installs all plugins in a bundle. Skips already-installed plugins.
     * Returns list of installed plugin IDs.
     */
    @Transactional
    @CacheEvict(value = "plugin-metadata", allEntries = true)
    public BundleInstallResult installBundle(String bundleId) {
        ValidationResult validation = validateBundle(bundleId);
        if (!validation.valid()) {
            return new BundleInstallResult(false, List.of(), validation.errors());
        }

        StoreEntry bundle = storeEntryRepository.findById(bundleId)
            .orElseThrow(() -> new ResourceNotFoundException("Bundle", bundleId));

        List<String> installed = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (String pluginId : extractPluginIds(bundle)) {
            StoreEntry entry = storeEntryRepository.findById(pluginId).orElse(null);
            if (entry == null) continue;

            if (pluginRepository.existsById(pluginId)) {
                skipped.add(pluginId);
                continue;
            }

            Map<String, Object> manifest = Map.of(
                "id", pluginId,
                "name", entry.getName(),
                "type", entry.getType() == StoreEntry.StoreEntryType.BUNDLE ? "SKILL" : guessType(entry),
                "version", entry.getVersion(),
                "author", entry.getAuthor() != null ? entry.getAuthor() : "unknown",
                "source", entry.getSource()
            );

            lifecycleService.install(manifest);
            installed.add(pluginId);
        }

        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "BundleInstallService"),
            "BUNDLE_INSTALLED",
            Map.of("bundleId", bundleId, "installed", installed.size(), "skipped", skipped.size()),
            null, null);

        return new BundleInstallResult(true, installed, List.of());
    }

    @SuppressWarnings("unchecked")
    private List<String> extractPluginIds(StoreEntry bundle) {
        Object meta = bundle.getMeta();
        if (!(meta instanceof Map<?, ?> map)) return List.of();
        Object plugins = map.get("plugins");
        if (!(plugins instanceof List<?> list)) return List.of();

        List<String> ids = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> pluginMap) {
                Object id = pluginMap.get("id");
                if (id != null) ids.add(id.toString());
            } else if (item instanceof String s) {
                ids.add(s);
            }
        }
        return ids;
    }

    private String guessType(StoreEntry entry) {
        if (entry.getTags() != null) {
            List<String> tags = entry.getTags();
            if (tags.contains("channel")) return "CHANNEL";
            if (tags.contains("model") || tags.contains("llm")) return "MODEL";
            if (tags.contains("mcp")) return "MCP";
        }
        return "SKILL";
    }

    public record BundleInstallResult(boolean success, List<String> installed, List<String> errors) {}
}
