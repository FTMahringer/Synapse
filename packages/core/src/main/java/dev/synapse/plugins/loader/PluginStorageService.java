package dev.synapse.plugins.loader;

import dev.synapse.core.infrastructure.config.SynapseProperties;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Manages plugin storage directories: system/ (persisted) and staging/ (runtime).
 *
 * <p>Storage layout under {@code $SYNAPSE_HOME/plugins/}:
 * <pre>
 * plugins/
 *   ├── system/     ← persisted across restarts
 *   └── staging/    ← runtime-installed this session
 * </pre>
 */
@Service
public class PluginStorageService {

    private final SystemLogService logService;
    private final Path pluginsHome;
    private final Path systemDir;
    private final Path stagingDir;

    public PluginStorageService(
        SynapseProperties properties,
        SystemLogService logService
    ) {
        this.logService = logService;
        String synapseHome = System.getenv().getOrDefault(
            "SYNAPSE_HOME",
            System.getProperty("user.home") + "/.synapse"
        );
        this.pluginsHome = Path.of(synapseHome, "plugins");
        this.systemDir = pluginsHome.resolve("system");
        this.stagingDir = pluginsHome.resolve("staging");
    }

    @PostConstruct
    public void init() {
        boolean dirsCreated = false;
        try {
            Files.createDirectories(systemDir);
            Files.createDirectories(stagingDir);
            dirsCreated = true;
        } catch (Exception e) {
            // Log but don't fail startup — plugins can still be managed in-memory
            // and directories will be retried on first actual storage operation
            System.err.println(
                "[PluginStorageService] WARNING: Could not create plugin storage directories at " +
                    pluginsHome +
                    ": " +
                    e.getMessage()
            );
        }

        if (dirsCreated) {
            logService.log(
                LogLevel.INFO,
                LogCategory.PLUGIN,
                Map.of("component", "PluginStorageService"),
                "PLUGIN_STORAGE_INIT",
                Map.of(
                    "systemDir",
                    systemDir.toString(),
                    "stagingDir",
                    stagingDir.toString()
                ),
                null,
                null
            );
        }
    }

    /** Returns the system/ directory path. */
    public Path getSystemDir() {
        return systemDir;
    }

    /** Returns the staging/ directory path. */
    public Path getStagingDir() {
        return stagingDir;
    }

    /** Lists all JAR files in the system directory. */
    public List<Path> listSystemJars() {
        return listJars(systemDir);
    }

    /** Lists all JAR files in the staging directory. */
    public List<Path> listStagingJars() {
        return listJars(stagingDir);
    }

    /** Moves a JAR from staging/ to system/ (idempotent). */
    public Path promoteToSystem(String jarName) throws IOException {
        if (!isValidJarName(jarName)) {
            throw new IllegalArgumentException("Invalid JAR name: " + jarName);
        }
        Path source = stagingDir.resolve(jarName);
        Path target = systemDir.resolve(jarName);
        if (Files.exists(source)) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            logService.log(
                LogLevel.INFO,
                LogCategory.PLUGIN,
                Map.of("component", "PluginStorageService"),
                "PLUGIN_PROMOTED",
                Map.of("jar", jarName),
                null,
                null
            );
        }
        return target;
    }

    /** Moves all JARs from staging/ to system/ (graceful shutdown). */
    public void promoteAllStaging() {
        List<Path> stagingJars = listStagingJars();
        for (Path jar : stagingJars) {
            try {
                promoteToSystem(jar.getFileName().toString());
            } catch (IOException e) {
                logService.log(
                    LogLevel.ERROR,
                    LogCategory.PLUGIN,
                    Map.of("component", "PluginStorageService"),
                    "PLUGIN_PROMOTE_FAILED",
                    Map.of("jar", jar.toString(), "error", e.getMessage()),
                    null,
                    null
                );
            }
        }
    }

    /** Copies a JAR into the staging directory. */
    public Path stageJar(Path sourceJar) throws IOException {
        String fileName = sourceJar.getFileName().toString();
        if (!isValidJarName(fileName)) {
            throw new IllegalArgumentException("Invalid JAR name: " + fileName);
        }
        // Resolve target within stagingDir and normalize to prevent path traversal
        Path target = stagingDir.resolve(fileName).normalize();
        if (!target.startsWith(stagingDir.normalize())) {
            throw new IllegalArgumentException(
                "Path traversal detected: " + fileName
            );
        }
        Files.copy(sourceJar, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    /** Deletes a JAR from both system/ and staging/. */
    public void deleteJar(String jarName) {
        if (!isValidJarName(jarName)) {
            logService.log(
                LogLevel.WARN,
                LogCategory.PLUGIN,
                Map.of("component", "PluginStorageService"),
                "PLUGIN_DELETE_INVALID_NAME",
                Map.of("jar", jarName),
                null,
                null
            );
            return;
        }
        try {
            Files.deleteIfExists(systemDir.resolve(jarName));
            Files.deleteIfExists(stagingDir.resolve(jarName));
        } catch (IOException e) {
            logService.log(
                LogLevel.WARN,
                LogCategory.PLUGIN,
                Map.of("component", "PluginStorageService"),
                "PLUGIN_DELETE_FAILED",
                Map.of("jar", jarName, "error", e.getMessage()),
                null,
                null
            );
        }
    }

    /**
     * Validates that a JAR name only contains safe characters.
     * Rejects path traversal attempts (.., /, \) and null bytes.
     */
    private boolean isValidJarName(String jarName) {
        if (jarName == null || jarName.isBlank()) {
            return false;
        }
        // Only allow alphanumeric, dash, underscore, dot
        return (
            jarName.matches("^[a-zA-Z0-9._-]+\\.jar$") &&
            !jarName.contains("..") &&
            !jarName.contains("/") &&
            !jarName.contains("\\")
        );
    }

    /** Checks if there are orphaned JARs in staging/ (crash recovery). */
    public boolean hasOrphanedStagingJars() {
        return !listStagingJars().isEmpty();
    }

    private List<Path> listJars(Path dir) {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(p -> p.toString().endsWith(".jar")).toList();
        } catch (IOException e) {
            return List.of();
        }
    }
}
