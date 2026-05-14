# SYNAPSE Ideas Index (Organized)

> Organized collection of ideas for SYNAPSE AI Platform. Original ideas preserved in `/ideas/` directory.

---

## Categories

### 🧠 Agent & Intelligence
- [Agent & Memory](./ideas/AGENT_AND_MEMORY.md) — Agent behavior, collaboration, memory lifecycle
- [Built-in Skills](./ideas/BUILTIN_SKILLS_STRATEGY.md) — Hybrid core/optional built-in skill model
- [Rules System](./ideas/RULES_SYSTEM.md) — Global + template + agent override policy system

### ⚙️ Runtime & Integrations
- [Runtime & Integrations](./ideas/RUNTIME_AND_INTEGRATIONS.md) — API delivery, observability, MCP/ACP/Git/skills
- [Plugin Publishing](./ideas/PLUGIN_PUBLISHING_AND_MAVEN_REPOSITORIES.md) — Maven repository strategy

### 🌐 Platform & Ecosystem
- [Platform & Ecosystem](./ideas/PLATFORM_AND_ECOSYSTEM.md) — Long-term product extensions, multi-modal, SDKs
- [Plugin Store Website](./ideas/PLUGIN_STORE_WEBSITE.md) — Modrinth-inspired public plugin browsing

### 🔐 Security & Admin
- [Device Trust](./ideas/DEVICE_TRUST_AND_FIRST_BOOT_SECURITY.md) — Device approval, first-boot hardening
- [Admin Debug Commands](./ideas/ADMIN_DEBUG_COMMAND_FAMILY.md) — Secure debug/ops command family
- [Admin Requests](./ideas/ADMIN_REQUEST_NOTIFICATION_CHANNEL.md) — User↔admin request workflow

---

## Roadmap Promotion Status

| Idea | V3 Ready | V4 | Ongoing | Notes |
|------|----------|-----|---------|-------|
| Agent & Memory | ✅ Done | - | - | Memory system shipped in v2.3.3 |
| Built-in Skills | 🔄 Partial | - | ✅ | Core enforced + TUI activation model |
| Rules System | 🔄 Partial | ✅ | ✅ | Global baseline done, overrides pending |
| Runtime/MCP | ✅ Done | - | ✅ | MCP designed, skills integration ongoing |
| Plugin Publishing | 🔄 Partial | ✅ | - | Nexus strategy defined, implementation pending |
| Platform/Ecosystem | - | ✅ | ✅ | Multi-modal, SDKs for v4+ |
| Plugin Store Website | - | ✅ | ✅ | Public browsing, separate from in-app |
| Device Trust | 🔄 Partial | ✅ | - | Bootstrap done, device approval pending |
| Admin Debug | - | ✅ | ✅ | Read-only first, approval-gated later |
| Admin Requests | - | ✅ | ✅ | Central request workflow |

---

## New Versioning (Post-v4.0.0)

Starting from v4.0.0, releases use **year.month.version-type** format:

- `2026.05.1-alpha` — Alpha release
- `2026.06.1-beta` — Beta release  
- `2026.07.1-stable` — Stable release

 VX roadmaps remain for planning; new versioning applies to actual release tags.

---

## Ideas Collected During Development

Additional ideas collected via automated research are stored in:
- `/tmp/synapse-ideas/NEW_IDEAS.md` — Research-generated ideas (until 10:00 collection window)

---

*Last organized: 2026-05-14*