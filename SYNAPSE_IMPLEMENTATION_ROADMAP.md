# SYNAPSE — Implementation Roadmap

> Source of truth for implementation order, versioning, and roadmap labels.
> This roadmap starts from the current scaffold baseline and moves toward a production-ready self-hosted AI platform.

---

## 1. Roadmap Rules

### 1.1 Versioning Model

- `v0.0.0` is the scaffold baseline.
- Each major roadmap step advances the minor version: `v0.1.0`, `v0.2.0`, `v0.3.0`, and so on.
- Small commits inside a step advance the patch version: `v0.1.1`, `v0.1.2`, `v0.1.3`.
- A step is considered complete only when its exit criteria are met and the next `v0.x.0` milestone is ready.
- Version numbers describe implementation progress, not package compatibility.

### 1.2 Commit and Tag Convention

- Patch work should be committed with the current patch version in the message or body.
- Step completion should be committed as the matching `v0.x.0` milestone.
- Optional tags may be created at major milestones for traceability.
- The roadmap itself is the planning document; the Git history is the implementation record.

### 1.3 Label Policy

- GitHub labels are fixed and stable.
- Labels are grouped by roadmap phase and execution status.
- Labels are used to track issues, PRs, and implementation slices.
- Labels are not versioned per patch. Versioning stays in commits and tags.

---

## 2. Fixed Roadmap Labels

<!-- ROADMAP_LABELS:BEGIN -->
```json
{
  "labels": [
    {
      "name": "roadmap:phase-foundation",
      "color": "0E7490",
      "description": "Repository governance, roadmap control, versioning rules, and label automation."
    },
    {
      "name": "roadmap:phase-backend",
      "color": "2563EB",
      "description": "Database schema, seed data, vault specification, persistence, and backend foundations."
    },
    {
      "name": "roadmap:phase-agents",
      "color": "7C3AED",
      "description": "Main agent, templates, team structure, AI-firm, and ECHO runtime behavior."
    },
    {
      "name": "roadmap:phase-plugins",
      "color": "EA580C",
      "description": "Channel plugins, model providers, skills, MCP support, ACP registry, and provider discovery."
    },
    {
      "name": "roadmap:phase-store",
      "color": "16A34A",
      "description": "Store concept, bundle format, registry data, submission flow, and community publishing rules."
    },
    {
      "name": "roadmap:phase-docs",
      "color": "6B7280",
      "description": "Architecture, subsystem docs, API reference, and operator-facing documentation."
    },
    {
      "name": "roadmap:phase-installer",
      "color": "D97706",
      "description": "Unix, macOS, and Windows installers, Docker Compose quick modes, and bootstrap flows."
    },
    {
      "name": "roadmap:phase-cli",
      "color": "0891B2",
      "description": "Go CLI command surface, Bubble Tea TUI behavior, and operator workflows."
    },
    {
      "name": "roadmap:phase-runtime",
      "color": "14B8A6",
      "description": "Dashboard theming, realtime transport, logging delivery, and learning loop runtime behavior."
    },
    {
      "name": "roadmap:phase-hardening",
      "color": "DC2626",
      "description": "Quality rules, validation, release candidate preparation, and final launch checks."
    },
    {
      "name": "roadmap:status-planned",
      "color": "94A3B8",
      "description": "Work item is planned but not yet started."
    },
    {
      "name": "roadmap:status-active",
      "color": "3B82F6",
      "description": "Work item is currently being implemented."
    },
    {
      "name": "roadmap:status-blocked",
      "color": "EF4444",
      "description": "Work item is blocked by a dependency, design decision, or external constraint."
    },
    {
      "name": "roadmap:status-done",
      "color": "22C55E",
      "description": "Work item is completed and validated."
    }
  ]
}
```
<!-- ROADMAP_LABELS:END -->

---

## 3. Roadmap Overview

