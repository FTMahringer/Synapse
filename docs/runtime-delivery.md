# Runtime Delivery

Runtime delivery describes how users and operators see the live system: chat streams, logs, heartbeat state, dashboard updates, and learning-loop feedback.

## Channels

| Channel | Primary Transport | Fallback |
|---|---|---|
| Chat stream | WebSocket `/ws/chat` | SSE where supported |
| Logs | WebSocket `/ws/logs` | SSE `/sse/logs` |
| Heartbeat status | WebSocket `/ws/heartbeat` | Polling `/api/health` |
| Store updates | REST refresh | Manual refresh |

## Dashboard Blocks

The default dashboard shows agent status, cost today, active projects, and live log feed. Operators can add task boards, model status, heartbeat status, store updates, and git provider panels.

## Main Agent Path

The user asks for status, logs, health, project state, or cost. The Main Agent queries runtime services, summarizes the result, and links to the relevant dashboard view.

## Manual Path

Operators use CLI commands such as `synapse logs`, `synapse health`, `synapse agents list`, and direct API calls. Manual views read from the same log and status sources as the dashboard.

## Failure Rules

- If WebSocket fails, clients fall back to SSE.
- If SSE fails, clients show a stale-state warning and offer manual refresh.
- If Redis Streams are unavailable, logs still persist to PostgreSQL.
- If Qdrant is unavailable, vault search falls back to basic retrieval.
- ECHO is never started automatically as a runtime fallback.

## Logging

Runtime delivery emits `SYSTEM`, `AGENT_MESSAGE`, `HEARTBEAT`, `LEARNING`, and `MEMORY` events depending on the stream.
