# SYNAPSE v2 Implementation Roadmap

> Roadmap for turning the v1 runnable baseline into a working AI platform.
> Development version: `v1.0.1-dev`.
> First V2 release version: `v2.0.0`.
> Base release: `v1.0.0`.

---

## 1. Release Rule

- `v1.0.1-dev` marks the active implementation track after `v1.0.0`.
- Each implementation slice should be committed as a small versioned step.
- Patch-sized commits use `v1.0.x-dev` in notes and tags only when they represent a validated slice.
- `v1.x.0` is tagged only after chat, model provider execution, persistence, realtime logs, auth, plugin registry, and dashboard management flows work locally.
- `v2.0.0` is tagged only after chat, model provider execution, persistence, realtime logs, auth, plugin registry, and dashboard management flows work locally.
- Release notes for `v2.0.0` must include the changelog from all V2 dev slices.

---

## 2. V2 Product Target

V2 turns SYNAPSE from a visible runtime shell into an operational AI platform:

- Users can sign in and receive role-scoped access.
- Users can create conversations and send messages to the Main Agent.
- The backend can call at least Ollama locally and one API-key model provider through a common provider interface.
- Main Agent routing is implemented as a real orchestration service.
- Messages, tool calls, costs, logs, and task events are persisted.
- The dashboard can manage agents, providers, plugins, conversations, logs, settings, and store entries.
- Realtime delivery streams logs and conversation events.
- Plugin and store records move from static specs to installable backend-managed resources.
- ECHO remains manual-only and debug-only.

---

## 3. Non-Negotiable Guardrails

- No automatic ECHO fallback. ECHO is invoked manually only.
- No secret values in logs, API responses, release notes, or commits.
- Provider credentials are encrypted at rest before production release.
- Every mutating API writes a structured system log.
- Every long-running operation has correlation and trace IDs.
- Dashboard must use real backend APIs when an API exists.
- Each milestone must leave Docker Compose runnable.
- V2 cannot break the `v1.0.0` health, agents, and logs APIs without a documented migration.

---

## 4. GitHub Labels

<!-- ROADMAP_LABELS:BEGIN -->
```json
{
  "labels": [
    {
      "name": "roadmap:v2-auth",
      "color": "0F766E",
      "description": "V2 authentication, sessions, users, roles, and authorization work."
    },
    {
      "name": "roadmap:v2-chat-runtime",
      "color": "2563EB",
      "description": "V2 conversation runtime, message persistence, streaming, and Main Agent execution."
    },
    {
      "name": "roadmap:v2-model-providers",
      "color": "7C3AED",
      "description": "V2 model provider registry, Ollama, OpenAI, Anthropic, and provider health checks."
    },
    {
      "name": "roadmap:v2-agent-orchestration",
      "color": "9333EA",
      "description": "V2 agent loading, routing, teams, AI-Firm dispatch, and task orchestration."
    },
    {
      "name": "roadmap:v2-plugins-store",
      "color": "EA580C",
      "description": "V2 plugin lifecycle, store registry, bundle install, and validation workflows."
    },
    {
      "name": "roadmap:v2-realtime",
      "color": "0891B2",
      "description": "V2 WebSocket, SSE, event delivery, live logs, and conversation streaming."
    },
    {
      "name": "roadmap:v2-dashboard",
      "color": "16A34A",
      "description": "V2 dashboard screens, forms, management flows, and operator UX."
    },
    {
      "name": "roadmap:v2-cli",
      "color": "64748B",
      "description": "V2 Go CLI and TUI implementation against backend APIs."
    },
    {
      "name": "roadmap:v2-hardening",
      "color": "DC2626",
      "description": "V2 testing, CI, release hardening, security checks, and migration validation."
    }
  ]
}
```
<!-- ROADMAP_LABELS:END -->

---

## 5. Milestone Overview

| Version | Name | Outcome |
|---|---|---|
| `v1.1.0` | Persistence API Layer | Real CRUD and query APIs over the v1 database foundation |
| `v1.2.0` | Auth and Users | Sign-in, JWT sessions, roles, and role-scoped access |
| `v1.3.0` | Model Providers | Provider registry plus Ollama and external-provider execution |
| `v1.4.0` | Chat Runtime | Conversation creation, message send, model response, persistence |
| `v1.5.0` | Agent Orchestration | Main Agent routing, teams, task dispatch, AI-Firm entry points |
| `v1.6.0` | Realtime Runtime | SSE/WebSocket event bus for logs and conversation events |
| `v1.7.0` | Plugin and Store Runtime | Install, enable, disable, validate, and list plugins/bundles |
| `v1.8.0` | Dashboard Management | Full operator UI over V2 APIs |
| `v1.9.0` | CLI Runtime | Go CLI/TUI connected to backend APIs |
| `v1.10.0` | Release Hardening | Tests, CI, docs, migrations, and `v2.0.0` release readiness |

