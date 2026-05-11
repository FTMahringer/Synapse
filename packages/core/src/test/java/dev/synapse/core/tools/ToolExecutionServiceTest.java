package dev.synapse.core.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.agents.service.AgentHardeningPolicyService;
import dev.synapse.agents.service.HardeningDecision;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.tools.NativeJavaTool;
import dev.synapse.tools.ToolExecutionContext;
import dev.synapse.tools.ToolExecutionResponse;
import dev.synapse.tools.ToolExecutionResult;
import dev.synapse.tools.ToolExecutionService;
import dev.synapse.tools.ToolRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolExecutionServiceTest {

    @Test
    void execute_returnsCachedResponseOnSecondCall() {
        AtomicInteger executionCount = new AtomicInteger(0);
        NativeJavaTool tool = new NativeJavaTool() {
            @Override
            public String toolId() {
                return "counting-tool";
            }

            @Override
            public String displayName() {
                return "Counting Tool";
            }

            @Override
            public String description() {
                return "counts invocations";
            }

            @Override
            public Map<String, Object> inputSchema() {
                return Map.of();
            }

            @Override
            public boolean isCacheable() {
                return true;
            }

            @Override
            public long cacheTtlSeconds() {
                return 60;
            }

            @Override
            public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> input) {
                return ToolExecutionResult.of(Map.of("count", executionCount.incrementAndGet()));
            }
        };

        ToolRegistryService registryService = new ToolRegistryService(List.of(tool), true);
        AgentHardeningPolicyService hardeningPolicyService = mock(AgentHardeningPolicyService.class);
        when(hardeningPolicyService.evaluateTokenBudget(any(), any(), anyString(), anyLong())).thenReturn(
            HardeningDecision.allow(List.of("TEST"), Map.of())
        );
        ToolExecutionService executionService = new ToolExecutionService(
            registryService,
            new ConcurrentMapCacheManager("native-tools"),
            new ObjectMapper(),
            mock(SystemLogService.class),
            hardeningPolicyService,
            true,
            300,
            5000
        );

        ToolExecutionResponse first = executionService.execute(
            "counting-tool",
            new ToolExecutionContext("a", null, null, null),
            Map.of("x", 1)
        );
        ToolExecutionResponse second = executionService.execute(
            "counting-tool",
            new ToolExecutionContext("a", null, null, null),
            Map.of("x", 1)
        );

        assertEquals(1, first.result().get("count"));
        assertEquals(1, second.result().get("count"));
        assertEquals(true, second.cached());
    }

    @Test
    void execute_throwsValidationOnDisabledTools() {
        ToolRegistryService registryService = new ToolRegistryService(List.of(), false);
        AgentHardeningPolicyService hardeningPolicyService = mock(AgentHardeningPolicyService.class);
        when(hardeningPolicyService.evaluateTokenBudget(any(), any(), anyString(), anyLong())).thenReturn(
            HardeningDecision.allow(List.of("TEST"), Map.of())
        );
        ToolExecutionService executionService = new ToolExecutionService(
            registryService,
            new ConcurrentMapCacheManager("native-tools"),
            new ObjectMapper(),
            mock(SystemLogService.class),
            hardeningPolicyService,
            true,
            300,
            5000
        );

        assertThrows(
            ValidationException.class,
            () -> executionService.execute("missing-tool", new ToolExecutionContext(null, null, null, null), Map.of())
        );
    }
}
