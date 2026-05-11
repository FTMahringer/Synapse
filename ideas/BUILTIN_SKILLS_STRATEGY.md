# Built-in Skills Strategy (Hybrid Model)

**Status**: Idea  
**Priority**: High (core built-ins), Medium (future candidate pipeline)  
**Target**: v2.x planning window

---

## Problem

Skill installation friction and repeated prompt overhead increase user token usage. Some capabilities should be available immediately without manual install flow.

---

## Goal

Introduce a hybrid built-in skill model that:

1. Guarantees core platform capabilities are always available.
2. Lets users activate curated built-ins through a first-run TUI (no install step).
3. Reduces token usage by default while keeping user control where appropriate.

---

## Hybrid Built-in Model

### 1. Core Enforced Built-ins (Forced Active)

These skills are shipped with SYNAPSE and cannot be disabled by end users.

- Initial core enforced skill: `codeburn`
- Purpose: platform-level usage/cost telemetry and related system workflows
- UX: shown as **Active (System)** in skill lists
- Lifecycle: pinned by platform version; updated through normal platform release cycle

### 2. Curated Selectable Built-ins (Activation Only)

These skills are also shipped with SYNAPSE, but users choose activation state.

- Initial selectable built-in: `caveman` (`https://github.com/JuliusBrussee/caveman`)
- UX: **Activate / Deactivate / Change mode**
- No install/uninstall path; only activation-state and mode changes
- Designed for token-saving and concise communication workflows

---

## First-Run TUI Selection Flow

At first-run (or via settings command), users see a built-in selection screen:

1. Show forced core skills (read-only active).
2. Show selectable built-ins with default recommendation.
3. Let user activate/skip optional built-ins.
4. Persist selection per instance.

Example behavior:

- `codeburn`: preselected, locked, always active
- `caveman`: optional toggle, mode configurable

---

## Lifecycle & Governance Rules

1. **Built-in packages are platform-managed**  
   Users do not install them from store sources.

2. **Activation state is instance-managed**  
   Optional built-ins are controlled at instance level (hybrid behavior), while core built-ins remain enforced globally.

3. **Version pinning**  
   Built-ins should be pinned to tested revisions per SYNAPSE release to avoid drift.

4. **Security/trust posture**  
   Promotion to built-in requires review for maintenance quality, safety, and compatibility.

---

## Skill Classes (Runtime View)

The runtime should treat skills as three classes:

1. `CORE_BUILTIN` — forced active, non-disableable (e.g., `codeburn`)
2. `OPTIONAL_BUILTIN` — shipped by platform, activation toggle only (e.g., `caveman`)
3. `USER_INSTALLED` — install/remove lifecycle via store/local sources

This keeps behavior clear and avoids mixing platform dependencies with user-managed extensions.

---

## Future Candidate Pipeline

Low-priority but valuable: maintain a shortlist of candidate built-ins from:

- `skills.sh`
- `https://skills.sh/obra/superpowers`

Candidate promotion criteria:

1. Broad utility across most users/chats
2. Clear token-efficiency or quality gain
3. Stable maintenance cadence
4. Safe defaults and predictable behavior

These remain non-enforced until explicitly promoted to one of the built-in classes.

---

## Rollout Proposal

1. Introduce classification and runtime flags (`CORE_BUILTIN`, `OPTIONAL_BUILTIN`).
2. Ship `codeburn` as core enforced.
3. Ship `caveman` as optional built-in with mode controls.
4. Add first-run TUI built-in selector.
5. Add periodic review process for future candidate promotions.

---

## Expected Benefits

- Lower token usage through immediate availability of compression-oriented workflows.
- Better out-of-box experience (no setup for essential capabilities).
- Clear control boundaries: platform-critical vs user-preference built-ins.
- Cleaner long-term governance for adding widely useful skills.
