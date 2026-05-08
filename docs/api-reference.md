# API Reference

This reference defines the initial REST, WebSocket, SSE, and CLI surface for {SYSTEM_NAME}.

## REST Endpoints

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/health` | System health |
| `GET` | `/api/system/metadata` | System name, version, settings |
| `GET` | `/api/agents` | List agents |
| `POST` | `/api/agents` | Create agent |
| `GET` | `/api/agents/{id}` | Agent detail |
| `PATCH` | `/api/agents/{id}` | Update agent config |
| `GET` | `/api/teams` | List teams |
| `POST` | `/api/teams` | Create team |
| `GET` | `/api/firm` | AI-Firm status |
| `POST` | `/api/firm` | Create AI-Firm |
| `GET` | `/api/plugins` | List installed plugins |
| `POST` | `/api/plugins/install` | Install plugin |
| `GET` | `/api/store` | Browse store |
| `POST` | `/api/store/bundles` | Create bundle submission |
| `GET` | `/api/logs` | Query logs |
| `GET` | `/api/users` | List users |
| `POST` | `/api/auth/login` | Login |
| `POST` | `/api/auth/logout` | Logout |

## Realtime

| Endpoint | Purpose |
|---|---|
| `WebSocket /ws/chat` | Agent chat stream |
| `WebSocket /ws/logs` | Live log stream |
| `WebSocket /ws/heartbeat` | Agent heartbeat status |
| `SSE /sse/logs` | Log fallback |

## CLI

| Command | Purpose |
|---|---|
| `synapse chat` | Chat with Main Agent |
| `synapse agents list` | List agents |
| `synapse agents new` | Guided agent creation |
| `synapse teams new` | Guided team creation |
| `synapse install <plugin>` | Install plugin |
| `synapse store` | Open store TUI |
| `synapse logs` | Live log tail |
| `synapse echo` | Start ECHO manually |
| `synapse git connect <provider>` | Connect git provider |

## Main Agent Path

The Main Agent calls these APIs through internal services and always logs mutating operations.

## Manual Path

Operators can call REST endpoints directly with authenticated tokens or use the CLI. Mutating calls require OWNER, ADMIN, or resource ownership depending on endpoint.
