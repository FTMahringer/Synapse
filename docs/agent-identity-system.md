# Agent Identity System

Every agent is defined by files, not hidden runtime state. The required files are `identity.md`, `soul.md`, `connections.md`, and `config.yml`.

## Required Files

| File | Purpose |
|---|---|
| `identity.md` | Role, capabilities, limitations, personality, activation |
| `soul.md` | Values, behavioral rules, communication style, preferences, growth notes |
| `connections.md` | Receives from, sends to, escalation path, blocked communication |
| `config.yml` | Model, memory, heartbeat, skills, MCP, learning settings |

## Main Agent Path

The user runs `/agents new`. The Main Agent asks for name, role, model, capabilities, limitations, personality, activation, routing, and memory preferences. It shows a summary before writing files, creates the agent directory from `agents/_templates/agent/`, validates all required sections, registers the agent in the database, and logs `agent.created`.

## Manual Path

Create `agents/<agent-id>/`, copy the four template files, replace all template values, then run `synapse reload` or use Dashboard agent reload. The runtime validates the folder, checks `config.yml`, and registers the agent only if all required files are present.

## Logging

Agent creation, validation, config reload, enable, disable, and delete operations use the `AGENT` category. Message traffic uses `AGENT_MESSAGE`. Team routing uses `AGENT_TEAM`.
