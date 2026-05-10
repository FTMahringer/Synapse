# API & Runtime

**Status**: Designed (detailed specs in `/docs/`)  
**Target**: v2.3.0 – v2.5.0

---

## REST API & WebSocket API

**Spec**: [`/docs/api-reference.md`](/docs/api-reference.md)  
**Priority**: High

Full REST, WebSocket, SSE, and CLI surface for SYNAPSE.

### Key Endpoint Groups

| Group | Transport | Purpose |
|---|---|---|
| `/api/agents` | REST | CRUD for agents and teams |
| `/api/conversations` | REST + WebSocket | Conversation management, streaming |
| `/api/tasks` | REST | Task creation, status, results |
| `/api/plugins` | REST | Plugin install, list, remove |
| `/api/health` | REST | Health check (used by Docker, CI) |
| `/api/metrics` | REST | Prometheus metrics (v2.1.1-dev) |
| `/ws/chat` | WebSocket | Real-time chat streaming |
| `/sse/events` | SSE | Dashboard live updates |

### Auth

JWT bearer tokens. Tokens issued via `/api/auth/login`. API key auth for programmatic access (System Settings → API Keys).

---

## Chat Runtime API

**Spec**: [`/docs/chat-api.md`](/docs/chat-api.md)  
**Priority**: High

Conversation management, message streaming, and session lifecycle via REST + WebSocket.

### Key Operations

- `POST /api/conversations` — Create conversation (optionally assign to agent/team)
- `POST /api/conversations/{id}/messages` — Send message, returns streamed response
- `GET /api/conversations/{id}/messages` — Fetch conversation history
- `DELETE /api/conversations/{id}` — Archive/delete conversation
- `WebSocket /ws/chat/{id}` — Real-time streaming with heartbeat

### Streaming Format

Server-sent events with `data:` prefix, `[DONE]` sentinel at end. Compatible with OpenAI streaming format for client library reuse.

---

## CLI Reference

**Spec**: [`/docs/cli-reference.md`](/docs/cli-reference.md)  
**Priority**: Medium

Go command-line application with optional Bubble Tea TUI for interactive workflows.

### Principles

- Single binary, no daemon required for CLI-only use
- All dashboard operations available via CLI
- Machine-readable output via `--json` flag
- Config file: `~/.synapse/config.yml`

### Key Commands

| Command | Purpose |
|---|---|
| `synapse start` | Start all services |
| `synapse stop` | Stop all services |
| `synapse status` | Show service health |
| `synapse agents list` | List agents |
| `synapse agents new` | Create agent interactively |
| `synapse plugins install {id}` | Install plugin |
| `synapse logs {service}` | Stream logs |
| `synapse reload` | Hot-reload agents and plugins |

---

## Runtime Delivery

**Spec**: [`/docs/runtime-delivery.md`](/docs/runtime-delivery.md)  
**Priority**: High

How users and operators see the live system: chat streams, logs, heartbeat state, dashboard updates, and learning-loop feedback.

### Channels

| Channel | Transport | Consumer |
|---|---|---|
| Chat stream | WebSocket / SSE | Dashboard, CLI, API clients |
| Log stream | SSE | Dashboard log panel |
| Heartbeat state | SSE | Dashboard agent status |
| Dashboard updates | SSE | Dashboard live panels |
| Learning-loop feedback | In-app notification | Dashboard notification center |

### Key Points

- All streaming endpoints require authentication
- Log stream is filterable by category (`AGENT`, `MEMORY`, `LEARNING`, etc.)
- Dashboard receives unified event bus — single SSE connection for all live updates
- CLI can stream any channel via `synapse logs --follow`
