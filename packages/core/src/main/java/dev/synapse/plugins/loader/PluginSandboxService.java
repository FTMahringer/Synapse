package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.repository.PluginRepository;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Plugin sandboxing and security enforcement service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>ASM bytecode scan at install time — reject forbidden refs</li>
 *   <li>Validate JPMS isolation at load time</li>
 *   <li>Enforce resource limits: thread count, timeouts, log volume</li>
 *   <li>Trust tier defaults: stricter for Community, relaxed for Official</li>
 *   <li>Mark plugin ERROR + disable on lifecycle hook timeout</li>
 * </ul>
 */
@Service
public class PluginSandboxService {

    private final PluginRepository pluginRepository;
    private final SystemLogService logService;
    private final BytecodeScanner bytecodeScanner;

    public PluginSandboxService(
        PluginRepository pluginRepository,
        SystemLogService logService
    ) {
        this.pluginRepository = pluginRepository;
        this.logService = logService;
        this.bytecodeScanner = new BytecodeScanner();
    }

    /**
     * Scans a plugin JAR for forbidden bytecode references.
     *
     * @param jarPath path to the plugin JAR
     * @return scan result
     */
    public BytecodeScanner.ScanResult scanJar(Path jarPath) {
        try {
            return bytecodeScanner.scan(jarPath);
        } catch (IOException e) {
            logService.log(LogLevel.ERROR, LogCategory.PLUGIN,
                Map.of("component", "PluginSandboxService"),
                "SANDBOX_SCAN_FAILED",
                Map.of("jar", jarPath.toString(), "error", e.getMessage()),
                null, null);
            return new BytecodeScanner.ScanResult(false, List.of(
                new BytecodeScanner.Violation(
                    "JAR",
                    "Could not read JAR: " + e.getMessage(),
                    BytecodeScanner.ViolationType.CLASS_REFERENCE
                )
            ));
        }
    }

    /**
     * Validates JPMS isolation: confirms plugin module cannot resolve core classes.
     *
     * @param loaded the loaded plugin record
     * @return true if isolation is valid
     */
    public boolean validateJpmsIsolation(LoadedPlugin loaded) {
        java.lang.ModuleLayer layer = loaded.moduleLayer();
        if (layer == null) {
            return true; // Non-JPMS plugins skip this check
        }

        // Check that no core modules are readable by the plugin layer
        for (java.lang.Module module : layer.modules()) {
            for (java.lang.Module readable : module.getLayer().configuration().modules()) {
                String name = readable.getName();
                if (name != null && (
                    name.startsWith("dev.synapse.core") ||
                    name.startsWith("org.springframework") ||
                    name.startsWith("org.hibernate") ||
                    name.startsWith("jakarta.persistence")
                )) {
                    logService.log(LogLevel.ERROR, LogCategory.PLUGIN,
                        Map.of("component", "PluginSandboxService"),
                        "SANDBOX_JPMS_VIOLATION",
                        Map.of("pluginId", loaded.pluginId(),
                               "module", name),
                        null, null);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Executes a plugin lifecycle hook with a timeout.
     *
     * @param pluginId the plugin id
     * @param hook the hook to execute (onLoad or onUnload)
     * @param isOnLoad true for onLoad, false for onUnload
     * @param dbPlugin the plugin entity for trust tier lookup
     * @return true if the hook completed within timeout
     */
    public boolean runLifecycleHookWithTimeout(
        String pluginId,
        Runnable hook,
        boolean isOnLoad,
        Plugin dbPlugin
    ) {
        long timeoutMs = getLifecycleTimeoutMs(dbPlugin);
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "plugin-hook-" + pluginId);
            t.setDaemon(true);
            return t;
        });

        Future<?> future = executor.submit(hook);
        boolean completed = false;
        try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
            completed = true;
        } catch (TimeoutException e) {
            future.cancel(true);
            logService.log(LogLevel.ERROR, LogCategory.PLUGIN,
                Map.of("component", "PluginSandboxService"),
                isOnLoad ? "SANDBOX_ONLOAD_TIMEOUT" : "SANDBOX_ONUNLOAD_TIMEOUT",
                Map.of("pluginId", pluginId, "timeoutMs", timeoutMs),
                null, null);

            if (isOnLoad) {
                markPluginError(pluginId,
                    "Lifecycle hook onLoad() timed out after " + timeoutMs + "ms");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            markPluginError(pluginId, "Lifecycle hook interrupted");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            logService.log(LogLevel.ERROR, LogCategory.PLUGIN,
                Map.of("component", "PluginSandboxService"),
                isOnLoad ? "SANDBOX_ONLOAD_ERROR" : "SANDBOX_ONUNLOAD_ERROR",
                Map.of("pluginId", pluginId, "error", cause.getMessage()),
                null, null);

            if (isOnLoad) {
                markPluginError(pluginId,
                    "Lifecycle hook onLoad() failed: " + cause.getMessage());
            }
        } finally {
            executor.shutdownNow();
        }

        return completed;
    }

    /**
     * Returns the lifecycle hook timeout based on trust tier.
     */
    public long getLifecycleTimeoutMs(Plugin dbPlugin) {
        return dbPlugin.getTrustTier() == Plugin.TrustTier.OFFICIAL
            ? 30_000L : 10_000L;
    }

    /**
     * Returns the message handler timeout based on trust tier.
     */
    public long getMessageHandlerTimeoutMs(Plugin dbPlugin) {
        return dbPlugin.getTrustTier() == Plugin.TrustTier.OFFICIAL
            ? 60_000L : 30_000L;
    }

    /**
     * Returns the maximum log entries per minute based on trust tier.
     */
    public int getMaxLogsPerMinute(Plugin dbPlugin) {
        return dbPlugin.getTrustTier() == Plugin.TrustTier.OFFICIAL
            ? 1000 : 300;
    }

    private void markPluginError(String pluginId, String message) {
        pluginRepository.findById(pluginId).ifPresent(p -> {
            p.setLoaderState(Plugin.LoaderState.ERROR);
            p.setErrorMessage(message);
            p.setStatus(Plugin.PluginStatus.ERROR);
            pluginRepository.save(p);
        });
    }
}