---

## 6. Detailed Milestones

### v1.1.0 - Persistence API Layer

Goal: replace runtime-only DTOs with database-backed services where the schema already exists.

Patch steps:

- `v1.0.1-dev`: Add repository/service boundaries for users, agents, plugins, model providers, conversations, messages, tasks, and settings.
- `v1.0.2-dev`: Add stable DTOs and validation classes for all read/write APIs.
- `v1.0.3-dev`: Implement agents and teams CRUD using database tables while preserving file-defined bootstrap agents.
- `v1.0.4-dev`: Implement settings and system metadata APIs.
- `v1.0.5-dev`: Implement task and task log APIs.
- `v1.0.6-dev`: Add API error model, request correlation IDs, and structured logging for mutating endpoints.

Exit criteria:

- Dashboard can query core resources from database APIs.
- File-defined agents can be imported or synchronized into database-backed records.
- All mutating APIs write structured logs.
- Compose runtime still starts cleanly from an empty PostgreSQL volume.

### v1.2.0 - Auth and Users

Goal: make SYNAPSE multi-user instead of an unauthenticated local console.

Patch steps:

- `v1.1.1-dev`: Add password hashing, user bootstrap, and owner account initialization.
- `v1.1.2-dev`: Add login, refresh, logout, and session revocation APIs.
- `v1.1.3-dev`: Add JWT verification middleware and role annotations.
- `v1.1.4-dev`: Enforce OWNER, ADMIN, USER, and VIEWER permissions on backend APIs.
- `v1.1.5-dev`: Add dashboard login, session storage, logout, and role-aware navigation.
- `v1.1.6-dev`: Add audit logs for auth events without exposing credentials.

Exit criteria:

- Anonymous users cannot access protected management APIs.
- Owner bootstrap works in Docker Compose.
- Role behavior matches `docs/multi-user.md`.

### v1.3.0 - Model Providers

Goal: implement real model calls behind a provider-neutral interface.

Patch steps:

- `v1.2.1-dev`: Add provider configuration model with encrypted secret storage boundary.
- `v1.2.2-dev`: Implement Ollama provider health and model listing.
- `v1.2.3-dev`: Implement Ollama chat completion request path.
- `v1.2.4-dev`: Implement OpenAI-compatible provider path.
- `v1.2.5-dev`: Implement Anthropic provider path.
- `v1.2.6-dev`: Add provider cost and latency logging.
- `v1.2.7-dev`: Add provider test endpoint that does not store prompt content by default.

Exit criteria:

- At least Ollama can produce a response locally.
- At least one external API-key provider can be configured.
- Failed provider calls are logged without secrets.
- ECHO is not used automatically when providers fail.

### v1.4.0 - Chat Runtime

Goal: make conversations functional end to end.

Patch steps:

- `v1.3.1-dev`: Add conversation create/list/detail APIs.
- `v1.3.2-dev`: Add message send API with persistence.
- `v1.3.3-dev`: Add Main Agent prompt assembly from identity, soul, connections, and system prompt.
- `v1.3.4-dev`: Connect message send to selected model provider.
- `v1.3.5-dev`: Persist assistant responses, token estimates, latency, and provider metadata.
- `v1.3.6-dev`: Add cancellation and timeout handling.
- `v1.3.7-dev`: Add dashboard conversation view with real messages.

Exit criteria:

- User can create a conversation from the dashboard.
- User can send a message and receive a model response.
- Conversation history survives backend restart.

### v1.5.0 - Agent Orchestration

Goal: implement the SYNAPSE agent model beyond static file listing.

Patch steps:

- `v1.4.1-dev`: Add agent runtime registry and activation states.
- `v1.4.2-dev`: Add Main Agent router service with deterministic routing rules.
- `v1.4.3-dev`: Add team leader/member dispatch contract.
- `v1.4.4-dev`: Add AI-Firm project/task dispatch entry point.
- `v1.4.5-dev`: Add agent memory vault read/write hooks.
- `v1.4.6-dev`: Add heartbeat records for active agents.
- `v1.4.7-dev`: Add dashboard management for agent activation and routing inspection.

Exit criteria:

- Main Agent can route a request to a configured agent/team path.
- Routing decisions are logged.
- Agent state is visible in dashboard.

### v1.6.0 - Realtime Runtime

Goal: stream platform events instead of relying only on polling.

Patch steps:

