package dev.synapse.tools;

import dev.synapse.core.infrastructure.exception.ApiException;

public class ToolExecutionException extends ApiException {
    public ToolExecutionException(String message) {
        super("TOOL_EXECUTION_ERROR", message, 500);
    }
}
