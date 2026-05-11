package dev.synapse.agents.service;

import java.util.List;
import java.util.Map;

public record HardeningDecision(
    Decision decision,
    String reasonCode,
    String enforcedMode,
    List<String> appliedRules,
    Map<String, Object> metadata
) {
    public enum Decision {
        ALLOW, WARN, BLOCK
    }

    public static HardeningDecision allow(List<String> appliedRules, Map<String, Object> metadata) {
        return new HardeningDecision(Decision.ALLOW, "OK", null, appliedRules, metadata);
    }

    public static HardeningDecision warn(
        String reasonCode,
        String enforcedMode,
        List<String> appliedRules,
        Map<String, Object> metadata
    ) {
        return new HardeningDecision(Decision.WARN, reasonCode, enforcedMode, appliedRules, metadata);
    }

    public static HardeningDecision block(
        String reasonCode,
        List<String> appliedRules,
        Map<String, Object> metadata
    ) {
        return new HardeningDecision(Decision.BLOCK, reasonCode, null, appliedRules, metadata);
    }
}
