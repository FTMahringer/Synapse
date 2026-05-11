# Rules System (Global + Template + Agent)

## Problem

Hardening and behavior constraints must stay consistent across all agents, while still allowing targeted overrides when justified.

## Direction

Adopt a **central policy model** with strict precedence:

1. global baseline rules (admin-managed, always applied)
2. template rules (role/module defaults)
3. agent overrides (only when explicitly approved)

Effective policy is computed centrally and consumed by collaboration, planning, tool/runtime, and provider dispatch paths.

## Governance Model

- **Admin-only direct edits** for global rules.
- Users can submit override requests, but cannot self-apply them.
- Requested overrides become active only after explicit admin approval.
- Every policy decision logs:
  - resolved rule set version
  - matched rule IDs
  - ALLOW/WARN/BLOCK outcome
  - reason code

## Data Model Ideas

- `policy_rules` (scope, rule_id, expression/config, status, version)
- `policy_override_requests` (requested_by, scope, proposed_changes, status, reviewed_by)
- `policy_decision_audit` (context, effective_policy_hash, decision, reason_code)

## Rollout

1. ship baseline central evaluator (global-only)
2. add template scope resolution
3. add admin-approved agent overrides
4. expose policy diff/preview endpoint before approval
