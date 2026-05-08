package dev.synapse.core.agents;

import dev.synapse.core.config.SynapseProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class AgentDefinitionLoader {

    private static final List<String> KNOWN_FILES = List.of("identity.md", "soul.md", "connections.md", "config.yml", "system-prompt.md", "firm.yml");

    private final Path agentsRoot;

    public AgentDefinitionLoader(SynapseProperties properties) {
        this.agentsRoot = Path.of(properties.agentsPath()).toAbsolutePath().normalize();
    }

    public List<AgentDefinition> listAgents() {
        if (!Files.isDirectory(agentsRoot)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(agentsRoot, 3)) {
            return paths
                    .filter(Files::isDirectory)
                    .filter(this::looksLikeAgentDirectory)
                    .map(this::load)
                    .sorted(Comparator.comparing(AgentDefinition::id))
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read agents directory: " + agentsRoot, ex);
        }
    }

    private boolean looksLikeAgentDirectory(Path path) {
        return KNOWN_FILES.stream().anyMatch(file -> Files.isRegularFile(path.resolve(file)));
    }

    private AgentDefinition load(Path path) {
        Map<String, String> metadata = readFrontmatter(path.resolve("identity.md"));
        String directoryName = agentsRoot.relativize(path).toString().replace('\\', '/');
        String id = metadata.getOrDefault("id", directoryName);
        String name = metadata.getOrDefault("name", id);
        String type = metadata.getOrDefault("type", "custom");
        List<String> files = KNOWN_FILES.stream()
                .filter(file -> Files.isRegularFile(path.resolve(file)))
                .toList();

        return new AgentDefinition(id, name, type, directoryName, files, metadata);
    }

    private Map<String, String> readFrontmatter(Path file) {
        if (!Files.isRegularFile(file)) {
            return Map.of();
        }

        try {
            List<String> lines = Files.readAllLines(file);
            if (lines.isEmpty() || !lines.getFirst().equals("---")) {
                return Map.of();
            }

            return lines.stream()
                    .skip(1)
                    .takeWhile(line -> !line.equals("---"))
                    .map(line -> line.split(":", 2))
                    .filter(parts -> parts.length == 2)
                    .collect(java.util.stream.Collectors.toMap(
                            parts -> parts[0].trim(),
                            parts -> parts[1].trim().replace("\"", ""),
                            (left, right) -> left
                    ));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read agent metadata: " + file, ex);
        }
    }
}
