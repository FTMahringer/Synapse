# v2.4.0 Advanced Agent Capabilities - Memory Tiering Design

## Problem Statement

The current memory model is not explicitly tiered for lifecycle and retrieval quality. To support reliable long-term evolution (including self-learning loops), we need structured memory tiers with clear transition rules, retention policies, and retrieval semantics.

## Scope (This Spec)

This spec defines the `v2.3.1-dev` design foundation for Advanced Agent Capabilities:

- Three-tier memory architecture
- Tier transition and promotion policy
- Retrieval ordering and filtering behavior
- Summarization/compression placement
- Error handling and test strategy

This spec does **not** implement full collaboration/reasoning/tool integration behavior; it creates the memory substrate they rely on.

## Goals

1. Introduce explicit tier boundaries in memory lifecycle.
2. Preserve backward compatibility while enabling future backend split.
3. Improve retrieval precision by separating reusable facts from compressed history.
4. Enable self-learning loops with policy-driven promotion across tiers.

## Non-Goals

1. Full vector infrastructure redesign in this step.
2. Complete autonomous learning policy optimization.
3. Cross-service distributed memory replication.

## Architecture

Use a **hybrid staged split**:

- Keep current storage path for now.
- Add tier metadata and lifecycle fields in domain entities.
- Introduce tier-aware interfaces so tiers can later route to different stores/backends.

### Memory Tiers

1. `SHORT_TERM`
   - Default write target for fresh chat/context memory.
   - High churn, short retention.
2. `KNOWLEDGE`
   - Reused or explicitly pinned high-value memory.
   - Stable facts, decisions, preferences.
3. `ARCHIVE`
   - Summarized/compressed historical memory artifacts.
   - Durable, low-verbosity context history.

## Data Model Changes

Add tiering/lifecycle metadata to memory entries:

- `tier` (`SHORT_TERM` | `KNOWLEDGE` | `ARCHIVE`)
- `promoted_at` (nullable timestamp)
- `promotion_reason` (nullable enum/string: `REUSED`, `PINNED`, `SUMMARIZED`, etc.)
- `last_accessed_at`
- `access_count`
- `retention_until` (nullable)
- `source_entry_ids` for archive/summarized artifacts (when relevant)

Legacy rows are backfilled to `SHORT_TERM` unless explicit migration policy overrides.

## Data Flow

1. New conversation memory writes default to `SHORT_TERM`.
2. Policy engine evaluates promotion candidates:
   - frequent reuse/access,
   - explicit pin/save action,
   - summarization output.
3. Promotion route:
   - `SHORT_TERM -> KNOWLEDGE` (high-signal reusable memory)
   - `SHORT_TERM -> ARCHIVE` (summarized/compressed output)
   - `KNOWLEDGE -> ARCHIVE` (optional consolidation over time)
4. Retrieval default behavior:
   - query `SHORT_TERM` first,
   - fallback to `KNOWLEDGE`,
   - use `ARCHIVE` for broader compressed historical context.
5. API supports strict tier filters and tier-weighted retrieval.

## Transition Rules

Allowed transitions:

- `SHORT_TERM -> KNOWLEDGE`
- `SHORT_TERM -> ARCHIVE`
- `KNOWLEDGE -> ARCHIVE`

Rejected transitions:

- Direct non-system writes to `ARCHIVE`
- `ARCHIVE -> SHORT_TERM`
- `ARCHIVE -> KNOWLEDGE` (unless future explicit restore flow is added)

## Self-Learning Loop Fit

Three-tier structure improves self-learning loops by separating:

- ephemeral interaction context (`SHORT_TERM`),
- validated reusable memory (`KNOWLEDGE`),
- compressed historical traces for long-horizon adaptation (`ARCHIVE`).

This reduces noise contamination and allows policy feedback to target the right tier.

### Lifecycle Cadence (Storage + Quality)

To reduce long-term storage and quality risks, run scheduled maintenance:

1. **Monthly KNOWLEDGE compaction to ARCHIVE**
   - Generate compressed summaries of stale but still relevant KNOWLEDGE clusters.
   - Preserve canonical high-value entries in KNOWLEDGE when confidence/importance remains high.
   - Link archive summaries back to source entries for traceability.

2. **Bi-monthly ARCHIVE cleanup/compaction**
   - Detect duplicate or near-duplicate archived summaries.
   - Merge/compact similar artifacts and drop low-value duplicates.
   - Recompute archive metadata (freshness, confidence, lineage pointers).

This cadence keeps retrieval sets cleaner and slows storage growth while preserving useful historical signal.

## API/Service Behavior

Introduce tier-aware methods while preserving existing API compatibility:

- write defaults to `SHORT_TERM`
- read supports optional `tier` filter
- read defaults to tier-priority order
- promotion endpoint/service method accepts explicit reason
- summarization pipeline writes only to `ARCHIVE`

## Error Handling

1. Invalid tier transitions return explicit domain validation errors.
2. Promotion without required metadata (reason/source) fails explicitly.
3. Retrieval with unknown tier values fails with validation errors.
4. No silent drops: failures are surfaced and logged consistently.

## Testing Strategy

1. Unit tests:
   - transition rule enforcement,
   - policy-based promotion decisions,
   - retrieval ordering and filtering.
2. Integration tests:
   - migration/backfill behavior for legacy entries,
   - end-to-end summarization to `ARCHIVE`,
   - tier-aware query correctness under realistic chat sequences.
3. Regression checks:
   - existing memory APIs continue to function for non-tier-aware callers.

## Rollout Plan (v2.3.1-dev Focus)

1. Add tier fields + migration.
2. Add domain enums/rules for transitions.
3. Implement policy service scaffolding.
4. Add tier-aware retrieval defaults and filter support.
5. Add tests for transitions/retrieval/migration.
6. Update docs and release artifacts for `v2.3.1-dev`.

## Risks and Mitigations

1. **Risk:** Retrieval regressions due to tier prioritization.
   - **Mitigation:** Preserve backward-compatible defaults and add integration tests.
2. **Risk:** Over-promotion into `KNOWLEDGE`.
   - **Mitigation:** conservative thresholds + explicit promotion reason tracking.
3. **Risk:** Archive bloat.
   - **Mitigation:** retention/compaction policies, source linkage, and bi-monthly archive dedupe/merge.

## Success Criteria

1. New entries are consistently stored as `SHORT_TERM` by default.
2. Promotion pipeline moves entries into `KNOWLEDGE`/`ARCHIVE` with valid reasons.
3. Summarization outputs are stored in `ARCHIVE`.
4. Tier-aware retrieval improves context relevance without breaking existing consumers.
5. Documentation and release workflow align with AGENT rules and docs-coupled prereleases.
