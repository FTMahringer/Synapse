package dev.synapse.tools;

import java.util.Map;

/**
 * Contract for native Java tools executed inside SYNAPSE runtime.
 * New tools should implement this interface and be discoverable via Spring.
 */
public interface NativeJavaTool {

    String toolId();

    String displayName();

    String description();

    Map<String, Object> inputSchema();

    default boolean isCacheable() {
        return false;
    }

    default long cacheTtlSeconds() {
        return 0;
    }

    default void validateInput(Map<String, Object> input) {
        // No-op by default
    }

    ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> input);
}
