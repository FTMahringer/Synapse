# Observability

**Status**: In progress (v2.1.1-dev done, v2.1.2–v2.2.0 planned)  
**Target**: v2.2.0  
**Roadmap**: v2.2.0 — Observability & Monitoring

---

## Metrics Infrastructure (v2.1.1-dev ✅)

Prometheus metrics endpoint at `/api/metrics`. Spring Boot Actuator + Micrometer with custom tags. Circular dependency resolved via `@PostConstruct` pattern.

**Key metrics exposed**:
- JVM metrics (memory, GC, threads)
- HTTP request rates, latency, error rates
- Custom SYNAPSE metrics (active agents, tasks, conversations)

---

## Distributed Tracing (v2.1.2-dev planned)

**Spec**: coming in v2.1.2-dev  
**Priority**: High

OpenTelemetry-based distributed tracing across backend services. Trace IDs propagated through all agent calls, tool calls, and database queries.

**Planned**:
- Jaeger or Tempo as trace backend
- Trace context in all log entries (`trace_id`, `span_id`)
- Agent-to-agent call tracing
- MCP server call spans

---

## Health Checks Enhancement (v2.1.3-dev planned)

**Priority**: Medium

Extended health endpoints beyond the basic `/api/health`:
- Per-service health (database, Redis, Qdrant, Ollama)
- Agent health (last heartbeat, session state)
- Dependency graph health
- Readiness vs liveness probes (Kubernetes-compatible)

---

## Logging System

**Spec**: [`/docs/logging-system.md`](/docs/logging-system.md)  
**Priority**: High

Structured logging across all SYNAPSE operations. Every operational action produces a structured log entry that can be stored, streamed, searched, and exported.

### Log Categories

| Category | Purpose |
|---|---|
| `AGENT` | Agent lifecycle, config reload, enable/disable |
| `AGENT_MESSAGE` | Message traffic between agents |
| `AGENT_TEAM` | Team routing events |
| `MEMORY` | Vault reads, writes, compression |
| `LEARNING` | Self-learning loop steps |
| `MCP` | MCP server startup, tool calls |
| `PLUGIN` | Plugin load, unload, errors |
| `SECURITY` | Auth events, permission denials |
| `SYSTEM` | Startup, shutdown, config changes |

### Log Format

```
{ISO8601} [{CATEGORY}] {event} {key}={value} {key}={value} ...
```

Example:
```
2025-06-12T09:31:44Z [MEMORY] compression-start agent_id=agent_finance_01 token_estimate=41230
```

### Structured Logging (v2.1.4-dev planned)

- JSON output mode for log aggregation (Loki, Elasticsearch)
- Log streaming via SSE to dashboard
- Filterable by category, agent, level, time range
- Export to file or external sink

---

## Monitoring Stack

**Spec**: [`/docs/operations/monitoring.md`](/docs/operations/monitoring.md)  
**Priority**: Medium  
**Target**: v2.2.0

Reference stack for production monitoring:

- **Prometheus** — scrapes `/api/metrics` on configurable interval
- **Grafana** — dashboards for JVM, HTTP, agent activity, task throughput
- **Loki** — log aggregation from structured JSON logs
- **Alertmanager** — alert routing (PagerDuty, Slack, email)

Docker Compose profile `--profile monitoring` brings up the full stack alongside SYNAPSE services.

**Planned dashboards**:
- System overview (JVM, HTTP, errors)
- Agent activity (active sessions, task rates, heartbeat state)
- Memory & learning (vault sizes, compression events, learning cycles)
- Plugin health (MCP server status, tool call rates)
