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

10. [VOICE_AUDIO_MULTIMODAL.md](./VOICE_AUDIO_MULTIMODAL.md)
    - voice conversation interface (WebRTC)
    - text-to-speech and speech-to-text pipelines
    - multi-modal image analysis
    - document processing (PDF, DOCX)
    - vision-enhanced agent memory

11. [WORKFLOW_AUTOMATION.md](./WORKFLOW_AUTOMATION.md)
    - visual workflow builder
    - scheduled task engine and cron
    - webhook trigger system
    - cross-agent workflow coordination
    - workflow templates library
    - failure handling and resumption

12. [DEVELOPER_EXPERIENCE.md](./DEVELOPER_EXPERIENCE.md)
    - official multi-language SDKs
    - GraphQL API surface
    - agent debugging and telemetry
    - local development mode
    - cost usage dashboard
    - resource optimization recommendations
    - backup and restore CLI

13. [SECURITY_COMPLIANCE_ADVANCED.md](./SECURITY_COMPLIANCE_ADVANCED.md)
    - zero-trust authentication (hardware keys, biometrics)
    - fine-grained RBAC
    - data residency controls
    - automated compliance reporting
    - audit log streaming
    - federated architecture
    - agent-to-agent protocol (A2A)
    - edge deployment profile
    - emergency agent freeze

14. [INFRASTRUCTURE_INTEGRATION_PLUGINS.md](./INFRASTRUCTURE_INTEGRATION_PLUGINS.md)
    - new plugin type: Integration
    - Proxmox VE integration (VM/CT management)
    - Linux system integration (process, services, logs)
    - Docker integration (container lifecycle)
    - Traefik integration (routing, certificates)
    - Nginx Proxy Manager integration
    - Kubernetes integration (pods, deployments)
    - Cloud provider integration (AWS, GCP, Azure)

15. [OIDC_SSO_INTEGRATION.md](./OIDC_SSO_INTEGRATION.md)
    - OIDC provider framework (pluggable architecture)
    - PocketId integration (first post-release priority)
    - Authentik integration
    - Auto-user provisioning from OIDC claims
    - OIDC session management
    - Multi-OIDC provider support

16. [CONTAINERIZED_AGENTS.md](./CONTAINERIZED_AGENTS.md)
    - containerized agent runtime (per-agent containers)
    - global shared storage for skills/plugins
    - agent registry and discovery
    - inter-agent communication bus
    - shared skill and plugin libraries
    - agent resource quotas
    - distributed agent memory
    - container orchestration layer

---

## Roadmap Promotion Notes

- Items that become near-term actionable are promoted into `docs/roadmaps/SYNAPSE_V3_IMPLEMENTATION_ROADMAP.md` as milestone sub-steps.
- Promoted items should be removed or reduced in ideas docs to avoid duplicated source-of-truth.
