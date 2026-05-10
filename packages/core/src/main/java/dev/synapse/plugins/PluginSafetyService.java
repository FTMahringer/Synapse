package dev.synapse.plugins;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Evaluates plugin safety before install.
 * Blocks community/unverified installs without explicit operator confirmation.
 */
@Service
public class PluginSafetyService {

    private static final Set<String> VERIFIED_SOURCES = Set.of("official", "acp");
    private static final Set<String> COMMUNITY_SOURCES = Set.of("community", "skills_sh");

    private final PluginLifecycleService lifecycleService;
    private final SystemLogService logService;

    public PluginSafetyService(PluginLifecycleService lifecycleService, SystemLogService logService) {
        this.lifecycleService = lifecycleService;
        this.logService = logService;
    }

    /**
     * Assess trust level for a manifest by its source field.
     */
    public PluginSafetyPolicy assess(Map<String, Object> manifest) {
        Object sourceObj = manifest.get("source");
        String source = sourceObj != null ? sourceObj.toString() : "";

        if (VERIFIED_SOURCES.contains(source)) {
            return PluginSafetyPolicy.verified();
        }
        if (COMMUNITY_SOURCES.contains(source)) {
            return PluginSafetyPolicy.community(source);
        }
        return PluginSafetyPolicy.unverified(source.isBlank() ? "unknown" : source);
    }

    /**
     * Install with safety check. Community/unverified require confirmed=true.
     */
    public Plugin safeInstall(Map<String, Object> manifest, boolean confirmed) {
        PluginSafetyPolicy policy = assess(manifest);

        if (policy.requiresConfirmation() && !confirmed) {
            throw new IllegalStateException(
                "Confirmation required for " + policy.trustLevel() + " plugin. Warnings: " +
                String.join(" | ", policy.warnings())
            );
        }

        String source = manifest.getOrDefault("source", "unknown").toString();

        logService.log(
            LogLevel.INFO,
            LogCategory.PLUGIN,
            Map.of("component", "PluginSafetyService"),
            "PLUGIN_SAFETY_ASSESSED",
            Map.of(
                "pluginId", manifest.getOrDefault("id", "unknown").toString(),
                "trustLevel", policy.trustLevel().name(),
                "source", source,
                "confirmed", String.valueOf(confirmed)
            ),
            null, null
        );

        return lifecycleService.install(manifest);
    }
}
