package dev.synapse.core.agents;

import dev.synapse.agents.service.AgentPlanningService;
import dev.synapse.agents.service.AgentHardeningPolicyService;
import dev.synapse.agents.service.HardeningDecision;
import dev.synapse.core.common.domain.PlanningArtifact;
import dev.synapse.core.common.domain.PlanningGoal;
import dev.synapse.core.common.domain.TeamMembership;
import dev.synapse.core.common.repository.AgentTeamRepository;
import dev.synapse.core.common.repository.PlanningArtifactRepository;
import dev.synapse.core.common.repository.PlanningGoalRepository;
import dev.synapse.core.common.repository.TeamMembershipRepository;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentPlanningServiceTest {

    @Mock
    private AgentTeamRepository teamRepository;
    @Mock
    private TeamMembershipRepository teamMembershipRepository;
    @Mock
    private PlanningGoalRepository planningGoalRepository;
    @Mock
    private PlanningArtifactRepository planningArtifactRepository;
    @Mock
    private AgentHardeningPolicyService hardeningPolicyService;
    @Mock
    private SystemLogService logService;

    private AgentPlanningService planningService;

    @BeforeEach
    void setUp() {
        planningService = new AgentPlanningService(
            teamRepository,
            teamMembershipRepository,
            planningGoalRepository,
            planningArtifactRepository,
            hardeningPolicyService,
            logService
        );
        when(hardeningPolicyService.evaluatePlanning(any(), anyInt(), anyInt())).thenReturn(
            HardeningDecision.allow(List.of("TEST"), Map.of())
        );
    }

    @Test
    void createInitialPlan_requiresStepTitle() {
        UUID goalId = UUID.randomUUID();
        PlanningGoal goal = new PlanningGoal();
        goal.setId(goalId);
        goal.setTeamId("ops-team");
        goal.setStatus(PlanningGoal.GoalStatus.ACTIVE);

        TeamMembership membership = new TeamMembership();
        membership.setTeamId("ops-team");
        membership.setAgentId("agent-alpha");

        when(teamRepository.existsById("ops-team")).thenReturn(true);
        when(planningGoalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(teamMembershipRepository.findByTeamId("ops-team")).thenReturn(List.of(membership));
        when(planningArtifactRepository.findByGoalIdOrderByPlanVersionDesc(goalId)).thenReturn(List.of());

        assertThrows(
            ValidationException.class,
            () -> planningService.createInitialPlan(
                "ops-team",
                goalId,
                "summary",
                List.of(Map.of("status", "PENDING")),
                List.of(),
                "agent-alpha"
            )
        );
    }

    @Test
    void refinePlan_incrementsVersionAndSupersedesBase() {
        UUID goalId = UUID.randomUUID();
        UUID basePlanId = UUID.randomUUID();

        PlanningGoal goal = new PlanningGoal();
        goal.setId(goalId);
        goal.setTeamId("ops-team");
        goal.setStatus(PlanningGoal.GoalStatus.ACTIVE);

        TeamMembership membership = new TeamMembership();
        membership.setTeamId("ops-team");
        membership.setAgentId("agent-alpha");

        PlanningArtifact basePlan = new PlanningArtifact();
        basePlan.setId(basePlanId);
        basePlan.setGoalId(goalId);
        basePlan.setPlanVersion(1);
        basePlan.setStatus(PlanningArtifact.PlanStatus.ACTIVE);

        PlanningArtifact latestPlan = new PlanningArtifact();
        latestPlan.setId(basePlanId);
        latestPlan.setGoalId(goalId);
        latestPlan.setPlanVersion(1);
        latestPlan.setStatus(PlanningArtifact.PlanStatus.ACTIVE);

        when(teamRepository.existsById("ops-team")).thenReturn(true);
        when(planningGoalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(teamMembershipRepository.findByTeamId("ops-team")).thenReturn(List.of(membership));
        when(planningArtifactRepository.findById(basePlanId)).thenReturn(Optional.of(basePlan));
        when(planningArtifactRepository.findByGoalIdOrderByPlanVersionDesc(goalId)).thenReturn(List.of(latestPlan));
        when(planningArtifactRepository.save(any(PlanningArtifact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanningArtifact refined = planningService.refinePlan(
            "ops-team",
            goalId,
            basePlanId,
            "v2 summary",
            List.of(Map.of("title", "step one")),
            List.of(Map.of("summary", "reasoning trace")),
            "agent-alpha"
        );

        assertEquals(PlanningArtifact.PlanStatus.SUPERSEDED, basePlan.getStatus());
        assertEquals(2, refined.getPlanVersion());
        assertEquals(PlanningArtifact.PlanStatus.ACTIVE, refined.getStatus());
    }
}
