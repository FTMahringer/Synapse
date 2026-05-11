package dev.synapse.tools.builtin;

import dev.synapse.tools.NativeJavaTool;
import dev.synapse.tools.ToolExecutionContext;
import dev.synapse.tools.ToolExecutionResult;
import dev.synapse.tools.ToolInputValidator;
import dev.synapse.tools.ToolRegistryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ToolRegistryInspectTool implements NativeJavaTool {

    private final ToolRegistryService toolRegistryService;

    public ToolRegistryInspectTool(@Lazy ToolRegistryService toolRegistryService) {
        this.toolRegistryService = toolRegistryService;
    }

    @Override
    public String toolId() {
        return "tool_registry_inspect";
    }

    @Override
    public String displayName() {
        return "Tool Registry Inspect";
    }

    @Override
    public String description() {
        return "Inspect registered native Java tools and metadata";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "toolId", Map.of("type", "string", "description", "optional specific tool id"),
                "includeSchema", Map.of("type", "boolean", "description", "include full input schema")
            )
        );
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public long cacheTtlSeconds() {
        return 120;
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> input) {
        String toolId = ToolInputValidator.optionalString(input, "toolId");
        boolean includeSchema = ToolInputValidator.optionalBoolean(input, "includeSchema", true);

        List<Map<String, Object>> tools = toolRegistryService.listToolDefinitions().stream()
            .filter(definition -> toolId == null || definition.toolId().equals(toolId))
            .map(definition -> {
                java.util.HashMap<String, Object> item = new java.util.HashMap<>();
                item.put("toolId", definition.toolId());
                item.put("displayName", definition.displayName());
                item.put("description", definition.description());
                item.put("cacheable", definition.cacheable());
                item.put("defaultTtlSeconds", definition.defaultTtlSeconds());
                if (includeSchema) {
                    item.put("inputSchema", definition.inputSchema());
                }
                return Map.copyOf(item);
            })
            .toList();

        return ToolExecutionResult.of(
            Map.of(
                "toolCount", tools.size(),
                "tools", tools
            )
        );
    }
}
