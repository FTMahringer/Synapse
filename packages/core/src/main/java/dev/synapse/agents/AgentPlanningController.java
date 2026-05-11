package dev.synapse.agents;

import dev.synapse.agents.service.AgentPlanningService;
import dev.synapse.core.dto.CreatePlanningArtifactRequest;
import dev.synapse.core.dto.CreatePlanningGoalRequest;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.NextPlanStepDTO;
import dev.synapse.core.dto.PlanningArtifactDTO;
import dev.synapse.core.dto.PlanningGoalDTO;
import dev.synapse.core.dto.RefinePlanningArtifactRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/planning")
public class AgentPlanningController {

    private final AgentPlanningService planningService;

    public AgentPlanningController(AgentPlanningService planningService) {
        this.planningService = planningService;
    }

    @GetMapping("/goals")
    public List<PlanningGoalDTO> listGoals(@PathVariable String teamId) {
        return planningService.listGoals(teamId).stream().map(DtoMapper::toDTO).toList();
    }

    @PostMapping("/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningGoalDTO createGoal(
        @PathVariable String teamId,
        @Valid @RequestBody CreatePlanningGoalRequest request
    ) {
        return DtoMapper.toDTO(
            planningService.createGoal(
                teamId,
                request.collaborationSessionId(),
                request.title(),
                request.goalStatement(),
                request.createdByAgentId()
            )
        );
    }

    @GetMapping("/goals/{goalId}")
    public PlanningGoalDTO getGoal(@PathVariable String teamId, @PathVariable UUID goalId) {
        return DtoMapper.toDTO(planningService.getGoal(teamId, goalId));
    }

    @GetMapping("/goals/{goalId}/plans")
    public List<PlanningArtifactDTO> listPlans(@PathVariable String teamId, @PathVariable UUID goalId) {
        return planningService.listPlans(teamId, goalId).stream().map(DtoMapper::toDTO).toList();
    }

    @PostMapping("/goals/{goalId}/plans")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningArtifactDTO createPlan(
        @PathVariable String teamId,
        @PathVariable UUID goalId,
        @Valid @RequestBody CreatePlanningArtifactRequest request
    ) {
        return DtoMapper.toDTO(
            planningService.createInitialPlan(
                teamId,
                goalId,
                request.compactSummary(),
                request.steps(),
                request.reasoningChain(),
                request.createdByAgentId()
            )
        );
    }

    @PostMapping("/goals/{goalId}/plans/refine")
    public PlanningArtifactDTO refinePlan(
        @PathVariable String teamId,
        @PathVariable UUID goalId,
        @Valid @RequestBody RefinePlanningArtifactRequest request
    ) {
        return DtoMapper.toDTO(
            planningService.refinePlan(
                teamId,
                goalId,
                request.basePlanId(),
                request.compactSummary(),
                request.steps(),
                request.reasoningChain(),
                request.createdByAgentId()
            )
        );
    }

    @GetMapping("/goals/{goalId}/next-step")
    public NextPlanStepDTO getNextStep(@PathVariable String teamId, @PathVariable UUID goalId) {
        var plans = planningService.listPlans(teamId, goalId);
        if (plans.isEmpty()) {
            return new NextPlanStepDTO(goalId, null, null, java.util.Map.of());
        }
        var active = plans.stream().filter(plan -> "ACTIVE".equals(plan.getStatus().name())).findFirst().orElse(plans.get(0));
        return new NextPlanStepDTO(goalId, active.getId(), active.getPlanVersion(), planningService.nextStep(teamId, goalId));
    }
}
