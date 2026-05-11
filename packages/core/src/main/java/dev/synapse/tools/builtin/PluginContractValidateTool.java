package dev.synapse.tools.builtin;

import dev.synapse.plugins.ManifestValidator;
import dev.synapse.plugins.PluginManifest;
import dev.synapse.plugins.PluginSafetyPolicy;
import dev.synapse.plugins.PluginSafetyService;
import dev.synapse.plugins.ValidationResult;
import dev.synapse.tools.NativeJavaTool;
import dev.synapse.tools.ToolExecutionContext;
import dev.synapse.tools.ToolExecutionResult;
import dev.synapse.tools.ToolInputValidator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PluginContractValidateTool implements NativeJavaTool {

    private final ManifestValidator manifestValidator;
    private final PluginSafetyService pluginSafetyService;

    public PluginContractValidateTool(
        ManifestValidator manifestValidator,
        PluginSafetyService pluginSafetyService
    ) {
        this.manifestValidator = manifestValidator;
        this.pluginSafetyService = pluginSafetyService;
    }

    @Override
    public String toolId() {
        return "plugin_contract_validate";
    }

    @Override
    public String displayName() {
        return "Plugin Contract Validate";
    }

    @Override
    public String description() {
        return "Validate plugin manifest contract and trust policy compatibility";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
            "type", "object",
            "required", java.util.List.of("manifest"),
            "properties", Map.of(
                "manifest", Map.of("type", "object", "description", "raw plugin manifest map")
            )
        );
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public long cacheTtlSeconds() {
        return 300;
    }

    @Override
    public void validateInput(Map<String, Object> input) {
        ToolInputValidator.requireObject(input, "manifest");
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> input) {
        Map<String, Object> manifestRaw = ToolInputValidator.requireObject(input, "manifest");

        PluginManifest manifest = PluginManifest.fromMap(manifestRaw);
        ValidationResult validationResult = manifestValidator.validate(manifest);
        PluginSafetyPolicy safetyPolicy = pluginSafetyService.assess(manifestRaw);

        HashMap<String, Object> normalizedManifest = new HashMap<>();
        if (manifest.id() != null) normalizedManifest.put("id", manifest.id());
        if (manifest.name() != null) normalizedManifest.put("name", manifest.name());
        if (manifest.type() != null) normalizedManifest.put("type", manifest.type().name());
        if (manifest.version() != null) normalizedManifest.put("version", manifest.version());
        if (manifest.author() != null) normalizedManifest.put("author", manifest.author());
        if (manifest.license() != null) normalizedManifest.put("license", manifest.license());
        if (manifest.minSynapse() != null) normalizedManifest.put("minSynapse", manifest.minSynapse());
        normalizedManifest.put("tags", manifest.tags());

        return ToolExecutionResult.of(
            Map.of(
                "valid", validationResult.valid(),
                "errors", validationResult.errors(),
                "manifest", normalizedManifest,
                "safety", Map.of(
                    "trustLevel", safetyPolicy.trustLevel().name(),
                    "requiresConfirmation", safetyPolicy.requiresConfirmation(),
                    "warnings", safetyPolicy.warnings()
                )
            )
        );
    }
}
