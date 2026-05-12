package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.plugins.PluginLifecycleService;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for plugin sandbox operations.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/plugins/sandbox/scan} — scan a JAR for forbidden references</li>
 *   <li>{@code GET /api/plugins/{id}/sandbox/limits} — get resource limits for a plugin</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/plugins")
public class PluginSandboxController {

    private final PluginSandboxService sandboxService;
    private final PluginLifecycleService lifecycleService;

    public PluginSandboxController(
        PluginSandboxService sandboxService,
        PluginLifecycleService lifecycleService
    ) {
        this.sandboxService = sandboxService;
        this.lifecycleService = lifecycleService;
    }

    /**
     * Scans a plugin JAR for forbidden bytecode references.
     */
    @PostMapping("/sandbox/scan")
    public Map<String, Object> scanJar(@RequestBody Map<String, String> body) {
        String jarPath = body.get("jarPath");
        if (jarPath == null || jarPath.isBlank()) {
            throw new IllegalArgumentException("jarPath is required");
        }

        BytecodeScanner.ScanResult result = sandboxService.scanJar(Path.of(jarPath));
        return Map.of(
            "clean", result.clean(),
            "violations", result.violations().stream().map(v -> Map.of(
                "classFile", v.classFile(),
                "reference", v.reference(),
                "type", v.type().name()
            )).toList()
        );
    }

    /**
     * Returns resource limits for a plugin.
     */
    @GetMapping("/{id}/sandbox/limits")
    public Map<String, Object> getLimits(@PathVariable String id) {
        Plugin plugin = lifecycleService.findById(id);
        return Map.of(
            "pluginId", id,
            "trustTier", plugin.getTrustTier().name(),
            "lifecycleTimeoutMs", sandboxService.getLifecycleTimeoutMs(plugin),
            "messageHandlerTimeoutMs", sandboxService.getMessageHandlerTimeoutMs(plugin),
            "maxLogsPerMinute", sandboxService.getMaxLogsPerMinute(plugin)
        );
    }
}
