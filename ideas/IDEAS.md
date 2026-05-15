# SYNAPSE Ideas Index

This file is a compact index for active idea domains. Detailed content is maintained in focused files.

---

## Core Idea Domains

1. [AGENT_AND_MEMORY.md](./AGENT_AND_MEMORY.md)
   - agent behavior and orchestration
   - memory lifecycle and self-learning direction
   - token-efficiency skill strategy linkage

2. [RUNTIME_AND_INTEGRATIONS.md](./RUNTIME_AND_INTEGRATIONS.md)
   - runtime delivery and API direction
   - observability/monitoring evolution
   - MCP/ACP/Git/skills integration strategy

3. [PLATFORM_AND_ECOSYSTEM.md](./PLATFORM_AND_ECOSYSTEM.md)
   - platform-level features and long-term backlog
   - plugin ecosystem and Java-first strategy
   - product expansion themes (v3+)

4. [BUILTIN_SKILLS_STRATEGY.md](./BUILTIN_SKILLS_STRATEGY.md)
   - hybrid built-in skill model
   - forced core vs TUI-selectable built-ins

5. [RULES_SYSTEM.md](./RULES_SYSTEM.md)
   - central rules/policy precedence
   - admin-approved override workflow

6. [ADMIN_REQUEST_NOTIFICATION_CHANNEL.md](./ADMIN_REQUEST_NOTIFICATION_CHANNEL.md)
   - user→admin privileged request lifecycle
   - centralized approval notifications and auditability

7. [DEVICE_TRUST_AND_FIRST_BOOT_SECURITY.md](./DEVICE_TRUST_AND_FIRST_BOOT_SECURITY.md)
   - first-boot admin hardening and default-credential warning
   - admin-approved device trust gate for Web UI access
   - first local (TUI/sudo) approval, then admin-UI approvals

8. [ADMIN_DEBUG_COMMAND_FAMILY.md](./ADMIN_DEBUG_COMMAND_FAMILY.md)
   - admin-only debug command namespace (`/debug ...`)
   - approval-gated recovery actions (including password reset workflows)
   - optional Redis/Postgres inspection integrations

9. [AGENT_SELF_HOSTED_PLATFORM.md](./AGENT_SELF_HOSTED_PLATFORM.md)
   - agent control plane for multi-agent orchestration
   - stateful graph-based runtime (LangGraph-style)
   - local model router for hybrid deployments
   - agentic SIEM for security monitoring
   - infrastructure agent copilot for platform engineering

---

## Roadmap Promotion Notes

- Items that become near-term actionable are promoted into `docs/roadmaps/SYNAPSE_V3_IMPLEMENTATION_ROADMAP.md` as milestone sub-steps.
- Promoted items should be removed or reduced in ideas docs to avoid duplicated source-of-truth.
