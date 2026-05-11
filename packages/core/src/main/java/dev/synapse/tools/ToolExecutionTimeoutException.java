package dev.synapse.tools;

import dev.synapse.core.infrastructure.exception.ApiException;

public class ToolExecutionTimeoutException extends ApiException {
    public ToolExecutionTimeoutException(String message) {
        super("TOOL_EXECUTION_TIMEOUT", message, 408);
    }
}
