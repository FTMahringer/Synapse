# Runtime & Integrations Ideas

**Status**: Mixed (implemented foundations + roadmap backlog)  
**Primary Roadmap Window**: v2.2.0 – v2.8.0

---

## Scope

This file consolidates API/runtime delivery, observability direction, and external integration strategy.

---

## API & Runtime Delivery

### REST / WebSocket / SSE Surface

**Status**: Implemented foundation  
**Specs**: [`/docs/api-reference.md`](/docs/api-reference.md), [`/docs/chat-api.md`](/docs/chat-api.md), [`/docs/runtime-delivery.md`](/docs/runtime-delivery.md)

- Core CRUD and orchestration endpoints
- Streaming channels for chat/logs/dashboard events
- Authenticated runtime delivery across dashboard and CLI

### CLI Runtime

**Status**: Implemented foundation  
**Spec**: [`/docs/cli-reference.md`](/docs/cli-reference.md)

- Operator-facing runtime control
- JSON output support
- Hot-reload and operational command set

---

## Observability & Monitoring

### Observability Milestone

**Status**: Completed milestone with continued hardening  
**Roadmap**: v2.2.0 (already shipped)

- Metrics/tracing/health/logging foundations delivered
- Remaining work is operational hardening, not milestone recovery

### Monitoring Stack Operations

**Status**: Ongoing operational evolution  

- Prometheus/Grafana/Loki/Alertmanager baseline
- Dashboard and runbook quality improvements
- Environment-specific alert tuning

---

## Integrations

### MCP Integration

**Status**: Designed  
**Spec**: [`/docs/mcp-integration.md`](/docs/mcp-integration.md)

- stdio/HTTP MCP servers
- lifecycle handling + tool discovery
- logging and isolation boundaries

### ACP Registry

**Status**: Designed  
**Spec**: [`/docs/acp-registry.md`](/docs/acp-registry.md)

- provider discovery and credential-mode abstraction
- startup cache of registry metadata

### Git Provider Integration

**Status**: Designed  
**Spec**: [`/docs/git-provider-integration.md`](/docs/git-provider-integration.md)

- GitHub/GitLab/Forgejo/Bitbucket workflows
- issue/PR/pipeline interaction model

### Skills Integration

**Status**: Designed + partially implemented runtime hooks  
**Spec**: [`/docs/skills-integration.md`](/docs/skills-integration.md)

- store/local/skills.sh sourcing model
- explicit consent for publishing
- reusable markdown skill lifecycle

