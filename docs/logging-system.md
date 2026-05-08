# Logging System

Logging is a critical system feature. Every operational action must produce structured logs that can be stored, streamed, searched, and exported.

## Categories

| Category | Events |
|---|---|
| SYSTEM | start, stop, config change, shutdown |
| AUTH | login, logout, token created, permission denied |
| AGENT | start, stop, create, update, task start, task end |
| AGENT_MESSAGE | inbound, outbound, tokens, cost, latency |
| AGENT_TEAM | team create, delegate, blocked, report |
| AI_FIRM | project intake, plan, delegate, complete |
| TASK | project create, task update, task blocked, task done |
| HEARTBEAT | sent, skipped, cache saved, cache missed |
| LEARNING | reflect start, pattern found, skill proposed |
| MEMORY | vault read, vault write, compression, search |
| PLUGIN | install, uninstall, enable, disable, error |
| STORE | browse, download, install request, bundle submit |
| CHANNEL | connect, disconnect, message received, message sent |
| MODEL | request, response, timeout, cost, provider error |
| MCP | server start, tool call, tool response, error |
| GIT | provider connected, repo linked, sync event, error |
| CUSTOM_COMMAND | create, update, invoke, fail |

## JSON Shape

```json
{
  "id": "uuid",
  "timestamp": "ISO-8601",
  "level": "INFO",
  "category": "AGENT_MESSAGE",
  "source": {
    "agent_id": "main-agent",
    "user_id": "user-id",
    "session_id": "session-id"
  },
  "event": "message.sent",
  "payload": {
    "tokens_in": 1200,
    "tokens_out": 400,
    "cost_usd": 0.01,
    "latency_ms": 850,
    "model": "claude-sonnet-4-6",
    "provider": "anthropic"
  },
  "correlation_id": "uuid",
  "trace_id": "uuid"
}
```

## Delivery

Logs are written to PostgreSQL, optionally streamed through Redis Streams, and delivered to the dashboard by WebSocket with SSE fallback.

## Main Agent Path

The user runs `/logs` or asks for diagnostics. The Main Agent filters logs by level, category, agent, session, task, or time range.

## Manual Path

Operators can query `system_logs`, use `synapse logs`, or call `GET /api/logs`.
