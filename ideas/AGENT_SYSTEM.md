# Agent System

**Status**: Designed (detailed specs in `/docs/`)  
**Target**: v2.4.0 – v2.5.0

---

## Agent Identity System

**Spec**: [`/docs/agent-identity-system.md`](/docs/agent-identity-system.md)  
**Priority**: High

Every agent is defined by four files on disk — not hidden runtime state:

| File | Purpose |
|---|---|
| `identity.md` | Role, capabilities, limitations, personality, activation |
| `soul.md` | Values, behavioral rules, communication style |
| `connections.md` | Receives from, sends to, escalation path |
| `config.yml` | Model, memory, heartbeat, skills, MCP, learning settings |

Agents are created via `/agents new` (Main Agent walks user through setup) or manually by creating the folder from templates and running `synapse reload`. The runtime validates all required files before registering an agent.

**Key points**:
- File-based: agents can be version-controlled and diff'd
- Validated on load: missing sections block registration
- Lifecycle events logged to `AGENT` category

---

## Agent Teams System

**Spec**: [`/docs/agent-teams-system.md`](/docs/agent-teams-system.md)  
**Priority**: Medium

Teams group a leader and member agents behind a single routing contract. A `team.yml` file describes the team structure, routing rules, and whether users may address the team directly.

- Created via `/teams new` or manually
- Leader has its own `identity.md` and `soul.md`
- Members are normal agents referenced by ID
- Routing enforces the Main Agent bypass rules
- Events logged to `AGENT_TEAM`

---

## AI-Firm System

**Spec**: [`/docs/ai-firm-system.md`](/docs/ai-firm-system.md)  
**Priority**: Low

Optional project-management layer with a single CEO agent. A SYNAPSE instance may have zero or one active firm. The firm coordinates cross-team work at an organizational level.

---

## Heartbeat System

**Spec**: [`/docs/heartbeat-system.md`](/docs/heartbeat-system.md)  
**Priority**: High

Keeps active agent sessions alive and records whether the context cache was preserved between turns. Heartbeats are sent on a configurable interval; missed heartbeats trigger session cleanup.

---

## ECHO Debug Agent

**Spec**: [`/docs/echo-debug-agent.md`](/docs/echo-debug-agent.md)  
**Priority**: Low

ECHO is a manual-only local debug agent. It is minimal, never activated automatically, and exists to let developers inspect routing, tool calls, and message payloads without affecting production agents.
