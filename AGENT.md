# AGENT.md - Agent Execution Guide

This file defines how AI agents should operate in this repository.

## Precedence model

1. `AGENT.md` (this file): agent execution behavior and repository context.
2. `RULES.md`: canonical project workflow and policy rules.

When a topic is process/policy (versioning, releases, testing, docs coupling), follow `RULES.md`.

---

## Required startup behavior

Before major work:
1. Read `RULES.md`.
2. Check current repository status and relevant nested repository status (`synapse-docs`).
3. Confirm scope and target repository (main repo vs docs repo).

---

## Repository context and boundaries

- Main codebase root: this repository.
- Docs live in nested, separate git repo: `synapse-docs\`.
- Never mix commits across these repositories.
- Keep changes scoped to the active task; avoid unrelated refactors.

---

## Agent execution standards

1. Make focused, reversible changes.
2. Prefer existing patterns and conventions over invention.
3. Surface blockers plainly and early.
4. Do not silently skip failing steps.
5. Keep user-facing communication concise and outcome-first.

---

## Commit and release reminders for agents

- Use Conventional Commit style.
- Include trailer:

`Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`

- Apply release/version/security rules from `RULES.md`.

---

## Quick links

- Project rules: `RULES.md`
- Root changelog: `CHANGELOG.md`
- Docs repository: `synapse-docs\`

