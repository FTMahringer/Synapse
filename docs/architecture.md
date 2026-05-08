# SYNAPSE Architecture

## System Overview

SYNAPSE is a self-hosted, fully extensible AI platform designed to operate as an **AI operating system**: a unified runtime in which language models, channels, agent teams, and plugins are first-class citizens that you install, configure, and orchestrate through a single interface. Rather than wrapping a single model behind a chat UI, SYNAPSE gives every AI component its own identity, memory, and role, and routes work between them the same way an operating system routes work between processes.

The central metaphor is deliberate. Just as an OS abstracts hardware and manages resources on behalf of applications, SYNAPSE abstracts model APIs and manages context, memory, and coordination on behalf of agents. Plugins extend the runtime the same way kernel modules extend an OS — they hook into well-defined lifecycle events and expose capabilities without requiring changes to the core.

Everything in SYNAPSE is configurable through a manifest, a YAML file, or the Main Agent's chat interface. No component has its behaviour hard-coded for a specific model provider or channel. A {SYSTEM_NAME} instance can run entirely on local models and local channels, or it can connect to cloud APIs, SaaS communication tools, and external project boards simultaneously.

---

## System Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│                          USER / OPERATOR                            │
└────────────────┬────────────────────────────────────────────────────┘
                 │  interacts via channels (Telegram, Web, CLI, …)
                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  INTERFACE LAYER  (Steel Blue #7B9FE0)              │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                     MAIN AGENT                              │   │
│   │  • single point of contact for the user                    │   │
│   │  • owns the conversation context and session state         │   │
│   │  • routes work to teams, firm, or handles it directly      │   │
│   │  • manages plugins, agents, teams through chat commands    │   │
│   └──────────────────────────┬──────────────────────────────────┘  │
│                              │                                      │
│   ┌──────────────────────────▼──────────────────────────────────┐   │
│   │                     CHANNELS                                │   │
│   │  Telegram · Web UI · Discord · Slack · XMPP · CLI · …      │   │
│   │  Each channel is a plugin; all channel events route to the  │   │
│   │  Main Agent via the internal message bus                   │   │
│   └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                 │
                 │  Main Agent delegates structured project work
                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│               MANAGEMENT LAYER  (Violet #B07FE8)  [optional, max 1] │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                      AI-FIRM                                │   │
│   │  • one CEO agent at the top                                 │   │
│   │  • optional direct sub-agents (requirements, alignment, …) │   │
│   │  • manages its own internal teams                          │   │
│   │  • board integration (GitLab / GitHub / Forgejo / none)    │   │
│   │  • never talks to the user directly                        │   │
│   └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                 │
                 │  Main Agent (or Firm CEO) delegates task work
                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  TEAMS LAYER  (Copper #E07B5A)  [optional, N teams]  │
│                                                                     │
│   ┌────────────────┐  ┌────────────────┐  ┌────────────────┐        │
│   │   Dev-Team     │  │ Research-Team  │  │  Support-Team  │  …     │
│   │  leader        │  │  leader        │  │  leader        │        │
│   │  backend-dev   │  │  researcher    │  │  support-agent │        │
│   │  frontend-dev  │  │  summariser    │  │  escalation    │        │
│   │  devops        │  │                │  │                │        │
│   └────────────────┘  └────────────────┘  └────────────────┘        │
│                                                                     │
│  Each team: leader orchestrates members, reports upward only.       │
│  Members are not directly accessible to the user by default.       │
└─────────────────────────────────────────────────────────────────────┘
                 │
                 │  fallback / debug path only
                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│               FALLBACK LAYER  (Green #4CAF87)  [debug only]         │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                        ECHO                                 │   │
│   │  • mirrors everything it receives back verbatim             │   │
│   │  • used to test channel plumbing and routing logic          │   │
│   │  • never connected to a real model                         │   │
│   │  • disabled in production profiles                         │   │
│   └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Component Overview

| Component | Role | Technology |
|-----------|------|------------|
| Main Agent | Central conversational agent; routes all work | Java 25 + Spring AI, Virtual Threads |
| AI-Firm (optional) | Strategic project management layer with CEO | Java agent runtime |
| Agent Teams | Specialist squads with leader + members | Java agent runtime |
| ECHO | Debug mirror agent | Java, no model call |
| Backend API | REST + WebSocket server | Spring Boot 4.x |
| Message Bus | Internal async routing between agents | Redis Streams |
| Task Queue | Background job execution | Redis Lists + pub/sub |
| Primary DB | Persistent state, history, config | PostgreSQL 18+ |
| Cache | Session state, hot config, rate limits | Redis |
| Web Dashboard | Admin UI and chat interface | Vue 3 + Vite + Pinia |
| CLI | Terminal interface and management commands | Go + Bubble Tea |
| Plugin Registry | Discovery, versioning, activation tracking | PostgreSQL + filesystem |
| Channel Plugins | Adapters for external messaging platforms | Plugin JARs / scripts |
| Model Plugins | Adapters for AI model providers | Plugin JARs / scripts |
| Skill Plugins | Callable capabilities (web search, code exec, …) | Plugin JARs / scripts |
| MCP Plugins | Model Context Protocol server integrations | Plugin JARs / scripts |

---

## Data Flow Diagrams

### Message Flow (User sends a message)

```
User
 │
 │  raw message (text, file, command)
 ▼
Channel Plugin
 │  normalises to ChannelEvent{source, user_id, content, attachments}
 │
 ▼
Redis Stream: channel.inbound
 │
 ▼
Backend MessageRouter
 │  resolves session, attaches conversation context
 │
 ▼
Main Agent
 │  decides: handle directly, delegate to team, delegate to firm
 ├──[direct]──────────────────────────────────────────────────────┐
 │                                                                 │
 ├──[team]────► Agent Team Leader                                 │
 │              │  breaks into subtasks, assigns members          │
 │              ├──► Member A ──► result                          │
 │              ├──► Member B ──► result                          │
 │              └──► aggregated ──► Main Agent ──────────────────┤
 │                                                                 │
 ├──[firm]────► AI-Firm CEO                                       │
 │              │  creates project, assigns teams                 │
 │              ├──► Team X ──► Team Y ──► aggregated            │
 │              └──► final report ──► Main Agent ────────────────┤
 │                                                                 │
 ▼                                                                 │
Model Plugin ◄───────────────────────────────────────────────────┘
 │  sends prompt, streams tokens
 ▼
Response assembled
 │
 ▼
Channel Plugin (outbound)
 │  formats for target platform
 ▼
User receives reply
```

### Plugin Install Flow

```
User / CLI / Dashboard
 │
 │  install request (name, source URL, or local path)
 ▼
Plugin Manager (backend)
 │
 ├──[1] Fetch manifest.yml from source
 │       validate schema, semver, synapse_version range
 │
 ├──[2] Dependency check
 │       verify required plugins/permissions are available
 │
 ├──[3] Download / copy plugin artefacts to plugins/ directory
 │
 ├──[4] Fire on_install hook in plugin
 │       plugin performs first-time setup (create tables, mkdir, …)
 │
 ├──[5] Persist plugin record to PostgreSQL
 │       (id, name, version, type, status=installed, config_schema)
 │
 ├──[6] Emit PLUGIN:INSTALL event to event log
 │
 └──[7] Return config schema to caller
         caller fills required fields → POST /api/plugins/:id/configure
         → on_start hook fires → status = active
```

### Learning Loop Flow

```
Agent completes a task
 │
 ├──[1] Task result + context written to working memory
 │
 ├──[2] If reflect_after_tasks threshold reached:
 │       Reflection model reads recent task log
 │       Generates candidate soul.md / identity.md updates
 │
 ├──[3] update_soul = false (default):
 │       Reflection stored in memory only
 │       soul.md unchanged — user consent required
 │
 ├──[3b] update_soul = true (user opted in):
 │        Candidate diff shown to user for approval
 │        On approval → soul.md updated, version bumped
 │        On rejection → diff discarded, memory cleared
 │
 └──[4] AGENT:REFLECT event emitted to event log
```

---

## Layer Descriptions

### Interface Layer — Main Agent + Channels (Steel Blue #7B9FE0)

The Interface Layer is the user-facing surface of {SYSTEM_NAME}. It consists of the Main Agent and all active channel plugins.

**Channels** are plugins that connect external communication platforms to the internal message bus. They receive raw events from the outside world (a Telegram message, a Discord slash command, a web chat submission), normalise them into a `ChannelEvent` object, and publish them to the `channel.inbound` Redis stream. On the outbound path they format the assembled response and deliver it back to the originating platform. Channels fire `on_message` hooks when a message arrives and `on_send` hooks before delivery.

**The Main Agent** is the single conversational entry point. It holds the user's session state, maintains conversation context in working memory, and makes the routing decision for every inbound message: handle it directly with a model call, delegate it to an Agent Team, or escalate it to the AI-Firm. It also serves as the operator interface — the user talks to the Main Agent to install plugins, create agents, manage teams, and query system status. The Main Agent is always online while {SYSTEM_NAME} is running.

### Management Layer — AI-Firm (Violet #B07FE8)

The Management Layer is optional and limited to exactly one instance per {SYSTEM_NAME} deployment. It provides structured project management for complex multi-team work.

The AI-Firm is headed by a CEO agent that receives high-level project briefs from the Main Agent, decomposes them into sized tasks (xs / s / m / l / xl), assigns tasks to internal teams, tracks progress through a board integration, and returns a final report to the Main Agent when the project is complete. The CEO never communicates directly with the user. Sub-agents (such as a requirements engineer or alignment agent) can assist the CEO in the decomposition and scoping phase.

### Teams Layer — Agent Teams (Copper #E07B5A)

The Teams Layer can contain any number of Agent Teams. Each team has a leader agent and any number of member agents. The leader receives work from above (Main Agent or Firm CEO), breaks it into subtasks, assigns each subtask to a suitable member, waits for results, aggregates them, and reports back up the chain. Team members are specialist agents; by default they are not directly accessible to the user.

### Fallback Layer — ECHO (Green #4CAF87)

ECHO is a debug-only agent that echoes every message it receives back to the sender without making any model call. It is used during development to verify that channel plumbing, routing logic, and session handling work correctly without consuming API credits or requiring a running model. ECHO should be disabled in any production or staging profile.

---

## Backend Architecture

### Spring Boot 4.x

The backend is a Spring Boot 4.x application. All HTTP endpoints are exposed as standard Spring MVC controllers annotated with `@RestController`. The application starts on a configurable port (default 8080). WebSocket connections for the dashboard are handled via Spring's `@MessageMapping` / STOMP infrastructure.

Long-lived agent loops, background reflection tasks, heartbeats, and plugin hook calls all run on **Virtual Threads** (Project Loom). Each Virtual Thread is lightweight enough to block on I/O without holding a platform thread, eliminating the need for reactive callback chains in agent code. The executor is configured as:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

### Redis

Redis serves three distinct purposes:

| Purpose | Mechanism | Key prefix |
|---------|-----------|------------|
| Internal message bus | Redis Streams | `stream:channel.inbound`, `stream:agent.{id}` |
| Task queue | Redis Lists (BRPOP) | `queue:tasks` |
| Cache | Redis Strings / Hashes | `cache:session:{id}`, `cache:config:{key}` |

Agents subscribe to their own stream at startup. The `MessageRouter` publishes inbound channel events to `stream:channel.inbound` and the Main Agent consumes them. When the Main Agent delegates to a team, it publishes to `stream:agent.{team_leader_id}`.

### PostgreSQL 18+

PostgreSQL is the single source of truth for all persistent state:

| Schema | Tables | Purpose |
|--------|--------|---------|
| `synapse` | `agents`, `agent_configs` | Agent registry and live config |
| `synapse` | `plugins`, `plugin_configs` | Plugin registry |
| `synapse` | `conversations`, `messages` | Conversation history |
| `synapse` | `teams`, `team_members` | Team definitions |
| `synapse` | `projects`, `tasks` | AI-Firm project tracking |
| `synapse` | `event_log` | Structured audit / debug log |
| `synapse` | `users`, `roles` | Operator accounts and RBAC |

---

## Frontend Architecture

The web dashboard is a **Vue 3** single-page application built with **Vite**. State is managed by **Pinia** stores (one store per major domain: agents, plugins, teams, conversations, system). Real-time updates are delivered over a **WebSocket** connection (STOMP over SockJS) established on login. All API calls go through a typed Axios client generated from the OpenAPI schema of the backend.

Key views:
- **Chat** — conversation interface with the Main Agent, rendered markdown, file upload
- **Agents** — list, detail, create / edit, status indicators per layer colour
- **Teams** — team list, member roster, routing configuration
- **Firm** — CEO status, active projects, task board view
- **Plugins** — installed plugins, store/registry browser, configure panel
- **Settings** — system config, security, backup

---

## CLI Architecture

The CLI is a Go binary built with **Bubble Tea** for the TUI layer. It communicates exclusively with the backend REST API and WebSocket endpoint; it does not touch the database or filesystem directly (except for `synapse init` during first-time setup).

Commands follow the pattern `synapse <resource> <verb> [flags]`:

```
synapse agents list
synapse agents reload
synapse plugins install <name>
synapse plugins reload
synapse teams new
synapse teams reload
synapse firma reload
synapse logs tail [--category AGENT] [--level DEBUG]
```

Interactive commands (wizards) use the Bubble Tea TUI for form rendering. Non-interactive commands print structured output (table or JSON via `--output json`).

---

## Plugin Lifecycle

```
DISCOVERY
  filesystem scan or registry query
  manifest.yml parsed and validated
      │
      ▼
INSTALL
  artefacts copied to plugins/ directory
  on_install hook fires
  DB record created (status = installed)
  PLUGIN:INSTALL logged
      │
      ▼
CONFIGURE
  config schema presented to operator
  required fields filled (secrets encrypted at rest)
  DB record updated with config values
      │
      ▼
ACTIVATE
  on_start hook fires
  status = active
  PLUGIN:ENABLE logged
      │
      ▼
USE
  on_message / on_send / on_request / on_response hooks fire
  per invocation
      │
      ▼
DEACTIVATE (graceful stop)
  on_stop hook fires
  status = installed
  PLUGIN:DISABLE logged
      │
      ▼
UNINSTALL
  on_uninstall hook fires
  artefacts removed from filesystem
  DB record deleted
  PLUGIN:UNINSTALL logged
```

---

## Security Model

### Authentication

All operator access to the backend API and dashboard requires a **JWT Bearer token** obtained by `POST /api/auth/login`. Tokens are short-lived (configurable, default 1 h) and refreshed via a refresh-token rotation scheme. The CLI stores tokens in the OS credential store.

### Authorisation

Role-based access control is enforced at the controller layer via Spring Security. Built-in roles:

| Role | Permissions |
|------|-------------|
| `ADMIN` | Full access — install plugins, create agents, view all logs |
| `OPERATOR` | Chat, view agents and plugins, cannot install or delete |
| `READONLY` | View dashboards and logs only |

Custom roles can be defined and assigned per operator account.

### Credential Encryption

Plugin configuration values marked `secret: true` in the config schema are encrypted with AES-256-GCM before being written to PostgreSQL. The encryption key is derived from a master secret set at install time and never stored in the database. Secret values are never returned in API responses — only a masked placeholder is returned.

### Plugin Permissions

Each plugin declares the permissions it requires in `manifest.yml` under `permissions`. The operator must accept the listed permissions at install time. Plugins that attempt to use undeclared permissions are rejected at runtime and PLUGIN:SECURITY_VIOLATION is logged.

---

## Deployment

### Docker Compose (Recommended for Quick Start)

The repository ships a `docker-compose.yml` that starts all required services:

```
synapse-backend    — Spring Boot 4.x JAR
synapse-frontend   — Nginx serving Vue 3 SPA
synapse-postgres   — PostgreSQL 18+
synapse-redis      — Redis (latest stable)
```

Run with:

```bash
docker compose up -d
```

Configuration is supplied through `.env` (copy `.env.example` and fill in secrets). The backend performs schema migrations on startup via Flyway.

### Bare Metal (Development)

For local development:

1. Start PostgreSQL 18+ and Redis on their default ports.
2. Copy `backend/src/main/resources/application-dev.yml.example` to `application-dev.yml` and fill in connection details.
3. Run `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` from `backend/`.
4. Run `npm install && npm run dev` from `frontend/` (Vite dev server, hot-reload).
5. Build the CLI with `go build -o synapse ./cmd/synapse` from `cli/`.

Data directories used by SYNAPSE at runtime:

| Path | Content |
|------|---------|
| `agents/` | Agent identity files (identity.md, soul.md, connections.md, config.yml) |
| `agents/teams/` | Team definitions (team.yml + agent subdirs) |
| `agents/ai-firm/` | Firm definition (firm.yml + CEO + sub-agents) |
| `plugins/` | Installed plugin artefacts |
| `plugins/store/` | Plugin registry cache |
| `logs/` | Structured event log output |
