# v2.4.1-dev Design — Hybrid API Security Foundation

## Context

After releasing `v2.4.0`, the next roadmap step is `v2.4.1-dev` under the `v2.5.0 Security Hardening` milestone.  
The selected implementation strategy is **hybrid**: edge/gateway controls plus app-native enforcement.

This spec defines the design for:

- rate limiting per user/endpoint
- request throttling
- API key rotation (provider keys + user/access keys)
- CORS hardening

Rollout policy selected:

- enforce rate limits immediately
- keep rotation-policy failures log-only in this step

---

## Goals

1. Add layered request protection that blocks abusive traffic early and consistently.
2. Enforce identity-aware backend quotas for authenticated and key-based traffic.
3. Introduce a unified key lifecycle model and rotation workflow for both provider and access keys.
4. Harden CORS policy to explicit allowlists and least-privilege defaults.
5. Preserve operational safety by using mixed enforcement rollout (hard for limits, log-only for rotation policy).

## Non-Goals (v2.4.1-dev)

- full auto-remediation for every key provider type
- mandatory hard-fail on rotation-policy violations
- broad security refactors outside API security scope

---

## Architecture

### 1. Edge Layer (Gateway)

The gateway provides coarse-grained, high-throughput controls before traffic reaches Spring:

- baseline rate limiting and burst throttling
- route-class segmentation (`public`, `authenticated`, `sensitive`)
- baseline CORS enforcement for origin/method/header policy

This layer is optimized for early rejection and perimeter protection.

### 2. App-Native Layer (Spring)

The backend enforces identity-aware policy:

- user/API-key scoped limits
- endpoint policy resolution
- key lifecycle and rotation-policy evaluation
- security audit event generation

This layer is the source of truth for business-aware enforcement and governance decisions.

### Boundary Rule

- Gateway owns coarse traffic protection.
- Backend owns actor-aware policy, key lifecycle, and security decision auditing.

---

## Components

### A. Gateway Security Configuration

- Route profiles:
  - `public`: stricter anonymous quotas
  - `authenticated`: identity-aware baseline limits
  - `sensitive`: lowest burst, strictest steady-state rate
- CORS configuration:
  - explicit allowed origins
  - explicit methods and headers
  - credentials policy by route profile

### B. Backend Rate-Limit Module

### `RateLimitPolicyService`

Resolves effective policy from:

- endpoint/route class
- actor type (`anonymous`, `user`, `api-key`)
- optional overrides for sensitive operations

### `RateLimitEnforcementFilter`

Runs after request context/auth resolution and:

- computes quota key (`apiKeyId` > `userId` > `ip`)
- applies token-bucket/sliding-window logic in Redis
- returns structured `429` responses for limit violations

### C. API Key Lifecycle Module

Unified model for both key classes:

- key type: `PROVIDER` or `ACCESS`
- owner scope (system/provider/user)
- status (active, rotated, revoked, expired)
- lifecycle timestamps (created, lastRotated, expiresAt)

Capabilities:

- manual rotation action endpoint
- scheduled rotation scanner
- rotation-policy evaluator (log-only in this step)

### D. Security Audit/Event Pipeline

Structured events for:

- allow/throttle/block decisions
- CORS denials
- rotation due/overdue/policy warning states

All events include correlation metadata to connect gateway and backend traces.

---

## Data Flow

### Request Flow

1. Request enters gateway.
2. Gateway applies route-class throttling and CORS baseline checks.
3. Backend resolves identity/context.
4. Backend applies identity-aware quota policy.
5. Request proceeds or returns `429`.
6. Decision is logged as structured security/audit event.

### Rotation Flow

1. Scheduler/manual trigger selects candidate keys.
2. Rotation action is attempted or key is marked rotation-needed.
3. Rotation-policy evaluator computes compliance state.
4. Violations emit warning events/logs (no hard rejection in `v2.4.1-dev`).

---

## Error Handling

- Gateway rejection responses are standardized (`429`/CORS deny payload shape).
- Backend limit rejections return structured policy metadata without leaking sensitive key material.
- Rotation-policy failures do not block traffic in this release; they emit warning-grade security events.
- For policy-store unavailability:
  - sensitive routes: fail closed
  - non-sensitive routes: fail open with explicit warning logs/events

---

## Testing Strategy

### Unit

- policy resolution by endpoint/actor type
- quota key precedence and derivation
- rate window/token calculations
- rotation-policy evaluation state transitions

### Integration

- edge + backend layered throttling interactions
- anonymous vs authenticated vs api-key quota behavior
- CORS allow/deny matrix across route classes
- scheduler/manual rotation behavior with warning event assertions

### Runtime/Smoke

- Compose startup with gateway and Redis enabled
- health endpoint validation
- enforcement behavior validation (`429` on expected over-limit scenarios)

---

## Configuration Surface

Introduce/update config groups aligned with existing patterns:

- gateway rate-limit settings by route class
- backend `synapse.security.rate-limit.*`
- backend `synapse.security.cors.*`
- backend `synapse.security.keys.rotation.*`
- rollout mode flags:
  - `enforceRateLimits=true`
  - `enforceRotationPolicy=false` (log-only)

All keys must be documented in `synapse-docs` environment/deployment docs during implementation.

---

## Rollout Plan for v2.4.1-dev

1. Implement gateway baseline throttling and CORS hardening.
2. Implement backend identity-aware rate-limit enforcement.
3. Implement unified key inventory + rotation hooks (provider + access keys).
4. Enable mixed mode:
   - enforce rate limiting
   - log-only rotation-policy failures
5. Ship docs and release artifacts for `v2.4.1-dev`.

---

## Acceptance Criteria

1. Requests exceeding configured quotas are rejected with deterministic `429` behavior.
2. CORS policy is explicit and denies non-allowlisted origin/method/header combinations.
3. Provider and access keys are tracked in a unified lifecycle model.
4. Rotation-policy violations are observable via logs/events without breaking request flow.
5. Compose startup and API health checks pass with security layer enabled.

---

## Risks and Mitigations

### Risk: Double-throttling false positives (gateway + backend)
- Mitigation: route-class budget calibration and documented policy hierarchy.

### Risk: Redis dependency pressure for high cardinality keys
- Mitigation: bounded key TTLs, key cardinality controls, and scoped key construction.

### Risk: Operational confusion in mixed enforcement phase
- Mitigation: explicit event reason codes and rollout-mode indicators in logs/docs.

### Risk: Key rotation variance across providers
- Mitigation: common lifecycle contract with provider-specific adapters and fallback warning states.

