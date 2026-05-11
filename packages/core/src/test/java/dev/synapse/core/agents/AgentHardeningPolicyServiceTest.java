package dev.synapse.core.agents;

import dev.synapse.agents.service.AgentHardeningPolicyService;
import dev.synapse.agents.service.HardeningDecision;
import dev.synapse.core.common.domain.CollaborationDelegation;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AgentHardeningPolicyServiceTest {

    private AgentHardeningPolicyService service;

    @BeforeEach
    void setUp() {
        service = new AgentHardeningPolicyService(
            mock(SystemLogService.class),
            true,
            2,
            3,
            2,
            100,
            100,
            100,
            100,
            0.5,
            60
        );
    }

    @Test
    void evaluateDelegation_blocksLoops() {
        CollaborationDelegation existing = new CollaborationDelegation();
        existing.setFromAgentId("agent-b");
        existing.setToAgentId("agent-a");
        existing.setStatus(CollaborationDelegation.DelegationStatus.PENDING);

        HardeningDecision decision = service.evaluateDelegation(
            UUID.randomUUID(),
            "agent-a",
            "agent-b",
            List.of(existing)
        );

        assertEquals(HardeningDecision.Decision.BLOCK, decision.decision());
        assertEquals("DELEGATION_LOOP_DETECTED", decision.reasonCode());
    }

    @Test
    void evaluatePlanning_blocksExcessiveSteps() {
        HardeningDecision decision = service.evaluatePlanning(UUID.randomUUID(), 4, 0);
        assertEquals(HardeningDecision.Decision.BLOCK, decision.decision());
        assertEquals("PLANNING_MAX_STEPS_EXCEEDED", decision.reasonCode());
    }

    @Test
    void evaluateTokenBudget_warnsThenBlocks() {
        HardeningDecision first = service.evaluateTokenBudget("agent-a", "ops", "TOOLING", 60);
        HardeningDecision second = service.evaluateTokenBudget("agent-a", "ops", "TOOLING", 41);

        assertEquals(HardeningDecision.Decision.WARN, first.decision());
        assertEquals("TOKEN_BUDGET_NEAR_LIMIT", first.reasonCode());
        assertEquals(HardeningDecision.Decision.BLOCK, second.decision());
        assertEquals("TOKEN_BUDGET_EXCEEDED", second.reasonCode());
    }
}