| Milestone | Version | Phase | Main Output |
|---|---:|---|---|
| Baseline | `v0.0.0` | Scaffold | Current repo skeleton, docs, agents, backend specs, plugins, and store specs |
| Step 1 | `v0.1.0` | Foundation | Roadmap control, label sync, repository discipline, release conventions |
| Step 2 | `v0.2.0` | Backend | PostgreSQL schema, seed data, vault contract, backend persistence rules |
| Step 3 | `v0.3.0` | Agents | Main Agent, templates, teams, AI-firm, and ECHO runtime files |
| Step 4 | `v0.4.0` | Plugins | Channel, model, skill, MCP, ACP, and provider integrations |
| Step 5 | `v0.5.0` | Store | Store types, bundle flow, registry structure, and submission path |
| Step 6 | `v0.6.0` | Docs | Full subsystem docs, API reference, and operator guidance |
| Step 7 | `v0.7.0` | Installer | Shell, PowerShell, and Compose-based installation paths |
| Step 8 | `v0.8.0` | CLI | Go CLI spec, command tree, TUI behavior, and operator contracts |
| Step 9 | `v0.9.0` | Runtime Delivery | Dashboard theming, realtime transport, logging flow, and learning loop wiring |
| Step 10 | `v0.10.0` | Hardening | Quality checks, integration validation, release candidate, and final launch review |

**Current version:** `v0.5.0`

---

## 4. Phase 1 - Foundation

**Target version:** `v0.1.0`

**Status:** `roadmap:status-done`

**Goal**
- Establish the implementation workflow before more code lands.
- Keep the roadmap, labels, and versioning rules stable.

**Scope**
- Add and maintain the roadmap file.
- Sync fixed GitHub labels from the roadmap.
- Define the commit/tag convention for patch and step versions.
- Add any repository policy documents needed to support implementation work.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 1
- SYNAPSE_BUILD_STEPS.md Step 2
- SYNAPSE_BUILD_STEPS.md Step 38 policy checks

**Patch window**
- `v0.1.1` sync workflow and label block
- `v0.1.2` repository policy refinements
- `v0.1.3` roadmap wording and milestone cleanup

**Exit criteria**
- The roadmap file is the single place where roadmap labels are defined. Complete.
- GitHub labels can be created or updated from the roadmap without manual editing. Complete.
- Repository contributors can follow the versioning rules without ambiguity. Complete.

---

## 5. Phase 2 - Backend Foundation

**Target version:** `v0.2.0`

**Status:** `roadmap:status-done`

**Goal**
- Lock down the server-side persistence model before runtime behavior expands.

**Scope**
- PostgreSQL schema for system metadata, logs, agents, teams, firms, plugins, users, tasks, sessions, stores, and integrations.
- Seed data for system metadata and baseline configuration.
- Vault specification for per-agent memory storage, compression, and vector search integration.
- Persistence boundaries for backend features that will depend on the schema.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 3
- SYNAPSE_BUILD_STEPS.md Step 4
- SYNAPSE_BUILD_STEPS.md Step 5

**Patch window**
- `v0.2.1` schema fixes and index adjustments
- `v0.2.2` seed data and default config refinement
- `v0.2.3` vault structure and compression notes

**Exit criteria**
- Database schema is complete enough to support the planned core entities. Complete.
- Seed data initializes the system cleanly. Complete.
- Vault storage and compression rules are documented clearly enough to implement without guesswork. Complete.

---

## 6. Phase 3 - Agents

**Target version:** `v0.3.0`

**Status:** `roadmap:status-done`

**Goal**
- Build the identity layer that makes the platform feel like a multi-agent system instead of a single chat app.

**Scope**
- Agent templates for identity, soul, connections, and config.
- Team templates with leader structure and routing.
- Main Agent files and system prompt.
- ECHO debug agent files and manual-only behavior.
- AI-firm example files and CEO control surface.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 6
- SYNAPSE_BUILD_STEPS.md Step 7
- SYNAPSE_BUILD_STEPS.md Step 8
- SYNAPSE_BUILD_STEPS.md Step 9
- SYNAPSE_BUILD_STEPS.md Step 10

**Patch window**
- `v0.3.1` agent identity template refinement
- `v0.3.2` team routing and config cleanup
- `v0.3.3` ECHO and AI-firm prompt tightening

**Exit criteria**
- A new agent can be described from the template set without inventing new file formats. Complete.
- Main Agent, team leader, and ECHO behavior are explicit and non-overlapping. Complete.
- AI-firm remains optional and limited to one instance. Complete.

---

## 7. Phase 4 - Plugins

**Target version:** `v0.4.0`

**Status:** `roadmap:status-done`

