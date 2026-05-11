package dev.synapse.agents.service;

import dev.synapse.core.common.domain.PlanningArtifact;
import dev.synapse.core.common.domain.PlanningGoal;
import dev.synapse.core.common.repository.AgentTeamRepository;
import dev.synapse.core.common.repository.PlanningArtifactRepository;
import dev.synapse.core.common.repository.PlanningGoalRepository;
import dev.synapse.core.common.repository.TeamMembershipRepository;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentPlanningService {

    private final AgentTeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final PlanningGoalRepository planningGoalRepository;
    private final PlanningArtifactRepository planningArtifactRepository;
    private final AgentHardeningPolicyService hardeningPolicyService;
    private final SystemLogService logService;

    public AgentPlanningService(
        AgentTeamRepository teamRepository,
        TeamMembershipRepository teamMembershipRepository,
        PlanningGoalRepository planningGoalRepository,
        PlanningArtifactRepository planningArtifactRepository,
        AgentHardeningPolicyService hardeningPolicyService,
        SystemLogService logService
    ) {
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.planningGoalRepository = planningGoalRepository;
        this.planningArtifactRepository = planningArtifactRepository;
        this.hardeningPolicyService = hardeningPolicyService;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public List<PlanningGoal> listGoals(String teamId) {
        ensureTeamExists(teamId);
        return planningGoalRepository.findByTeamIdOrderByCreatedAtDesc(teamId);
    }

    @Transactional(readOnly = true)
    public PlanningGoal getGoal(String teamId, UUID goalId) {
        PlanningGoal goal = planningGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("PlanningGoal", goalId.toString()));
        ensureGoalBelongsToTeam(teamId, goal);
        return goal;
    }

    @Transactional
    public PlanningGoal createGoal(
        String teamId,
        UUID collaborationSessionId,
        String title,
        String goalStatement,
        String createdByAgentId
    ) {
        ensureTeamExists(teamId);
        ensureAgentInTeam(teamId, createdByAgentId);

        PlanningGoal goal = new PlanningGoal();
        goal.setTeamId(teamId);
        goal.setCollaborationSessionId(collaborationSessionId);
        goal.setTitle(title);
        goal.setGoalStatement(goalStatement);
        goal.setCreatedByAgentId(createdByAgentId);
        goal.setStatus(PlanningGoal.GoalStatus.ACTIVE);

        PlanningGoal saved = planningGoalRepository.save(goal);
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT_TEAM,
            Map.of("component", "AgentPlanningService", "goalId", saved.getId().toString(), "teamId", teamId),
            "PLANNING_GOAL_CREATED",
            Map.of("title", title, "createdByAgentId", createdByAgentId),
            null,
            null
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<PlanningArtifact> listPlans(String teamId, UUID goalId) {
        getGoal(teamId, goalId);
        return planningArtifactRepository.findByGoalIdOrderByPlanVersionDesc(goalId);
    }

    @Transactional
    public PlanningArtifact createInitialPlan(
        String teamId,
        UUID goalId,
        String compactSummary,
        List<Map<String, Object>> steps,
        List<Map<String, Object>> reasoningChain,
        String createdByAgentId
    ) {
        PlanningGoal goal = getGoal(teamId, goalId);
        ensureGoalActive(goal);
        ensureAgentInTeam(teamId, createdByAgentId);
        if (!planningArtifactRepository.findByGoalIdOrderByPlanVersionDesc(goalId).isEmpty()) {
            throw new ValidationException("Goal '" + goalId + "' already has planning artifacts; use refine endpoint");
        }
        HardeningDecision hardeningDecision = hardeningPolicyService.evaluatePlanning(goalId, steps != null ? steps.size() : 0, 0);
        if (hardeningDecision.decision() == HardeningDecision.Decision.BLOCK) {
            throw new ValidationException("Planning blocked by hardening policy: " + hardeningDecision.reasonCode());
        }
        return saveNewPlan(goalId, 1, compactSummary, steps, reasoningChain, createdByAgentId);
    }

    @Transactional
    public PlanningArtifact refinePlan(
        String teamId,
        UUID goalId,
        UUID basePlanId,
        String compactSummary,
        List<Map<String, Object>> steps,
        List<Map<String, Object>> reasoningChain,
        String createdByAgentId
    ) {
        PlanningGoal goal = getGoal(teamId, goalId);
        ensureGoalActive(goal);
        ensureAgentInTeam(teamId, createdByAgentId);

        PlanningArtifact basePlan = planningArtifactRepository.findById(basePlanId)
            .orElseThrow(() -> new ResourceNotFoundException("PlanningArtifact", basePlanId.toString()));
        if (!basePlan.getGoalId().equals(goalId)) {
            throw new ValidationException("Base plan does not belong to goal '" + goalId + "'");
        }

        int nextVersion = planningArtifactRepository.findByGoalIdOrderByPlanVersionDesc(goalId).stream()
            .findFirst()
            .map(existing -> existing.getPlanVersion() + 1)
            .orElse(1);
        HardeningDecision hardeningDecision = hardeningPolicyService.evaluatePlanning(
            goalId,
            steps != null ? steps.size() : 0,
            nextVersion - 1
        );
        if (hardeningDecision.decision() == HardeningDecision.Decision.BLOCK) {
            throw new ValidationException("Planning refinement blocked by hardening policy: " + hardeningDecision.reasonCode());
        }

        basePlan.setStatus(PlanningArtifact.PlanStatus.SUPERSEDED);
        planningArtifactRepository.save(basePlan);

        return saveNewPlan(goalId, nextVersion, compactSummary, steps, reasoningChain, createdByAgentId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> nextStep(String teamId, UUID goalId) {
        getGoal(teamId, goalId);
        PlanningArtifact activePlan = planningArtifactRepository.findFirstByGoalIdAndStatusOrderByPlanVersionDesc(
                goalId, PlanningArtifact.PlanStatus.ACTIVE
            )
            .orElseThrow(() -> new ResourceNotFoundException("PlanningArtifact", "active plan for goal " + goalId));

        return activePlan.getStepsJson().stream()
            .filter(step -> !"DONE".equals(String.valueOf(step.getOrDefault("status", "PENDING"))))
            .findFirst()
            .orElse(Map.of());
    }

    private PlanningArtifact saveNewPlan(
        UUID goalId,
        int version,
        String compactSummary,
        List<Map<String, Object>> steps,
        List<Map<String, Object>> reasoningChain,
        String createdByAgentId
    ) {
        if (steps == null || steps.isEmpty()) {
            throw new ValidationException("Planning artifact must include at least one step");
        }

        List<Map<String, Object>> normalizedSteps = steps.stream()
            .map(step -> {
                if (step == null) {
                    throw new ValidationException("Planning step cannot be null");
                }
                if (!step.containsKey("title")) {
                    throw new ValidationException("Each planning step must include a 'title'");
                }
                java.util.HashMap<String, Object> normalized = new java.util.HashMap<>(step);
                normalized.putIfAbsent("status", "PENDING");
                return Map.copyOf(normalized);
            })
            .toList();

        int completedSteps = (int) normalizedSteps.stream()
            .filter(step -> "DONE".equals(String.valueOf(step.get("status"))))
            .count();

        PlanningArtifact artifact = new PlanningArtifact();
        artifact.setGoalId(goalId);
        artifact.setPlanVersion(version);
        artifact.setStatus(PlanningArtifact.PlanStatus.ACTIVE);
        artifact.setCompactSummary(compactSummary);
        artifact.setStepsJson(normalizedSteps);
        artifact.setReasoningChainJson(reasoningChain != null ? reasoningChain : List.of());
        artifact.setTotalSteps(normalizedSteps.size());
        artifact.setCompletedSteps(completedSteps);
        artifact.setCreatedByAgentId(createdByAgentId);

        PlanningArtifact saved = planningArtifactRepository.save(artifact);
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT_TEAM,
            Map.of("component", "AgentPlanningService", "goalId", goalId.toString()),
            "PLANNING_ARTIFACT_SAVED",
            Map.of("planVersion", version, "totalSteps", normalizedSteps.size(), "createdByAgentId", createdByAgentId),
            null,
            null
        );
        return saved;
    }

    private void ensureGoalActive(PlanningGoal goal) {
        if (goal.getStatus() != PlanningGoal.GoalStatus.ACTIVE) {
            throw new ValidationException("Planning goal '" + goal.getId() + "' is not active");
        }
    }

    private void ensureTeamExists(String teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("AgentTeam", teamId);
        }
    }

    private void ensureAgentInTeam(String teamId, String agentId) {
        boolean present = teamMembershipRepository.findByTeamId(teamId).stream()
            .anyMatch(membership -> membership.getAgentId().equals(agentId));
        if (!present) {
            throw new ValidationException("Agent '" + agentId + "' is not a member of team '" + teamId + "'");
        }
    }

    private void ensureGoalBelongsToTeam(String teamId, PlanningGoal goal) {
        if (!goal.getTeamId().equals(teamId)) {
            throw new ValidationException("Planning goal does not belong to team '" + teamId + "'");
        }
    }
}
