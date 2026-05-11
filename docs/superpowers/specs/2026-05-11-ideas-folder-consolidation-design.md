# Ideas Folder Consolidation Design

## Problem

The `ideas/` folder currently has overlapping files and repeated content. Some entries are already represented in the roadmap, while others are fragmented across multiple documents.

This causes:

- duplicate maintenance work
- unclear source-of-truth for idea status
- harder roadmap promotion decisions

## Goals

1. Reduce duplication by consolidating related files.
2. Keep idea navigation simple and maintainable.
3. Promote roadmap-ready ideas into `docs/roadmaps/SYNAPSE_V3_IMPLEMENTATION_ROADMAP.md` with clearer sub-steps.
4. Preserve useful content while removing stale or repeated text.

## Non-Goals

1. Changing released version history.
2. Implementing feature code from ideas in this cleanup pass.
3. Rewriting the roadmap scope beyond relevant idea promotion/sub-step refinement.

## Approved Approach (Recommended Hybrid Cleanup)

Use focused consolidated domain files plus a compact index:

1. `AGENT_AND_MEMORY.md`
   - merge: `AGENT_SYSTEM.md` + `MEMORY_AND_LEARNING.md`
2. `RUNTIME_AND_INTEGRATIONS.md`
   - merge: `API_AND_RUNTIME.md` + `INTEGRATIONS.md` + observability/runtime-relevant parts
3. `PLATFORM_AND_ECOSYSTEM.md`
   - merge: `PLATFORM_FEATURES.md` + `PLUGIN_ECOSYSTEM.md` + `plugin-language-strategy.md`
4. keep `BUILTIN_SKILLS_STRATEGY.md` as dedicated document
5. trim `IDEAS.md` into a compact index/status summary
6. remove superseded source files after consolidation

## Roadmap Promotion Rules

Promote ideas from the consolidated files into roadmap only when at least one is true:

1. Already directly aligned with current/next milestone themes.
2. Dependencies for already-planned roadmap items.
3. Immediately actionable within upcoming dev steps without introducing major scope drift.

Promotion action:

- add/adjust sub-steps in relevant milestone section
- avoid introducing net-new milestone families
- mark promoted idea snippets as moved (or remove duplication in ideas files)

## File-Level Changes

### New/Updated Files

- `ideas/AGENT_AND_MEMORY.md` (new)
- `ideas/RUNTIME_AND_INTEGRATIONS.md` (new)
- `ideas/PLATFORM_AND_ECOSYSTEM.md` (new)
- `ideas/IDEAS.md` (trim to compact index)
- `docs/roadmaps/SYNAPSE_V3_IMPLEMENTATION_ROADMAP.md` (sub-step refinements from promoted items)

### Removed Files (after merge)

- `ideas/AGENT_SYSTEM.md`
- `ideas/MEMORY_AND_LEARNING.md`
- `ideas/API_AND_RUNTIME.md`
- `ideas/INTEGRATIONS.md`
- `ideas/OBSERVABILITY.md`
- `ideas/PLATFORM_FEATURES.md`
- `ideas/PLUGIN_ECOSYSTEM.md`
- `ideas/plugin-language-strategy.md`

(`ideas/README.md` stays if useful as folder entrypoint; not required to duplicate content.)

## Data Flow / Content Flow

1. Inventory current sections and map each to one consolidated target file.
2. Merge content with dedupe and normalize section style (`Status`, `Priority`, `Target`, key points).
3. Extract roadmap-ready items and patch roadmap sub-steps.
4. Replace `IDEAS.md` with compact index + “promoted items” notes.
5. Delete superseded files.

## Error Handling / Safety

1. No destructive rewrite without preserving equivalent content in consolidated files.
2. Keep `BUILTIN_SKILLS_STRATEGY.md` intact as standalone strategy doc.
3. Avoid deleting files until merged content is written and reviewed in working tree.
4. Maintain internal links or update them to new consolidated docs.

## Validation Plan

1. Confirm no duplicated sections remain across consolidated docs.
2. Confirm roadmap receives only selected promoted items.
3. Confirm deleted files are fully represented in consolidated replacements.
4. Confirm ideas folder has clearer, smaller document set.

## Risks and Mitigations

1. **Risk:** accidental content loss during merge  
   **Mitigation:** merge first, delete later; verify by section inventory.

2. **Risk:** roadmap bloat  
   **Mitigation:** only promote actionable/dependency items and keep sub-steps concise.

3. **Risk:** broken cross-doc links  
   **Mitigation:** update references after consolidation and keep names stable.

## Success Criteria

1. `ideas/` has fewer, clearer documents with minimal overlap.
2. Roadmap includes selected promoted items as concrete sub-steps.
3. `IDEAS.md` functions as compact index rather than duplicate catalog.
4. No important idea content is lost.
