package dev.synapse.core.tools;

import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.tools.NativeJavaTool;
import dev.synapse.tools.ToolExecutionContext;
import dev.synapse.tools.ToolExecutionResult;
import dev.synapse.tools.ToolRegistryService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToolRegistryServiceTest {

    @Test
    void constructor_rejectsDuplicateToolIds() {
        NativeJavaTool first = simpleTool("duplicate-tool");
        NativeJavaTool second = simpleTool("duplicate-tool");

        assertThrows(
            ValidationException.class,
            () -> new ToolRegistryService(List.of(first, second), true)
        );
    }

    @Test
    void listToolDefinitions_returnsEmptyWhenDisabled() {
        ToolRegistryService service = new ToolRegistryService(List.of(simpleTool("tool-a")), false);
        assertEquals(0, service.listToolDefinitions().size());
    }

    private NativeJavaTool simpleTool(String id) {
        return new NativeJavaTool() {
            @Override
            public String toolId() {
                return id;
            }

            @Override
            public String displayName() {
                return id;
            }

            @Override
            public String description() {
                return "test";
            }

            @Override
            public Map<String, Object> inputSchema() {
                return Map.of();
            }

            @Override
            public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> input) {
                return ToolExecutionResult.of(Map.of());
            }
        };
    }
}
