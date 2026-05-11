package dev.synapse.tools;

import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolRegistryService {

    private final Map<String, NativeJavaTool> toolsById;
    private final boolean toolsEnabled;

    public ToolRegistryService(
        List<NativeJavaTool> tools,
        @Value("${synapse.tools.enabled:true}") boolean toolsEnabled
    ) {
        this.toolsEnabled = toolsEnabled;
        Map<String, NativeJavaTool> index = new LinkedHashMap<>();
        for (NativeJavaTool tool : tools) {
            if (tool.toolId() == null || tool.toolId().isBlank()) {
                throw new ValidationException("Native Java tool id must not be blank");
            }
            if (index.containsKey(tool.toolId())) {
                throw new ValidationException("Duplicate native Java tool id: " + tool.toolId());
            }
            index.put(tool.toolId(), tool);
        }
        this.toolsById = Map.copyOf(index);
    }

    public List<ToolDefinition> listToolDefinitions() {
        if (!toolsEnabled) {
            return List.of();
        }
        return toolsById.values().stream()
            .map(this::toDefinition)
            .toList();
    }

    public ToolDefinition getDefinition(String toolId) {
        return toDefinition(getTool(toolId));
    }

    public NativeJavaTool getTool(String toolId) {
        if (!toolsEnabled) {
            throw new ValidationException("Native Java tools are disabled");
        }
        NativeJavaTool tool = toolsById.get(toolId);
        if (tool == null) {
            throw new ResourceNotFoundException("NativeJavaTool", toolId);
        }
        return tool;
    }

    private ToolDefinition toDefinition(NativeJavaTool tool) {
        return new ToolDefinition(
            tool.toolId(),
            tool.displayName(),
            tool.description(),
            tool.inputSchema(),
            tool.isCacheable(),
            tool.isCacheable() ? tool.cacheTtlSeconds() : 0
        );
    }
}
