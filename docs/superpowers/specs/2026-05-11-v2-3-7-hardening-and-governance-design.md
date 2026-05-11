# v2.3.7-dev Agent Capability Hardening & Governance Design

## Problem Statement

Advanced agent flows now include memory, collaboration, planning, and native tools. Without centralized safety policy enforcement, the system risks:

- delegation loops and runaway plans
- token-budget overrun and inconsistent concise-mode behavior
- fragmented rule checks scattered across services
- weak governance for policy override requests

## Goals

1. Implement strict hardening for both delegation/planning safety and token/concise controls.
2. Use a single **central policy engine** for all runtime decisions.
3. Define a rule precedence model that allows admin-governed overrides.
4. Prepare future-ready governance ideas for rule management and user↔admin request flows.

## Non-Goals

1. Full external rule DSL in this step.
2. Complete admin portal implementation in this step.
3. Cross-service distributed policy orchestration beyond current backend boundaries.

## Chosen Approach

Use a **central policy engine** (`AgentHardeningPolicyService`) that evaluates advanced-agent actions before execution.  
This engine returns unified outcomes: `ALLOW`, `WARN`, or `BLOCK`, with structured reason codes.

This is preferred over feature-local checks because it minimizes drift and allows future rule extension without refactoring all services.

## Runtime Hardening Model

### 1. Delegation & Planning Guardrails (strict)

Block conditions:

- circular delegation path detected
- delegation depth/hops exceed configured limits
- plan step count exceeds configured maximum
- refinement count exceeds configured maximum

Behavior:

- runtime action is blocked with explicit validation/error code
- structured event logged for audit/profiling

### 2. Token & Concise Guardrails (strict)

Controls:

- per-run token budget
- per-phase token budget (planning/collaboration/tooling)
- concise-mode threshold that forces concise behavior when crossed

Block conditions:

- hard token cap exceeded

Behavior:

- if threshold crossed: enforce concise mode
- if cap exceeded: block with explicit reason code
- log policy decision with budget metrics

### 3. Unified Policy Outcome

All guarded runtime paths consume one policy response envelope:

- `decision`: `ALLOW` | `WARN` | `BLOCK`
- `reasonCode`
- `appliedRules` (rule IDs/sources)
- optional `enforcedMode` (e.g., concise)

## Rules Model

Rules are structured by scope:

1. `GLOBAL` (baseline defaults)
2. `TEMPLATE` (reusable policy packs)
3. `AGENT` (agent-specific rules/overrides)

Rule domains:

- `DELEGATION`
- `PLANNING`
- `TOKEN`
- `MODE`

Rule payload contains typed limits and flags (e.g., `maxDelegationDepth`, `maxPlanSteps`, `maxRefinements`, `tokenBudget`, `conciseThreshold`).

## Rule Precedence & Governance

Precedence:

1. apply global baseline
2. apply template
3. apply agent override

**Important governance rule:**

- global and agent overrides are mutable only by admins
- users may request overrides, but requests require admin approval before activation

Status model for override requests:

- `PENDING_ADMIN_APPROVAL`
- `APPROVED`
- `REJECTED`
- `EXPIRED`

Only approved entries affect effective policy.

## Integration Points (v2.3.7 implementation)

The policy engine is invoked before:

1. delegation creation/forwarding
2. planning artifact creation/refinement
3. token-cost-sensitive advanced workflow execution

Each integration path must use the same policy contract and reason-code set.

## Performance & Cost Profiling

Add profiling hooks around advanced-agent flows:

- policy evaluation latency
- blocked vs allowed rates by reason code
- token usage by phase and by agent/team
- concise-mode enforcement frequency

Purpose:

- validate guardrail effectiveness
- tune thresholds using real usage data

## Testing Strategy

1. **Policy unit tests**
   - precedence merge behavior (`GLOBAL -> TEMPLATE -> AGENT`)
   - delegation loop/depth detection
   - planning runaway checks
   - budget threshold and hard-cap behavior

2. **Integration tests**
   - collaboration/planning paths correctly blocked on policy violations
   - concise-mode enforcement path on threshold crossing
   - consistent reason codes and response shape

3. **Regression tests**
   - normal flows unaffected when under limits
   - no duplicate policy checks causing inconsistent decisions

## Documentation Updates (v2.3.7)

Refresh docs for advanced operations:

- hardening guardrails
- policy outcome semantics
- token budget + concise enforcement behavior
- operator guidance for threshold tuning

## New Ideas to add in `ideas/`

### 1. Rules System (extensible governance)

Idea document should cover:

- global/template/agent rule classes
- admin-only override governance
- template library model and reusable policy packs

### 2. Central Request/Notification Channel (user↔admin)

Idea document should cover:

- request lifecycle for override and policy exceptions
- admin inbox and decision actions
- user notifications and audit trail
- runtime linkage: only approved requests impact effective policy

## Risks & Mitigations

1. **Risk:** policy engine becomes bottleneck  
   **Mitigation:** lightweight evaluation, profile latency, cache derived effective policy where safe.

2. **Risk:** overblocking harms usability  
   **Mitigation:** clear reason codes, staged thresholds, operator-tunable limits.

3. **Risk:** rule sprawl/inconsistency over time  
   **Mitigation:** strict precedence model, template governance, centralized evaluation service.

4. **Risk:** token controls not uniformly applied  
   **Mitigation:** enforce integration contract at all advanced flow entry points.

## Success Criteria

1. Delegation loops and runaway plans are deterministically blocked.
2. Token budget and concise enforcement behave consistently across advanced flows.
3. All hardening decisions come from the central policy engine.
4. Rule governance and override approval model is clearly defined for future rollout.
5. Docs and release artifacts reflect the hardening model and operator guidance.