**Goal**
- Provide the extension runtime that lets the platform connect to channels, models, skills, and external protocols.

**Scope**
- Channel plugin templates and Telegram example.
- Model provider templates for Anthropic, OpenAI, DeepSeek, and Ollama.
- Skills and MCP templates.
- ACP registry integration rules and provider discovery.
- Git provider integration for optional source control linkage.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 11
- SYNAPSE_BUILD_STEPS.md Step 12
- SYNAPSE_BUILD_STEPS.md Step 13
- SYNAPSE_BUILD_STEPS.md Step 25
- SYNAPSE_BUILD_STEPS.md Step 32

**Patch window**
- `v0.4.1` channel manifest and loader cleanup
- `v0.4.2` model-provider manifest and provider contract cleanup
- `v0.4.3` skills, MCP, ACP, and git-provider integration polishing

**Exit criteria**
- Extension types have stable manifest formats. Complete.
- Providers and channels can be described without extra ad hoc metadata. Complete.
- Optional integrations remain optional and clearly isolated. Complete.

---

## 8. Phase 5 - Store

**Target version:** `v0.5.0`

**Status:** `roadmap:status-done`

**Goal**
- Make community distribution, bundle publishing, and store browsing explicit before full public docs are finalized.

**Scope**
- Store specification and bundle specification.
- Registry structure example.
- Contributor submission guide.
- Official vs community store distinction.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 14

**Patch window**
- `v0.5.1` store types and filtering rules
- `v0.5.2` bundle validation and submission flow
- `v0.5.3` registry examples and store taxonomy cleanup

**Exit criteria**
- Store types are defined clearly. Complete.
- Bundle validation rules are documented. Complete.
- Community publishing can be reasoned about without reading the implementation. Complete.

---

## 9. Phase 6 - Documentation

**Target version:** `v0.6.0`

**Status:** `roadmap:status-planned`

**Goal**
- Turn the platform design into a usable reference set for builders and operators.

**Scope**
- Architecture documentation.
- Plugin system documentation.
- Agent identity, team, and AI-firm documentation.
- Memory vault, self-learning, heartbeat, skills, MCP, ACP, store, bundle, multi-user, logging, theming, ECHO, git provider, custom commands, and API reference docs.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 15
- SYNAPSE_BUILD_STEPS.md Step 16
- SYNAPSE_BUILD_STEPS.md Step 17
- SYNAPSE_BUILD_STEPS.md Step 18
- SYNAPSE_BUILD_STEPS.md Step 19
- SYNAPSE_BUILD_STEPS.md Step 20
- SYNAPSE_BUILD_STEPS.md Step 21
- SYNAPSE_BUILD_STEPS.md Step 22
- SYNAPSE_BUILD_STEPS.md Step 23
- SYNAPSE_BUILD_STEPS.md Step 24
- SYNAPSE_BUILD_STEPS.md Step 25
- SYNAPSE_BUILD_STEPS.md Step 26
- SYNAPSE_BUILD_STEPS.md Step 27
- SYNAPSE_BUILD_STEPS.md Step 28
- SYNAPSE_BUILD_STEPS.md Step 29
- SYNAPSE_BUILD_STEPS.md Step 30
- SYNAPSE_BUILD_STEPS.md Step 31
- SYNAPSE_BUILD_STEPS.md Step 32
- SYNAPSE_BUILD_STEPS.md Step 33
- SYNAPSE_BUILD_STEPS.md Step 34

**Patch window**
- `v0.6.1` architecture and logging docs
- `v0.6.2` agent and plugin docs
- `v0.6.3` store, installer, CLI, and API docs cleanup

**Exit criteria**
- Every major subsystem has a dedicated doc.
- The docs match the file tree and the system rules.
- Both guided and manual creation paths are described where relevant.

---

## 10. Phase 7 - Installer

**Target version:** `v0.7.0`

**Goal**
- Make the platform installable in a repeatable way on Unix, macOS, and Windows.

**Scope**
- Interactive shell installer.
- PowerShell installer.
- Docker Compose quick mode and production compose files.
- Installer prompts for system name, install type, domain, provider selection, ECHO, and git provider options.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 35
- SYNAPSE_BUILD_STEPS.md Step 36