- `v1.5.1-dev`: Add internal event publisher abstraction.
- `v1.5.2-dev`: Add Redis Streams-backed log fanout.
- `v1.5.3-dev`: Add SSE endpoint for live logs.
- `v1.5.4-dev`: Add WebSocket endpoint for conversation events.
- `v1.5.5-dev`: Add dashboard live log panel.
- `v1.5.6-dev`: Add dashboard streaming conversation updates.
- `v1.5.7-dev`: Add reconnect, backoff, and polling fallback behavior.

Exit criteria:

- Logs appear live in the dashboard.
- Conversation responses stream or update without manual refresh.
- Transport failures do not activate ECHO.

### v1.7.0 - Plugin and Store Runtime

Goal: make plugins and store entries installable resources.

Patch steps:

- `v1.6.1-dev`: Add manifest parser and validator for channels, models, skills, and MCP plugins.
- `v1.6.2-dev`: Add plugin install/enable/disable/uninstall APIs.
- `v1.6.3-dev`: Add store registry sync from local `store/registry.yml`.
- `v1.6.4-dev`: Add bundle validation and install flow.
- `v1.6.5-dev`: Add plugin stats tracking.
- `v1.6.6-dev`: Add dashboard plugin/store management views.
- `v1.6.7-dev`: Add safety rules for community plugin install prompts.

Exit criteria:

- Operator can install a local plugin manifest.
- Operator can enable or disable installed plugins.
- Store entries are queryable through backend APIs.

### v1.8.0 - Dashboard Management

Goal: turn the dashboard from status panels into the primary operator interface.

Patch steps:

- `v1.7.1-dev`: Add routing, state management, and API error handling structure.
- `v1.7.2-dev`: Add authenticated layout and role-aware navigation.
- `v1.7.3-dev`: Add providers screen.
- `v1.7.4-dev`: Add conversations screen.
- `v1.7.5-dev`: Add agents and teams screen.
- `v1.7.6-dev`: Add plugins and store screens.
- `v1.7.7-dev`: Add logs and observability screen.
- `v1.7.8-dev`: Add settings and user management screens.

Exit criteria:

- Core V2 APIs have dashboard workflows.
- Dashboard handles loading, empty, error, and unauthorized states.
- Dashboard remains usable on desktop and mobile widths.

### v1.9.0 - CLI Runtime

Goal: implement the Go CLI described in the v0 docs.

Patch steps:

- `v1.8.1-dev`: Add Go module and command shell.
- `v1.8.2-dev`: Add auth login/logout/session commands.
- `v1.8.3-dev`: Add health, logs, agents, providers, and plugin commands.
- `v1.8.4-dev`: Add conversation send and stream commands.
- `v1.8.5-dev`: Add Bubble Tea TUI overview.
- `v1.8.6-dev`: Add config profile handling.

Exit criteria:

- CLI can connect to local backend.
- CLI can inspect health, logs, agents, providers, and conversations.
- CLI can send a message through the chat runtime.

### v1.10.0 - Release Hardening

Goal: make `v2.0.0` safe to tag as the first operational platform release.

Patch steps:

- `v1.9.1-dev`: Add backend unit and integration tests for auth, providers, chat, logs, and plugins.
- `v1.9.2-dev`: Add frontend test/build CI.
- `v1.9.3-dev`: Add migration validation CI.
- `v1.9.4-dev`: Add Compose smoke test workflow.
- `v1.9.5-dev`: Update docs to match implemented behavior.
- `v1.9.6-dev`: Add release notes with all V2 changelog entries.
- `v1.9.7-dev`: Tag and release `v2.0.0`.

Exit criteria:

- Fresh clone can start the V2 platform with Docker Compose.
- Auth, model call, conversation persistence, realtime logs, plugin registry, and dashboard management are verified.
- `v2.0.0` release notes are complete.

---

## 7. First Implementation Order

1. Persistence service boundaries and API error model.
2. Authentication and role enforcement.
3. Provider registry and Ollama execution.
4. Conversation/message runtime.
5. Main Agent orchestration.
6. Realtime event delivery.
7. Plugin/store runtime.
8. Dashboard workflows.
9. CLI.
10. Release hardening.

This order keeps backend contracts ahead of dashboard expansion and prevents UI work from becoming static mock data.

---

## 8. V2 Completion Definition

V2 is complete when:

- A user can log in.
- A user can configure at least one model provider.
- A user can create a conversation and receive an assistant response.
- The Main Agent can assemble and use its identity files in the runtime prompt.
- Logs and conversation events stream live to the dashboard.
- Plugins can be registered, validated, enabled, disabled, and listed.
- Store entries can be viewed and installed through backend-managed flows.
- The dashboard can operate the implemented platform without direct database access.
- The CLI can inspect and use the core platform APIs.
- Docker Compose starts the full platform from a clean checkout.
