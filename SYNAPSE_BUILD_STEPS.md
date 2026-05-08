# SYNAPSE — Build Steps

> Reference: [SYNAPSE_OPUS_PLAN.md](./SYNAPSE_OPUS_PLAN.md)
> No implementation here — pure execution order.

---

## Phase 1 — Project Skeleton

**Step 1: Create repo folder structure**
> Ref: [PART 3 — PROJECT STRUCTURE](./SYNAPSE_OPUS_PLAN.md#part-3--project-structure) (L59–212)
- Create all top-level directories: `packages/`, `backend/`, `agents/`, `plugins/`, `store/`, `docs/`, `installer/`
- Create all subdirectories as shown in the tree

**Step 2: Root files**
> Ref: [PART 5 — Block 6](./SYNAPSE_OPUS_PLAN.md#files-you-create) (L1263–1268)
- Write `README.md` (features, getting started, architecture overview)
- Write `LICENSE`
- Write `CONTRIBUTING.md`

---

## Phase 2 — Database Foundation (CRITICAL)

**Step 3: Write complete PostgreSQL schema**
> Ref: [4.5 Backend — Part 1: SQL Database](./SYNAPSE_OPUS_PLAN.md#part-1-sql-database-postgresql) (L500–551)
- All tables: `system_metadata`, `system_logs`, `agents`, `agent_teams`, `agent_team_members`, `ai_firm`, `plugins`, `installed_channels`, `installed_models`, `installed_skills`, `installed_mcp`, `custom_commands`, `conversations`, `messages`, `projects`, `tasks`, `task_logs`, `agent_costs`, `heartbeat_log`, `sessions`, `store_cache`, `plugin_stats`, `users`, `user_agents`, `user_channels`, `sessions_auth`, `git_providers`, `git_repos`
- File: `backend/db/schema.sql`

**Step 4: Write seed data**
> Ref: [PART 5 — Block 4](./SYNAPSE_OPUS_PLAN.md#block-4--backend-3-files) (L1252–1255)
- Default `system_metadata` row (name = "SYNAPSE", version, created_at)
- Default config entries
- File: `backend/db/seed.sql`

**Step 5: Write vault specification**
> Ref: [4.5 Backend — Part 2: Knowledge Vault](./SYNAPSE_OPUS_PLAN.md#part-2-knowledge-vault-per-agent) (L552–600)
- Full vault folder structure spec
- Compression mechanism docs
- Vector search (Qdrant) integration notes
- File: `backend/vault/VAULT_SPEC.md`

---

## Phase 3 — Agent Identity Files

**Step 6: Write agent template files**
> Ref: [4.1 Agent Identity System](./SYNAPSE_OPUS_PLAN.md#41-agent-identity-system) (L214–336)
- `agents/_templates/agent/identity.md`
- `agents/_templates/agent/soul.md`
- `agents/_templates/agent/connections.md`
- `agents/_templates/agent/config.yml`

**Step 7: Write team template files**
> Ref: [4.2 Agent Teams System](./SYNAPSE_OPUS_PLAN.md#42-agent-teams-system) (L336–390)
- `agents/_templates/team/team.yml`
- `agents/_templates/team/leader/identity.md`
- `agents/_templates/team/leader/soul.md`

**Step 8: Write Main Agent files**
> Ref: [4.4 Main Agent — System Controller](./SYNAPSE_OPUS_PLAN.md#44-main-agent--system-controller) (L435–498)
- `agents/main/identity.md` — calm, direct, competent system controller
- `agents/main/soul.md` — core values, behavioral rules
- `agents/main/connections.md` — receives from all users/channels, sends to all agents
- `agents/main/system-prompt.md` — full system prompt (all commands, modes, logic)
- `agents/main/config.yml` — Anthropic/Sonnet default, memory thresholds, heartbeat

**Step 9: Write ECHO debug agent files**
> Ref: [4.10 ECHO — Debug Agent](./SYNAPSE_OPUS_PLAN.md#410-echo--debug-agent-manual-only) (L742–781)
- `agents/echo/identity.md` — minimalist, reliable, knows it's ECHO
- `agents/echo/soul.md`
- `agents/echo/system-prompt.md` — manual-only, local-only, clear capability list

**Step 10: Write AI-Firm example files**
> Ref: [4.3 AI-Firm System](./SYNAPSE_OPUS_PLAN.md#43-ai-firm-system) (L390–435)
- `agents/ai-firm/firm.yml` — example with Paperclip mode
- `agents/ai-firm/ceo/identity.md`
- `agents/ai-firm/ceo/soul.md`
- `agents/ai-firm/ceo/system-prompt.md`

---

## Phase 4 — Plugin Templates

**Step 11: Write channel plugin template + Telegram**
> Ref: [4.6 Plugin System](./SYNAPSE_OPUS_PLAN.md#46-plugin-system) (L600–652)
- `plugins/channels/_template/manifest.yml` — full manifest spec
- `plugins/channels/_template/Channel.java` — interface skeleton
- `plugins/channels/telegram/manifest.yml` — real Telegram manifest
- `plugins/channels/telegram/TelegramChannel.java` — implementation skeleton

**Step 12: Write model provider templates**
> Ref: [4.6 Plugin System](./SYNAPSE_OPUS_PLAN.md#46-plugin-system) (L600–652) + [PART 6 — TECHNOLOGY STACK](./SYNAPSE_OPUS_PLAN.md#part-6--technology-stack) (L1294–1372)
- `plugins/models/_template/manifest.yml`
- `plugins/models/_template/ModelProvider.java`
- `plugins/models/anthropic/manifest.yml`
- `plugins/models/openai/manifest.yml`
- `plugins/models/deepseek/manifest.yml`
- `plugins/models/ollama/manifest.yml`

**Step 13: Write skills + MCP templates**
> Ref: [4.6 Plugin System](./SYNAPSE_OPUS_PLAN.md#46-plugin-system) (L600–652)
- `plugins/skills/_template/manifest.yml`
- `plugins/skills/_template/skill.md`
- `plugins/mcp/_template/manifest.yml`

---

## Phase 5 — Store System

**Step 14: Write store specification files**
> Ref: [4.11 Store & Bundle System](./SYNAPSE_OPUS_PLAN.md#411-store--bundle-system) (L781–852)
- `store/STORE_SPEC.md` — store types, stats, install flow, filtering
- `store/BUNDLE_SPEC.md` — bundle format, community PR flow, validation
- `store/registry.yml` — example registry structure
- `store/submit-plugin.md` — contributor guide

---

## Phase 6 — Documentation (20 files)

> Ref: [PART 5 — Block 1](./SYNAPSE_OPUS_PLAN.md#files-you-create) (L1194–1215)
> Quality rules apply to ALL docs: no TODOs, no placeholders, English only, always document both Main Agent path + Manual path.
> Ref: [Quality Rules](./SYNAPSE_OPUS_PLAN.md#quality-rules) (L1269–1284)

**Step 15: Architecture doc**
- `docs/architecture.md` — ASCII diagrams, system layers, data flows, component relationships

**Step 16: Plugin system doc**
- `docs/plugin-system.md` — manifest spec, plugin lifecycle, creation paths (guided + manual)

**Step 17: Agent identity system doc**
- `docs/agent-identity-system.md` — identity/soul/connections format, config.yml fields

**Step 18: Agent teams system doc**
- `docs/agent-teams-system.md` — team.yml, leader/member roles, routing, creation options

**Step 19: AI-Firm system doc**
- `docs/ai-firm-system.md` — max-1 rule, CEO structure, firm.yml, Paperclip mode

**Step 20: Memory vault doc**
- `docs/memory-vault.md` — vault structure, compression trigger + flow, Obsidian compatibility, Qdrant optional

**Step 21: Self-learning loop doc**
- `docs/self-learning-loop.md` — 5-step flow, rate limits, skill creation consent, `/skills publish` manual only

**Step 22: Heartbeat system doc**
- `docs/heartbeat-system.md` — purpose, logic, cache TTL, tracking in `heartbeat_log`, dashboard status

**Step 23: Skills integration doc**
- `docs/skills-integration.md` — skills.sh API, Claude Code Skills format, `/skills publish` command flow

**Step 24: MCP integration doc**
- `docs/mcp-integration.md` — MCP JSON-RPC protocol, stdio + HTTP transport, plugin format

**Step 25: ACP registry doc**
- `docs/acp-registry.md` — provider types, subscription_id vs api_key, flow, auto-endpoint setup

**Step 26: Store concept doc**
- `docs/store-concept.md` — 5 store types, plugin stats, install flow, Official vs Community distinction

**Step 27: Bundle system doc**
- `docs/bundle-system.md` — bundle creation UI, PR flow, community repo, CI validation

**Step 28: Multi-user doc**
- `docs/multi-user.md` — 4 roles (OWNER/ADMIN/USER/VIEWER), JWT auth, OAuth, 2FA, per-user resources

**Step 29: Logging system doc**
- `docs/logging-system.md` — all 17 log categories, JSON format, Redis Streams, WebSocket delivery, CLI + API

**Step 30: Dashboard theming doc**
- `docs/dashboard-theming.md` — 3 theming levels, block layout, theme JSON format, community themes

**Step 31: ECHO debug agent doc**
- `docs/echo-debug-agent.md` — MANUAL only, model selection, capabilities vs limitations, `/echo` command

**Step 32: Git provider integration doc**
- `docs/git-provider-integration.md` — 4 providers, setup flow, per-user vs system-wide, optional nature

**Step 33: Custom commands doc**
- `docs/custom-commands.md` — `/commands new` flow, YAML definition format, natural language description

**Step 34: API reference doc**
- `docs/api-reference.md` — all REST endpoints, WebSocket endpoints, request/response formats

---

## Phase 7 — Installer

**Step 35: Write Unix/Mac installer**
> Ref: [4.14 Installer](./SYNAPSE_OPUS_PLAN.md#414-installer) (L1023–1069)
- `installer/install.sh` — full interactive flow: system name, install type, domain, model provider, ECHO, git provider
- Docker Compose quick mode + bare metal dev mode
- `installer/compose/docker-compose.yml`
- `installer/compose/docker-compose.prod.yml`

**Step 36: Write Windows installer**
> Ref: [4.14 Installer](./SYNAPSE_OPUS_PLAN.md#414-installer) (L1023–1069)
- `installer/install.ps1` — PowerShell equivalent of install.sh

---

## Phase 8 — CLI Specification

**Step 37: CLI command reference**
> Ref: [4.15 CLI (Go)](./SYNAPSE_OPUS_PLAN.md#415-cli-go) (L1069–1122)
- Define all CLI commands with args, flags, descriptions
- TUI feature spec (Bubble Tea + Lipgloss color system)
- Goes into `docs/api-reference.md` CLI section or separate internal spec

---

## Phase 9 — Final Review

**Step 38: Cross-check quality rules**
> Ref: [Quality Rules](./SYNAPSE_OPUS_PLAN.md#quality-rules) (L1269–1284)
- No `{TODO}` or placeholder holes in any file
- `{SYSTEM_NAME}` used everywhere instead of hardcoded "SYNAPSE"
- Every component documents what it logs
- Both creation paths documented (Main Agent + Manual) for all creatable entities
- All Java examples use Java 25+ / Spring Boot 4.x
- All CLI examples use Go / Bubble Tea style
- ACP subscription support present (api_key AND subscription_id)
- ECHO is debug-only everywhere — no automatic fallback language
- Official Store vs Community Store distinction clear in all store docs

**Step 39: Verify critical files are complete**
> Ref: [Critical Files](./SYNAPSE_OPUS_PLAN.md#critical-files) (L1284–1294)
- `docs/architecture.md` — foundation for everything
- `docs/logging-system.md` — critical feature
- `agents/main/system-prompt.md` — heart of the system
- `backend/db/schema.sql` — database foundation
- `docs/agent-teams-system.md` — core of flexibility

---

## File Count Checklist

| Block | Files | Status |
|-------|-------|--------|
| Block 1 — Docs (Steps 15–34) | 20 | - |
| Block 2 — Agent Files (Steps 6–10) | 19 | - |
| Block 3 — Plugin Templates (Steps 11–13) | 13 | - |
| Block 4 — Backend (Steps 3–5) | 3 | - |
| Block 5 — Store (Step 14) | 4 | - |
| Block 6 — Root & Installer (Steps 2, 35–36) | 4 | - |
| **Total** | **63** | |

> Note: Original plan specifies 63 files (Steps 1–63 in PART 5).
> Ref: [PART 5 — FILES YOU CREATE](./SYNAPSE_OPUS_PLAN.md#files-you-create) (L1192–1268)
