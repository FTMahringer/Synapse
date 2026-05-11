package dev.synapse.tools;

import java.util.Map;

public record ToolExecutionResult(
    Map<String, Object> result
) {
    public static ToolExecutionResult of(Map<String, Object> result) {
        return new ToolExecutionResult(result != null ? result : Map.of());
    }
}
