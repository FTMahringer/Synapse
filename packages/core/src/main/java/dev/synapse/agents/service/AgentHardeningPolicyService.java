package dev.synapse.agents.service;

import dev.synapse.core.common.domain.CollaborationDelegation;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentHardeningPolicyService {

    private final SystemLogService logService;
    private final boolean enabled;
    private final int maxDelegationHops;
    private final int maxPlanSteps;
    private final int maxPlanRefinements;
    private final long tokenBudgetPlanning;
    private final long tokenBudgetCollaboration;
    private final long tokenBudgetTooling;
    private final long tokenBudgetDefault;
    private final double conciseThresholdRatio;
    private final long tokenWindowMinutes;
    private final ConcurrentHashMap<String, TokenWindowUsage> tokenUsage = new ConcurrentHashMap<>();

    public AgentHardeningPolicyService(
        SystemLogService logService,
        @Value("${synapse.hardening.enabled:true}") boolean enabled,
        @Value("${synapse.hardening.delegation.max-hops:6}") int maxDelegationHops,
        @Value("${synapse.hardening.planning.max-steps:20}") int maxPlanSteps,
        @Value("${synapse.hardening.planning.max-refinements:5}") int maxPlanRefinements,
        @Value("${synapse.hardening.token.phase-budgets.planning:4000}") long tokenBudgetPlanning,
        @Value("${synapse.hardening.token.phase-budgets.collaboration:4000}") long tokenBudgetCollaboration,
        @Value("${synapse.hardening.token.phase-budgets.tooling:4000}") long tokenBudgetTooling,
        @Value("${synapse.hardening.token.default-budget:12000}") long tokenBudgetDefault,
        @Value("${synapse.hardening.token.concise-threshold-ratio:0.75}") double conciseThresholdRatio,
        @Value("${synapse.hardening.token.scope-window-minutes:60}") long tokenWindowMinutes
    ) {
        this.logService = logService;
        this.enabled = enabled;
        this.maxDelegationHops = maxDelegationHops;
        this.maxPlanSteps = maxPlanSteps;
        this.maxPlanRefinements = maxPlanRefinements;
        this.tokenBudgetPlanning = tokenBudgetPlanning;
        this.tokenBudgetCollaboration = tokenBudgetCollaboration;
        this.tokenBudgetTooling = tokenBudgetTooling;
        this.tokenBudgetDefault = tokenBudgetDefault;
        this.conciseThresholdRatio = conciseThresholdRatio;
        this.tokenWindowMinutes = tokenWindowMinutes;
    }

    public HardeningDecision evaluateDelegation(
        UUID sessionId,
        String fromAgentId,
        String toAgentId,
        List<CollaborationDelegation> existingDelegations
    ) {
        long startedAtNs = System.nanoTime();
        List<String> rules = List.of("GLOBAL_BASELINE", "DELEGATION_GUARDRAILS");
        if (!enabled) {
            return HardeningDecision.allow(rules, Map.of("hardeningEnabled", false));
        }
        if (fromAgentId.equals(toAgentId)) {
            return logDecision(
                "DELEGATION",
                HardeningDecision.block("DELEGATION_SELF_TARGET", rules, Map.of("sessionId", sessionId.toString())),
                startedAtNs
            );
        }

        Map<String, Set<String>> graph = new HashMap<>();
        for (CollaborationDelegation delegation : existingDelegations) {
            if (delegation.getStatus() == CollaborationDelegation.DelegationStatus.REJECTED
                || delegation.getStatus() == CollaborationDelegation.DelegationStatus.CANCELLED
                || delegation.getStatus() == CollaborationDelegation.DelegationStatus.COMPLETED) {
                continue;
            }
            graph.computeIfAbsent(delegation.getFromAgentId(), ignored -> new HashSet<>()).add(delegation.getToAgentId());
        }
        graph.computeIfAbsent(fromAgentId, ignored -> new HashSet<>()).add(toAgentId);

        if (pathExists(graph, toAgentId, fromAgentId)) {
            return logDecision(
                "DELEGATION",
                HardeningDecision.block("DELEGATION_LOOP_DETECTED", rules, Map.of("sessionId", sessionId.toString())),
                startedAtNs
            );
        }

        int hops = longestHopPath(graph, fromAgentId);
        if (hops > maxDelegationHops) {
            return logDecision(
                "DELEGATION",
                HardeningDecision.block(
                    "DELEGATION_MAX_HOPS_EXCEEDED",
                    rules,
                    Map.of("sessionId", sessionId.toString(), "hops", hops, "maxDelegationHops", maxDelegationHops)
                ),
                startedAtNs
            );
        }

        return logDecision(
            "DELEGATION",
            HardeningDecision.allow(rules, Map.of("sessionId", sessionId.toString(), "hops", hops)),
            startedAtNs
        );
    }

    public HardeningDecision evaluatePlanning(UUID goalId, int stepCount, int refinementCount) {
        long startedAtNs = System.nanoTime();
        List<String> rules = List.of("GLOBAL_BASELINE", "PLANNING_GUARDRAILS");
        if (!enabled) {
            return HardeningDecision.allow(rules, Map.of("hardeningEnabled", false));
        }
        if (stepCount > maxPlanSteps) {
            return logDecision(
                "PLANNING",
                HardeningDecision.block(
                    "PLANNING_MAX_STEPS_EXCEEDED",
                    rules,
                    Map.of("goalId", goalId.toString(), "stepCount", stepCount, "maxPlanSteps", maxPlanSteps)
                ),
                startedAtNs
            );
        }
        if (refinementCount > maxPlanRefinements) {
            return logDecision(
                "PLANNING",
                HardeningDecision.block(
                    "PLANNING_MAX_REFINEMENTS_EXCEEDED",
                    rules,
                    Map.of(
                        "goalId", goalId.toString(),
                        "refinementCount", refinementCount,
                        "maxPlanRefinements", maxPlanRefinements
                    )
                ),
                startedAtNs
            );
        }
        return logDecision(
            "PLANNING",
            HardeningDecision.allow(
                rules,
                Map.of("goalId", goalId.toString(), "stepCount", stepCount, "refinementCount", refinementCount)
            ),
            startedAtNs
        );
    }

    public HardeningDecision evaluateTokenBudget(
        String agentId,
        String teamId,
        String phase,
        long estimatedTokens
    ) {
        long startedAtNs = System.nanoTime();
        List<String> rules = List.of("GLOBAL_BASELINE", "TOKEN_GUARDRAILS", "MODE_GUARDRAILS");
        if (!enabled) {
            return HardeningDecision.allow(rules, Map.of("hardeningEnabled", false));
        }

        String normalizedPhase = normalizePhase(phase);
        long budget = budgetForPhase(normalizedPhase);
        String scopeKey = nonNull(agentId) + "|" + nonNull(teamId) + "|" + normalizedPhase;

        TokenWindowUsage windowUsage = tokenUsage.compute(scopeKey, (key, current) -> {
            Instant now = Instant.now();
            if (current == null || now.isAfter(current.windowEndsAt())) {
                return new TokenWindowUsage(0, now.plusSeconds(tokenWindowMinutes * 60));
            }
            return current;
        });

        long projected = windowUsage.usedTokens() + Math.max(estimatedTokens, 0);
        if (projected > budget) {
            return logDecision(
                "TOKEN",
                HardeningDecision.block(
                    "TOKEN_BUDGET_EXCEEDED",
                    rules,
                    Map.of("phase", normalizedPhase, "projectedTokens", projected, "budget", budget)
                ),
                startedAtNs
            );
        }

        tokenUsage.compute(scopeKey, (key, current) -> {
            Instant now = Instant.now();
            if (current == null || now.isAfter(current.windowEndsAt())) {
                return new TokenWindowUsage(Math.max(estimatedTokens, 0), now.plusSeconds(tokenWindowMinutes * 60));
            }
            return new TokenWindowUsage(current.usedTokens() + Math.max(estimatedTokens, 0), current.windowEndsAt());
        });

        if (projected >= Math.round(budget * conciseThresholdRatio)) {
            return logDecision(
                "TOKEN",
                HardeningDecision.warn(
                    "TOKEN_BUDGET_NEAR_LIMIT",
                    "CONCISE",
                    rules,
                    Map.of("phase", normalizedPhase, "projectedTokens", projected, "budget", budget)
                ),
                startedAtNs
            );
        }

        return logDecision(
            "TOKEN",
            HardeningDecision.allow(
                rules,
                Map.of("phase", normalizedPhase, "projectedTokens", projected, "budget", budget)
            ),
            startedAtNs
        );
    }

    private HardeningDecision logDecision(String policyType, HardeningDecision decision, long startedAtNs) {
        long elapsedMicros = (System.nanoTime() - startedAtNs) / 1_000;
        LogLevel logLevel = decision.decision() == HardeningDecision.Decision.BLOCK
            ? LogLevel.WARN
            : LogLevel.INFO;
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("policyType", policyType);
        payload.put("decision", decision.decision().name());
        payload.put("reasonCode", decision.reasonCode());
        payload.put("evaluationMicros", elapsedMicros);
        payload.putAll(decision.metadata());
        if (decision.enforcedMode() != null) {
            payload.put("enforcedMode", decision.enforcedMode());
        }

        logService.log(
            logLevel,
            LogCategory.AGENT,
            Map.of("component", "AgentHardeningPolicyService"),
            "HARDENING_POLICY_EVALUATED",
            payload,
            null,
            null
        );
        return decision;
    }

    private boolean pathExists(Map<String, Set<String>> graph, String source, String target) {
        ArrayDeque<String> stack = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        stack.push(source);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            if (target.equals(current)) {
                return true;
            }
            for (String next : graph.getOrDefault(current, Set.of())) {
                stack.push(next);
            }
        }
        return false;
    }

    private int longestHopPath(Map<String, Set<String>> graph, String start) {
        return longestHopPathDfs(graph, start, new HashSet<>());
    }

    private int longestHopPathDfs(Map<String, Set<String>> graph, String current, Set<String> path) {
        if (!path.add(current)) {
            return 0;
        }
        int maxDepth = 0;
        for (String next : graph.getOrDefault(current, Set.of())) {
            maxDepth = Math.max(maxDepth, 1 + longestHopPathDfs(graph, next, path));
        }
        path.remove(current);
        return maxDepth;
    }

    private long budgetForPhase(String phase) {
        return switch (phase) {
            case "PLANNING" -> tokenBudgetPlanning;
            case "COLLABORATION" -> tokenBudgetCollaboration;
            case "TOOLING" -> tokenBudgetTooling;
            default -> tokenBudgetDefault;
        };
    }

    private String normalizePhase(String phase) {
        if (phase == null || phase.isBlank()) {
            return "DEFAULT";
        }
        return phase.toUpperCase();
    }

    private String nonNull(String value) {
        return value != null ? value : "";
    }

    private record TokenWindowUsage(long usedTokens, Instant windowEndsAt) {}

}