**Patch window**
- `v0.7.1` shell installer flow
- `v0.7.2` Windows installer flow
- `v0.7.3` compose and environment bootstrap refinement

**Exit criteria**
- Installers can bootstrap the platform without manual file surgery.
- Quick mode and dev mode are both represented.
- Platform defaults are visible and changeable at install time.

---

## 11. Phase 8 - CLI

**Target version:** `v0.8.0`

**Goal**
- Define the operator CLI contract before implementation begins.

**Scope**
- CLI command reference.
- Flags, args, and help text model.
- Bubble Tea and Lipgloss TUI behavior.
- Operator-friendly command structure for connection, status, logs, and management flows.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 37

**Patch window**
- `v0.8.1` command tree and help text
- `v0.8.2` TUI layout and color system
- `v0.8.3` operator workflow cleanup

**Exit criteria**
- CLI commands are named and grouped consistently.
- TUI expectations are explicit before implementation.
- CLI documentation can be generated or maintained from one canonical spec.

---

## 12. Phase 9 - Runtime Delivery

**Target version:** `v0.9.0`

**Goal**
- Wire the user-facing runtime surfaces that make the system observable and usable day to day.

**Scope**
- Dashboard theming and layout rules.
- Realtime delivery strategy.
- Logging flow and delivery surfaces.
- Heartbeat, self-learning, and operational feedback loops where they affect runtime behavior.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 21
- SYNAPSE_BUILD_STEPS.md Step 22
- SYNAPSE_BUILD_STEPS.md Step 29
- SYNAPSE_BUILD_STEPS.md Step 30

**Patch window**
- `v0.9.1` realtime delivery and dashboard updates
- `v0.9.2` logging categories and event shape cleanup
- `v0.9.3` heartbeat and learning-loop polish

**Exit criteria**
- Runtime feedback is visible to operators.
- Logging, heartbeat, and UI behavior are consistent.
- The dashboard has a defined theming contract.

---

## 13. Phase 10 - Hardening

**Target version:** `v0.10.0`

**Goal**
- Validate the whole system against the quality rules before release candidate work is considered done.

**Scope**
- Cross-check the quality rules.
- Verify the critical files.
- Remove placeholders and inconsistency risks.
- Confirm that the platform is still aligned with the original plan.

**Build-step coverage**
- SYNAPSE_BUILD_STEPS.md Step 38
- SYNAPSE_BUILD_STEPS.md Step 39

**Patch window**
- `v0.10.1` quality-rule fixes
- `v0.10.2` critical-file review
- `v0.10.3` final consistency pass

**Exit criteria**
- No placeholder text remains in roadmap-critical files.
- Main Agent, backend schema, logging docs, and team docs are complete enough to support implementation.
- The release candidate checklist is explicit and reviewable.

---

## 14. Execution Labels and Status Usage

Use these labels on issues and PRs while implementing the roadmap:

- `roadmap:phase-foundation` for roadmap control, versioning, and repo governance.
- `roadmap:phase-backend` for schema, seed, vault, and backend persistence work.
- `roadmap:phase-agents` for agent identity, prompt, team, ECHO, and AI-firm work.
- `roadmap:phase-plugins` for channels, models, skills, MCP, ACP, and git-provider work.
- `roadmap:phase-store` for store, bundle, and registry work.
- `roadmap:phase-docs` for documentation and reference updates.
- `roadmap:phase-installer` for install and bootstrap flows.
- `roadmap:phase-cli` for CLI/TUI specification and implementation.
- `roadmap:phase-runtime` for dashboard, logging delivery, realtime transport, and learning-loop wiring.
- `roadmap:phase-hardening` for QA, cleanup, and release candidate work.
- `roadmap:status-planned` for queued work.
- `roadmap:status-active` for work in progress.
- `roadmap:status-blocked` for blocked work.
- `roadmap:status-done` for finished work.

---

## 15. Practical Workflow

1. Pick the next `v0.x.0` milestone from the phase list.
2. Mark the related issue or PR with the correct phase label and `roadmap:status-active`.
3. Implement one patch-sized slice at a time as `v0.x.1`, `v0.x.2`, and so on.
4. When the step is complete, move the work to `roadmap:status-done`.
5. Advance the roadmap to the next `v0.x.0` milestone only after the previous one is validated.
