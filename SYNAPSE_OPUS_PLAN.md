# SYNAPSE — Opus Creation Plan (English)

> **You (Opus) read this plan and create all architecture and specification files.**
> **Reference Architecture:** The `/new/` folder is background context, NOT a static template.
> **Goal:** Define a complete, extensible AI platform project.

---

## PART 1 — VISION

SYNAPSE is a self-hosted, fully extensible AI platform — not a rigid system, but a framework. Everything is addable at runtime: channels, agents, models, skills, MCP servers. Third parties can run SYNAPSE with their own agents, own channels, and own providers.

**Inspirations:**
- **OpenClaw** → Heartbeat system (context keepalive for API sessions)
- **Hermes** → Self-learning/improvement loop (agents learn from tasks)
- **Paperclip** → CEO/issue/project-management structure
- **Claude Code** → Skills system via skills.sh
- **Obsidian** → Markdown-based knowledge vault per agent
- **VSCode** → Plugin architecture (everything swappable, everything installable)

**Result:** A system that feels like an AI operating system — with CLI, dashboard, installer, offline fallback, and store.

---

## PART 2 — SYSTEM IDENTITY

**Name:** SYNAPSE (default)
> **Important:** The system name is freely choosable by the user. Default is "Synapse". The chosen name is stored as `system.name` in the database (table `system_metadata`) and referenced dynamically everywhere in the UI, CLI, and agent prompts. **No hardcoding of the name.**

**Tagline:** _"Your AI. Your Rules. Your Stack."_

**Layers:**

| Layer | Name | Role | Character |
|-------|------|------|-----------|
| Interface | **MAIN AGENT** | Primary assistant, system controller | Calm, direct, competent |
| Management | **AI-FIRM** (optional, max. 1) | Project management with CEO structure | Strategic, structured |
| Teams | **AGENT TEAMS** (optional, N) | Specialized team groups | Varies by team |
| Fallback | **ECHO** | Debug-only agent (manual, local) | Minimalist, reliable |

**Color System:**
```
Background:      #0F1117
Surface:         #181C27
Border:          #252A38
Text-Primary:    #E8ECF4
Text-Muted:      #6B7490

Main Agent:      #7B9FE0   (Steel Blue)
AI-Firm:         #B07FE8   (Violet)
Agent Teams:     #E07B5A   (Copper — variable per team)
ECHO (Debug):    #4CAF87   (Green)
Status-Warn:     #E8B84B
Status-Error:    #E05A5A
```

---

## PART 3 — PROJECT STRUCTURE

```
synapse/
├── README.md
├── LICENSE
├── CONTRIBUTING.md
│
├── installer/
│   ├── install.sh                # Unix/Mac Installer
│   ├── install.ps1               # Windows Installer
│   └── compose/
│       ├── docker-compose.yml    # Basic (Quick Install)
│       └── docker-compose.prod.yml
│
├── packages/
│   ├── core/                     # Core Runtime (Java/Spring Boot)
│   │   └── src/main/java/dev/synapse/
│   │       ├── runtime/          # Agent Execution Engine
│   │       ├── plugins/          # Plugin Manager
│   │       ├── memory/           # Memory/Vault System
│   │       ├── heartbeat/        # Heartbeat Engine
│   │       ├── learning/         # Self-Learning Loop
│   │       ├── logging/          # Logging System (Core)
│   │       ├── store/            # Plugin Store Client
│   │       └── git/              # Optional Git-Provider Integration
│   │
│   ├── cli/                      # CLI (Go)
│   │   ├── cmd/
│   │   │   └── synapse/
│   │   │       └── main.go
│   │   ├── internal/
│   │   │   ├── tui/              # Bubble Tea TUI
│   │   │   ├── commands/
│   │   │   └── api/              # API client for core
│   │   └── go.mod
│   │
│   ├── dashboard/                # Web UI
│   │   ├── backend/              # Spring Boot API layer (part of core)
│   │   └── frontend/             # Vue 3 + Vite
│   │       ├── src/
│   │       │   ├── components/
│   │       │   ├── views/
│   │       │   ├── stores/       # Pinia
│   │       │   └── themes/       # Theming system
│   │       └── package.json
│   │
│   └── fallback/                 # ECHO Debug Agent
│       └── src/main/java/dev/synapse/echo/
│           ├── EchoAgent.java
│           ├── LocalModelClient.java   # Ollama integration
│           └── ResponseCache.java
│
├── backend/
│   ├── db/
│   │   ├── schema.sql            # Complete PostgreSQL schema
│   │   ├── migrations/
│   │   └── seed.sql              # Initial data (system_metadata, default config)
│   └── vault/
│       └── VAULT_SPEC.md         # Knowledge vault specification
│
├── agents/
│   ├── _templates/               # Templates for new agents
│   │   ├── agent/                # Single-agent template
│   │   │   ├── identity.md
│   │   │   ├── soul.md
│   │   │   ├── connections.md
│   │   │   └── config.yml
│   │   └── team/                 # Agent-team template
│   │       ├── team.yml          # Team configuration
│   │       ├── leader/           # Team-leader agent
│   │       │   ├── identity.md
│   │       │   └── soul.md
│   │       └── members/          # Member agent templates
│   ├── main/                     # Main Agent (required, always active)
│   │   ├── identity.md
│   │   ├── soul.md
│   │   ├── connections.md
│   │   ├── system-prompt.md
│   │   └── config.yml
│   ├── echo/                     # ECHO Debug Agent (required)
│   │   ├── identity.md
│   │   ├── soul.md
│   │   └── system-prompt.md
│   └── ai-firm/                  # Optional AI-Firm (max. 1)
│       ├── firm.yml              # Firm configuration
│       ├── ceo/
│       ├── teams/                # Internal firm teams
│       └── agents/               # Individual sub-agents of firm
│
├── plugins/
│   ├── channels/
│   │   ├── _template/
│   │   │   ├── manifest.yml
│   │   │   ├── Channel.java
│   │   │   └── README.md
│   │   ├── telegram/
│   │   ├── discord/
│   │   └── whatsapp/
│   ├── models/
│   │   ├── _template/
│   │   │   ├── manifest.yml
│   │   │   ├── ModelProvider.java
│   │   │   └── README.md
│   │   ├── openai/
│   │   ├── anthropic/
│   │   ├── deepseek/
│   │   ├── gemini/
│   │   ├── openrouter/
│   │   └── ollama/               # Self-hosted local models
│   ├── skills/
│   │   ├── _template/
│   │   │   ├── manifest.yml
│   │   │   ├── skill.md
│   │   │   └── README.md
│   │   └── [community-skills]/
│   └── mcp/
│       ├── _template/
│       │   ├── manifest.yml
│       │   └── README.md
│       └── [mcp-servers]/
│
├── store/
│   ├── STORE_SPEC.md
│   ├── BUNDLE_SPEC.md            # Community bundle system
│   ├── registry.yml
│   └── submit-plugin.md
│
└── docs/
    ├── architecture.md
    ├── plugin-system.md
    ├── agent-identity-system.md
    ├── agent-teams-system.md
    ├── ai-firm-system.md
    ├── memory-vault.md
    ├── self-learning-loop.md
    ├── heartbeat-system.md
    ├── skills-integration.md
    ├── mcp-integration.md
    ├── acp-registry.md
    ├── store-concept.md
    ├── bundle-system.md
    ├── multi-user.md
    ├── logging-system.md
    ├── dashboard-theming.md
    ├── echo-debug-agent.md
    ├── git-provider-integration.md
    ├── custom-commands.md
    └── api-reference.md
```

---

## PART 4 — CORE COMPONENTS SPECIFICATION

### 4.1 Agent Identity System

Every agent has an own folder with these required files:

**`identity.md`** — Who is this agent?
```markdown
---
id: [agent-id]
name: [Agent Name]
version: 1.0.0
created: [ISO Date]
type: [main|team-member|team-leader|firm-ceo|firm-agent|custom]
---

# Identity: [Name]

## Role
[What is this agent's main task? 2-3 sentences.]

## Capabilities
- [What can it do?]

## Limitations
- [What can it not do / is not allowed to do?]

## Personality
[How does it behave? Tone, style, character.]

## Activation
[When is it active? Triggers, events, manual.]
```

**`soul.md`** — What makes it who it is?
```markdown
---
last-updated: [ISO Date]
immutable: false
---

# Soul: [Name]

## Core Values
[What are the unchangeable principles?]

## Behavioral Rules
1. [Rule 1 — always active]
2. [Rule 2 — ...]

## Communication Style
[How does this agent communicate? Examples.]

## What I Love
[What does this agent do particularly well / enjoy?]

## What I Avoid
[What does this agent not do / avoid?]

## Growth Notes
[What has this agent learned? Updated by self-learning loop.]
```

**`connections.md`** — Who sends, who receives?
```markdown
---
last-updated: [ISO Date]
---

# Connections: [Name]

## Receives From
| Source         | Type             | Trigger              | Priority |
|----------------|------------------|----------------------|----------|
| User (Channel) | Direct Message   | always               | high     |
| [Agent X]      | Task Delegation  | when X is overloaded | medium   |

## Sends To
| Target    | Type     | When                        |
|-----------|----------|-----------------------------|
| User      | Response | after task completion       |
| [Agent Y] | Task     | when task is in Y's domain  |

## Escalation Path
[Who to escalate to if blocked?]

## Blocked From
[Who is this agent NOT allowed to communicate with directly?]
```

**`config.yml`** — Technical configuration:
```yaml
model:
  provider: anthropic
  model_id: claude-sonnet-4-6
  fallback_provider: openai
  fallback_model_id: gpt-4o-mini

memory:
  max_working_tokens: 50000
  compression_threshold: 40000
  compression_provider: ollama      # Choosable from registered self-hosted providers
  compression_model: llama3.2
  semantic_search: true

heartbeat:
  enabled: true
  interval_minutes: 45
  only_when_active: true

skills:
  - skill-id-1

mcp_servers:
  - mcp-server-id-1

learning:
  enabled: true
  reflect_after_tasks: true
  update_soul: false
```

---

### 4.2 Agent Teams System

**Core Principle:** Users can create unlimited agent teams. Each team has a team-leader and unlimited member-agents. The team-leader orchestrates members and responds upward (to main agent or user).

**Team Configuration (`team.yml`):**
```yaml
id: dev-team
name: Development Team
description: "Team for coding tasks"
leader: dev-lead          # Agent ID of leader
members:
  - backend-dev
  - frontend-dev
  - design-dev
  - code-tester
routing:
  receives_from:
    - main-agent
    - user-direct: false  # User cannot speak directly with members
  reports_to:
    - main-agent
```

**Creation — Two Options:**

**Option A: Via Main Agent (simple, guided):**
```
User: "Create a Dev Team with backend, frontend, and tester"
Main Agent:
  → Asks: Team name?
  → Asks: Which member roles?
  → Creates identity.md + soul.md for leader and all members
  → Creates team.yml
  → Registers in DB
  → "Dev-Team is active."
```

**Option B: Manual (for power users):**
```
1. Create folder: agents/teams/[team-name]/
2. Fill out team.yml (template available)
3. Create leader/ and members/ with identity.md, soul.md, config.yml
4. Via CLI: synapse teams reload
   or Dashboard: Teams → "Reload from Disk"
```

**Example Teams (from /new folder — examples only, not fixed):**
- Dev-Team (Backend, Frontend, Design, Tester)
- Research-Team (Researcher, Analyst, Writer)
- Support-Team (Triage, FAQ-Bot, Escalation)
- Idea-Team (Idea-Finder, Evaluator, Board-Writer)

---

### 4.3 AI-Firm System

**Rule: Maximum one AI-Firm per SYNAPSE instance (optional).**

The AI-Firm is an extended structure with CEO at the top, optional individual sub-agents, and optional internal teams.

**Firm Configuration (`ai-firm/firm.yml`):**
```yaml
id: my-firm
name: "FTM AI Company"
ceo: firm-ceo
agents:                  # Direct sub-agents of CEO (optional)
  - requirements-engineer
  - alignment-agent
teams:                   # Internal teams (optional)
  - planning-team
  - design-team
workflow:
  mode: paperclip        # paperclip | custom
  board_integration: gitlab  # gitlab | github | forgejo | none
```

**Creation — Two Options:**

**Option A: Via Main Agent:**
```
User: "Create an AI-Firm in Paperclip style"
Main Agent:
  → Explains structure (CEO, sub-agents, teams)
  → Asks: Firm name?
  → Asks: Which teams/agents?
  → Creates all files
  → "AI-Firm is active. CEO awaits projects."
```

**Option B: Manual:**
```
1. Create agents/ai-firm/ structure
2. Fill out firm.yml
3. Create CEO, sub-agents, teams individually
4. synapse firma reload
```

---

### 4.4 Main Agent — System Controller

**Two Modes:**

**Chat Mode** (default) — Normal assistant, questions, vault access, tasks.

**System Mode** — Triggered by system commands or system contexts:
```
User: "Install the Telegram channel"
Main Agent:
  → Searches store (official + skills.sh + ACP)
  → Shows: name, version, author, permissions, estimated cost impact
  → Asks: "Install? [Yes/No/Details]"
  → On Yes: download, verify, guided configuration, activation
  → Confirmation: "Telegram channel is running."
```

**Standard Commands:**
```
/agents           → All agents + teams + firm status
/agents new       → Create new single agent (guided)
/teams new        → Create new agent team (guided)
/firm             → AI-Firm status
/firm new         → Create AI-Firm guided (only if none exists)
/channels         → All channels
/channels new     → Add new channel (guided)
/models           → All model providers
/models new       → Add new model provider (guided)
/skills           → All installed skills
/skills publish   → Publish custom skill to skills.sh (manual, requires user consent)
/mcp              → All MCP servers
/install [plugin] → Install plugin directly
/store            → Open store
/commands         → All custom commands
/commands new     → Create new custom command (guided)
/costs            → Cost overview
/snapshot [proj]  → Project status snapshot
/story [proj]     → "Story so far" summary
/dump [idea]      → Quick save idea without overhead
/echo             → Launch ECHO debug agent (manual)
/health           → System health check
/logs [agent]     → View agent logs
/git              → Git provider status
/config           → Open system configuration
```

**Custom Commands (user-definable):**
```
User: "/commands new"
Main Agent: "New custom command"
  → Name? (e.g. "daily-brief")
  → Description: What should it do?
    (e.g. "Summarize all open tasks from GitLab and send me a morning briefing")
  → Main Agent: implements the command handler internally
  → "Command /daily-brief is active."

Technical: Custom commands stored as YAML definitions.
Main Agent interprets description and executes appropriate actions.
No code writing required — description in natural language only.
```

---

### 4.5 Backend — Two-Part System

#### Part 1: SQL Database (PostgreSQL)

```sql
-- SYSTEM
system_metadata        -- name, version, created_at, settings (JSON)
system_logs            -- All system events (CRITICAL — see 4.12)

-- AGENTS
agents                 -- id, name, type, status, config (JSON), created_at
agent_teams            -- id, name, leader_agent_id, config (JSON)
agent_team_members     -- team_id, agent_id, role
ai_firm                -- id, name, ceo_agent_id, config (JSON) — max. 1 row

-- PLUGINS
plugins                -- id, name, type, version, status, manifest (JSON)
installed_channels     -- id, plugin_id, user_id, config (JSON)
installed_models       -- id, plugin_id, user_id, config (JSON), is_self_hosted
installed_skills       -- id, plugin_id, agent_id, config (JSON)
installed_mcp          -- id, plugin_id, agent_id, config (JSON)

-- CUSTOM COMMANDS
custom_commands        -- id, name, description, handler_config (JSON), created_by

-- CONVERSATIONS
conversations          -- id, agent_id, user_id, channel_id, started_at, status
messages               -- id, conversation_id, role, content, tokens, created_at

-- TASKS & PROJECTS
projects               -- id, name, status, ai_firm_id, created_at
tasks                  -- id, project_id, title, status, assigned_agent_id, size, version
task_logs              -- id, task_id, event, payload (JSON), created_at

-- TRACKING
agent_costs            -- id, agent_id, provider_id, tokens_in, tokens_out, cost, created_at
heartbeat_log          -- id, agent_id, session_id, sent_at, cache_saved (bool)
sessions               -- id, agent_id, user_id, started_at, last_activity, status

-- STORE
store_cache            -- Cached store data (plugins + bundles), last_updated
plugin_stats           -- plugin_id, downloads, stars, bundle_count (synced from central store)

-- USERS & AUTH
users                  -- id, username, email, role, settings (JSON), created_at
user_agents            -- user_id, agent_id, permission_level
user_channels          -- user_id, channel_id
sessions_auth          -- id, user_id, token_hash, expires_at, created_at

-- GIT INTEGRATION (optional)
git_providers          -- id, type (gitlab|github|forgejo), url, user_id, token_encrypted
git_repos              -- id, provider_id, repo_path, project_id, linked_at
```

#### Part 2: Knowledge Vault (per Agent)

```
vault/
└── agents/
    └── [agent-id]/
        ├── identity.md           # Never compressed
        ├── soul.md               # Never compressed
        ├── connections.md        # Never compressed
        ├── memory/
        │   ├── working/          # Current session — uncompressed
        │   │   └── [session-id].md
        │   ├── episodic/         # Past sessions — compressed
        │   │   ├── 2026-W19.md
        │   │   └── index.md
        │   └── semantic/
        │       ├── patterns.md
        │       ├── learned.md
        │       └── skills-notes.md
        └── projects/
            └── [project-id]/
                └── context.md
```

**Compression Mechanism:**
```
Trigger: working/ session > compression_threshold (configurable, default 40k tokens)

Compression provider: choosable from all registered self-hosted providers
  → Default: Ollama with llama3.2 (if installed)
  → Alternative: Any other self-hosted provider added by user
  → Setting in: System Settings → Vault → Compression Provider

Process:
  1. LLM reads working/[session].md
  2. Creates compact summary (max 2000 tokens)
  3. Updates semantic/learned.md
  4. Moves to episodic/[week].md
  5. Deletes original working/ session
  6. Updates index.md

Rule: identity.md, soul.md, connections.md — NEVER compressed.
```

**Vector Search (optional):** Qdrant for semantic search across vault.

---

### 4.6 Plugin System

All plugin types (channels, models, skills, MCP) follow the same manifest structure.

**Manifest (`manifest.yml`):**
```yaml
id: telegram-channel
name: Telegram Channel
version: 1.0.0
type: channel           # channel | model | skill | mcp
author: synapse-core
license: MIT
description: "Telegram Bot as channel"

requires:
  synapse_version: ">=0.1.0"
  java: ">=25"

config_schema:
  bot_token:
    type: string
    required: true
    secret: true
  allowed_users:
    type: list
    required: false

hooks:
  on_install: "setup"
  on_uninstall: "cleanup"
  on_message: "handleMessage"
  on_send: "sendMessage"

store:
  tags: [messaging, bot]
  icon: "🤖"
```

**Creation — always two options:**

**Via Main Agent:**
- Guided wizard for each plugin category
- Asks all required config fields
- Installs, configures, activates

**Manual:**
- Create plugin folder with `manifest.yml` + code
- Via CLI: `synapse plugins reload`
- Via Dashboard: Plugins → "Load from Disk"

---

### 4.7 ACP Registry Integration

ACP (Agent Communication Protocol) Registry is a central directory of AI providers and agent definitions.

```
Integration:
  → SYNAPSE taps the ACP Registry as additional provider source
  → Many providers are pre-configured there
  → No manual endpoint setup needed
  → SYNAPSE: "Choose a provider from ACP Registry" → ready to use immediately

Provider Types in ACP:
  → API-key based (OpenAI, Anthropic, DeepSeek, etc.)
  → Subscription-based (some providers offer subscription models)
  → Self-hosted (Ollama, local models)

For Subscription Providers:
  → Configuration stores subscription_id instead of api_key
  → Billing handled by provider
  → Token usage still tracked in SYNAPSE logging

Flow:
  1. SYNAPSE loads provider list from ACP Registry
  2. User chooses provider (e.g. "Anthropic via ACP")
  3. Only need to enter credential (API key OR subscription ID)
  4. Done — endpoint, model-list, capabilities auto-loaded

Advantage:
  → No plugin file needed for standard providers
  → ACP keeps endpoints current (no update overhead)
  → New providers auto-available when added to ACP
```

---

### 4.8 Heartbeat System (OpenClaw-inspired)

```
Purpose: Keep API session context cache alive.

Logic:
  - Only for active sessions with expected continuation
  - Only if provider supports caching (Anthropic: 5-min TTL)
  - Heartbeat sent 45 sec before cache expiration
  - Content: minimal ping — no actual content
  - Stops: if session idle > 10 min
  - Stops: if task completed

Tracking:
  - heartbeat_log table: agent_id, session_id, sent_at, cache_saved
  - Dashboard shows: "Cache warm" / "Cache cold" status
  - Costs: heartbeat tokens tracked separately
```

---

### 4.9 Self-Learning Loop (Hermes-inspired)

```
Trigger: After completed task/conversation

Flow:
  1. REFLECT
     Agent reads last session/task
     Creates internally: "What went well? What went badly? What's new?"

  2. UPDATE MEMORY
     Writes insights to semantic/learned.md
     Compresses if threshold reached

  3. UPDATE PATTERNS
     If same pattern 3x → semantic/patterns.md
     
  4. SKILL CREATION (optional, requires user consent)
     Pattern frequent enough → "Should I create a skill for this?"
     On yes → create skill.md
     Manual publishing: `/skills publish` command — NOT automatic
     
  5. SOUL UPDATE (only with explicit user consent)
     soul.md never auto-updated — always ask first

Rate Limiting:
  - Max 1 learn-cycle per hour per agent
  - Max 3 pattern-updates per day
  - Skill-creation: always requires user consent
  - Skill-publishing to skills.sh: always manual via `/skills publish`
```

---

### 4.10 ECHO — Debug Agent (Manual Only)

```
Purpose: Manual debugging and simple queries in terminal.
NOT an automatic internet fallback — manual invocation only.

Model: Choosable from registered self-hosted providers
  → Default: Ollama with micro-models (CPU-optimized)
  → Important: ONLY small models for CPU-only scenarios
    Recommended: phi3-mini, llama3.2:1b, mistral:7b-q4
  → User can choose ECHO provider in settings:
    "ECHO Model" → list of all self-hosted providers + available models

Activation:
  → MANUAL ONLY: `/echo` command in CLI or Dashboard
  → NOT automatic internet fallback
  → Debug use case: simple questions, testing, dev work
  → Terminal-first (CLI), not a system-wide fallback mode

ECHO Identity:
  - Knows it's ECHO, not main agent
  - Clearly states: "I'm ECHO. I can help with [X] locally."

Capabilities:
  ✅ Simple questions (local model)
  ✅ Read vault (local markdown files)
  ✅ Save tasks and notes
  ✅ Read project context
  ✅ Query memories

Not available:
  ❌ Store access
  ❌ Git provider access
  ❌ External API calls
  ❌ Internet-dependent features
```

---

### 4.11 Store & Bundle System

**Store Types:**
```
1. Official Store    — SYNAPSE-maintained, trusted plugins (private external repo)
2. Community Store   — Community-submitted bundles (public external repo)
3. skills.sh         — Direct integration via API
4. ACP Registry      — Providers and agent definitions
5. Direct URL        — Install directly from GitHub/GitLab URL
```

**Plugin Statistics (central hosting):**
```
Per plugin in store:
  → Download count (how many times installed)
  → Stars (users can rate)
  → Bundle count (in how many bundles included)
  → Latest version + update date
  → Compatibility (SYNAPSE version range)

Filtering:
  → By popularity (stars / downloads)
  → By category
  → By compatibility
  → By type (channel / model / skill / mcp / bundle)
```

**Bundle Community System:**
```
Concept:
  → User creates bundles in SYNAPSE UI
  → Bundle = collection of plugins + configuration as package
  → Stored in central community repo (public GitHub/GitLab)
  → Submission via pull request

Flow in UI:
  User: Bundles → "Create new bundle"
  → Select plugins (multi-select)
  → Name, description, tags
  → "Submit to community" → automatic PR to bundle repo
  → After merge: visible in store for everyone

Store Clarification:
  → Official Store: External private repo (SYNAPSE-maintained, trusted plugins only)
  → Community Store: External public repo (synapse-community/bundles, PR-based)
  → Both integrated into SYNAPSE store UI seamlessly

Central Bundle Repo:
  → Public GitHub/GitLab repo (synapse-community/bundles)
  → Each bundle as [bundle-name].yml
  → PR-based review system
  → Automated validation (CI check: are all referenced plugins valid?)

Note on skills.sh:
  → skills.sh has own data infrastructure
  → Skill stats loaded directly from skills.sh API (no own hosting)
  → Bundle counts for skills.sh skills as best-effort
```

**Install Flow:**
```
User: "/install telegram" or Store UI
→ Plugin info: name, version, author, stats, permissions
→ "Install?" [Yes/No/Details]
→ Guided configuration (all required config_schema fields)
→ Activation
→ "Telegram channel active."
```

---

### 4.12 Logging System (CRITICAL)

Logging is one of the most important components. Everything is logged.

**Log Categories:**
```
SYSTEM          → Start, stop, config changes, plugin events
AGENT           → Agent start/stop, task start/end
AGENT_MESSAGE   → Every message (in/out), tokens, cost, latency
HEARTBEAT       → Sent, cache-hit, cache-miss, skip-reason
LEARNING        → Reflect-start, memory-update, pattern-found, skill-created
MEMORY          → Compression start/end, vault write/read
PLUGIN          → Install, uninstall, update, enable, disable, error
STORE           → Browse, install request, download
CHANNEL         → Connect, disconnect, message received/sent, error
MODEL           → Provider request, response, token-count, cost, timeout, error
MCP             → Server start, tool-call, tool-response, error
COMMAND         → Command executed, custom-command-created, error
AUTH            → Login, logout, token-created, permission-denied
GIT             → Provider connected, repo linked, sync-event, error
TASK            → Created, assigned, status-change, done, blocked
PROJECT         → Created, status-change, done, archived
COST            → Budget alerts (50%, 80%, 100%)
ERROR           → All unexpected errors (always with stack trace)
```

**Log Entry Format (all logs):**
```json
{
  "id": "uuid",
  "timestamp": "ISO-8601",
  "level": "INFO|WARN|ERROR|DEBUG",
  "category": "AGENT_MESSAGE",
  "source": {
    "agent_id": "main-agent",
    "user_id": "user-123",
    "session_id": "session-xyz"
  },
  "event": "message.sent",
  "payload": {
    "tokens_in": 1200,
    "tokens_out": 340,
    "cost_usd": 0.0023,
    "latency_ms": 1240,
    "model": "claude-sonnet-4-6",
    "provider": "anthropic"
  },
  "correlation_id": "uuid",
  "trace_id": "uuid"
}
```

**Storage:**
```
Persistent: PostgreSQL (system_logs table)
Realtime:   Redis Streams → WebSocket to dashboard
Retention:  Configurable (default: 90 days, then archive or delete)
Export:     CSV / JSON via dashboard or CLI
```

**Log Delivery:**
```
Dashboard → /logs page:
  → Live stream via WebSocket (SSE fallback)
  → Filter: level, category, agent, time range, search
  → Timeline view per session/task
  → Cost breakdown from logs

CLI:
  synapse logs                    → Live tail (all)
  synapse logs --agent main       → Main agent only
  synapse logs --category ERROR   → Errors only
  synapse logs --session [id]     → Complete session

API:
  GET /api/logs?category=ERROR&since=ISO&limit=100
  WebSocket /ws/logs (live stream)
```

---

### 4.13 Dashboard — Theming & Layout

**Theming System:**
```
Dashboard UI is themeable.
Routes, API endpoints, and backend stay unchanged.
Only: colors, fonts, layout blocks.

Theming Levels:
  1. CSS Variables Override
     → User can override all design tokens via settings
     → Stored in DB (user_settings.theme_overrides JSON)
     
  2. Block-based Layout (Drupal-like)
     → Dashboard pages consist of configurable blocks
     → User can add, remove, reorder blocks
     → Blocks: AgentStatus, CostWidget, LogFeed, TaskBoard, ChatPanel, etc.
     → Configuration stored in DB (user_settings.dashboard_layout JSON)
     
  3. Community Themes
     → Theme files as JSON/CSS (in store as "theme" category)
     → Import/export via dashboard

Theme Format:
  {
    "name": "Dark Hacker",
    "colors": {
      "--color-bg": "#0F1117",
      "--color-surface": "#181C27",
      "--color-main-agent": "#7B9FE0",
      ...
    },
    "fonts": {
      "--font-primary": "JetBrains Mono",
      "--font-size-base": "14px"
    }
  }
```

**Dashboard Pages:**
```
/ (Home)
  → Configurable block dashboard
  → Default: AgentStatus + CostToday + ActiveProjects + LogFeed

/chat
  → Multi-agent chat
  → Switch agent via dropdown
  → Team-chat view (when team selected)
  → Conversation history

/agents
  → Single agents + teams + AI-firm overview
  → Status: online / offline / echo-mode
  → Agent detail: memory stats, recent tasks, costs
  → Create: guided or "Manual Setup Guide" link

/projects
  → Project list (AI-firm style when active)
  → Kanban board per project
  → Session logs

/store
  → Plugin categories (channels, models, skills, MCP, bundles)
  → Installed plugins with update indicator
  → Stars, downloads, bundle-count per plugin
  → Bundle creator UI

/costs
  → Cost dashboard: per agent, provider, day/week/month
  → Budget alerts configuration

/logs
  → Live log feed (WebSocket)
  → Filter, search, timeline
  → Session replay for tasks

/settings
  → System name (changeable)
  → Model provider keys
  → ECHO provider selection
  → Vault compression provider selection
  → Git provider accounts
  → Heartbeat settings
  → Dashboard theming (block layout + color overrides)
  → Multi-user management (if ADMIN)
```

---

### 4.14 Installer

**Modes:**
```
1. Quick (Docker Compose) — for most users
2. Bare Metal / Dev — for developers

No k3s/Helm as core feature (too complex for average users).
k3s is possible for later "Advanced" docs, but not part of core installer.
```

**Installer Flow:**
```
./install.sh

  Welcome to {SYSTEM_NAME}!
  ─────────────────────────────────────────────────
  System name? [SYNAPSE] → stored in DB

  Install type?
  [1] Quick (Docker Compose) — recommended
  [2] Dev / Bare Metal

  Domain or localhost? [localhost]

  Set up first model provider?
  [1] OpenAI  [2] Anthropic  [3] DeepSeek  [4] Ollama (local)  [5] From ACP Registry  [6] Skip

  → Enter API key (or Ollama URL)

  Offline support (ECHO) via Ollama? [Yes/No]
  → If Yes: Which micro-model? [phi3-mini / llama3.2:1b / mistral:7b-q4]

  Connect Git provider? [Yes/No]
  → If Yes: [GitLab / GitHub / Forgejo / Gitea / Self-hosted GitLab]
  → URL + token

  Starting installation...
  ─────────────────────────────────────────────────
  ✅ {SYSTEM_NAME} running at http://localhost:3000
  ✅ CLI: synapse chat
  ✅ Main Agent ready.
```

---

### 4.15 CLI (Go)

```bash
# Chat
synapse chat                      # Chat with main agent
synapse chat --agent [id]         # Directly with specific agent
synapse chat --team [id]          # With team-leader

# Agents & Teams
synapse agents list               # All agents
synapse agents new                # Create guided
synapse teams list                # All teams
synapse teams new                 # Create guided
synapse firma                     # AI-Firm status

# Plugins
synapse install [plugin]          # Install plugin
synapse plugins list              # All plugins
synapse channels list             # All channels
synapse models list               # All model providers

# Store
synapse store                     # Open store TUI

# System
synapse costs                     # Cost overview
synapse logs                      # Live logs
synapse logs --agent [id]         # Agent-specific
synapse logs --category ERROR     # Errors only
synapse logs --session [id]       # Full session
synapse health                    # Health check
synapse echo                      # Launch ECHO debug agent
synapse config                    # Show/change configuration
synapse commands list             # Custom commands
synapse commands new              # Create new command (guided)
synapse reload                    # Reload plugins + agents (for manual changes)
synapse git connect [provider]    # Connect git provider

# TUI (Bubble Tea)
synapse tui                       # Full terminal dashboard
```

**TUI Features (Bubble Tea + Lipgloss):**
- Colors (SYNAPSE color system)
- Selection menus with arrow keys
- Fuzzy search in lists
- Scrollable lists (long outputs)
- Multi-select with space
- Confirmation with enter
- Live log feed in TUI

---

### 4.16 Git Provider Integration (Optional)

```
Purpose: Agents can optionally access Git repos for:
  → Reading/creating issues (project management)
  → Reading code (context for coding tasks)
  → Managing MRs/PRs (AI-Firm workflow)

Supported Providers:
  → GitLab (cloud + self-hosted)
  → GitHub (cloud)
  → Forgejo (self-hosted, GitHub-compatible)
  → Gitea (self-hosted)

Setup:
  → Via installer (optional)
  → Via dashboard: Settings → Git Provider → "Add Provider"
  → Via CLI: synapse git connect [provider]
  → Credential storage: encrypted in DB

Per-user configuration:
  → Each user can have own Git provider accounts
  → ADMIN can configure system-wide providers

Important: Git integration is OPTIONAL.
SYNAPSE runs completely without Git.
Git only enables advanced project-management features.
```

---

### 4.17 Multi-User System

```
User Roles:
  OWNER  → First user, full control, can create ADMINs
  ADMIN  → Manages system, installs plugins, creates system agents
  USER   → Own agents, own channels, own projects
  VIEWER → Read-only (no chat, no actions)

Login System (complex — think carefully):
  → JWT-based authentication
  → Session management (sessions_auth table)
  → Optional: OAuth providers (GitHub, GitLab as login option)
  → 2FA optional

Role-based View:
  → Dashboard adapts to role
  → OWNER/ADMIN: sees system settings, all users, all agents
  → USER: sees only own resources + shared
  → VIEWER: read-only view
  → Agent visibility: Public (everyone), Private (owner only), Shared (selected users)

Per User:
  → Own agent instances or use shared agents
  → Own vault section
  → Own API keys
  → Own channels
  → Own dashboard layout and theme

Shared Resources:
  → System plugins (Telegram, etc. shared)
  → Shared agents (configurable)
  → Shared projects
```

---

## PART 5 — OPUS TASK

### Files You Create:

**Block 1 — Documentation (20 files)**
1. `docs/architecture.md` — Complete architecture (ASCII diagrams, layers, flows)
2. `docs/plugin-system.md` — Plugin architecture, manifest spec, creation paths
3. `docs/agent-identity-system.md` — identity/soul/connections fully documented
4. `docs/agent-teams-system.md` — Teams, leader, member, team.yml, routing
5. `docs/ai-firm-system.md` — AI-Firm, max. 1, CEO structure, firm.yml
6. `docs/memory-vault.md` — Vault, compression, Obsidian compatibility
7. `docs/self-learning-loop.md` — Full learning loop specification
8. `docs/heartbeat-system.md` — Heartbeat, caching, tracking
9. `docs/skills-integration.md` — skills.sh API, `/skills publish` command
10. `docs/mcp-integration.md` — MCP protocol, plugins
11. `docs/acp-registry.md` — ACP Registry integration with subscription support
12. `docs/store-concept.md` — Store types, stats, install flow
13. `docs/bundle-system.md` — Bundle community system, PR flow
14. `docs/multi-user.md` — User roles, auth, role-based views
15. `docs/logging-system.md` — Log categories, format, storage, API
16. `docs/dashboard-theming.md` — Theming, block layout, theme format
17. `docs/echo-debug-agent.md` — ECHO, model selection, capabilities, MANUAL activation only
18. `docs/git-provider-integration.md` — Providers, setup, capabilities
19. `docs/custom-commands.md` — Creating custom commands, format, handling
20. `docs/api-reference.md` — All REST API endpoints

**Block 2 — Agent Files (19 files)**
21. `agents/_templates/agent/identity.md`
22. `agents/_templates/agent/soul.md`
23. `agents/_templates/agent/connections.md`
24. `agents/_templates/agent/config.yml`
25. `agents/_templates/team/team.yml`
26. `agents/_templates/team/leader/identity.md`
27. `agents/_templates/team/leader/soul.md`
28. `agents/main/identity.md`
29. `agents/main/soul.md`
30. `agents/main/connections.md`
31. `agents/main/system-prompt.md`
32. `agents/main/config.yml`
33. `agents/echo/identity.md`
34. `agents/echo/soul.md`
35. `agents/echo/system-prompt.md`
36. `agents/ai-firm/firm.yml` (example configuration)
37. `agents/ai-firm/ceo/identity.md`
38. `agents/ai-firm/ceo/soul.md`
39. `agents/ai-firm/ceo/system-prompt.md`

**Block 3 — Plugin Templates (13 files)**
40. `plugins/channels/_template/manifest.yml`
41. `plugins/channels/_template/Channel.java`
42. `plugins/channels/telegram/manifest.yml`
43. `plugins/channels/telegram/TelegramChannel.java`
44. `plugins/models/_template/manifest.yml`
45. `plugins/models/_template/ModelProvider.java`
46. `plugins/models/anthropic/manifest.yml`
47. `plugins/models/openai/manifest.yml`
48. `plugins/models/deepseek/manifest.yml`
49. `plugins/models/ollama/manifest.yml`
50. `plugins/skills/_template/manifest.yml`
51. `plugins/skills/_template/skill.md`
52. `plugins/mcp/_template/manifest.yml`

**Block 4 — Backend (3 files)**
53. `backend/db/schema.sql` — Complete PostgreSQL schema (all tables)
54. `backend/db/seed.sql` — Initial data (system_metadata default name, etc.)
55. `backend/vault/VAULT_SPEC.md`

**Block 5 — Store (4 files)**
56. `store/STORE_SPEC.md`
57. `store/BUNDLE_SPEC.md`
58. `store/registry.yml`
59. `store/submit-plugin.md`

**Block 6 — Root & Installer (4 files)**
60. `README.md` — Getting started, features, architecture
61. `CONTRIBUTING.md`
62. `installer/install.sh`
63. `installer/install.ps1`

### Quality Rules:

1. **No placeholder holes** — No `[TODO]`, no `[Details here]` — every file complete
2. **System name never hardcoded** — Always `{SYSTEM_NAME}` reference or "SYNAPSE (default)"
3. **Two paths always documented** — Main Agent + Manual for everything creatable
4. **English only** — All code examples, attributes, table names in English
5. **Logging always mentioned** — Every component describes what it logs
6. **Production-ready** — Templates immediately usable when implemented
7. **Java/Spring stack** — All code examples in Java (no Python)
8. **Go CLI** — CLI examples in Go/Bubble Tea style
9. **ACP subscription support** — Handle both `api_key` AND `subscription_id`
10. **Skill publishing manual** — `/skills publish` command, NOT automatic
11. **ECHO is debug-only** — Manual `/echo` invocation, NOT automatic fallback
12. **Store clarity** — Official Store (private) vs Community Store (public) distinction always clear

### Critical Files:

- `docs/architecture.md` — Foundation for everything
- `docs/logging-system.md` — Critical feature
- `agents/main/system-prompt.md` — Heart of the system
- `backend/db/schema.sql` — Database foundation
- `docs/agent-teams-system.md` — Core of the flexibility

---

## PART 6 — TECHNOLOGY STACK

```yaml
backend:
  language: Java 25+
  framework: Spring Boot 4.x
  database: PostgreSQL 18+
  orm: Spring Data JPA
  async:
    primary: Virtual Threads
    logs: Redis Streams / EventBus
    scheduled_tasks: Spring Scheduler / Redis Queue
  queue: Redis
  realtime: WebSocket
  api_docs: springdoc-openapi 3.x / Swagger / Scalar
  logging:
    processing: Redis Streams or EventBus
    delivery: WebSocket or SSE
    storage: PostgreSQL (system_logs table)

frontend:
  framework: Vue 3 + Vite
  styling: Tailwind CSS
  ui: Custom dashboard UI system + shadcn-vue
  state: Pinia
  realtime:
    primary: WebSocket
    fallback: SSE

cli:
  language: Go
  framework:
    tui: Bubble Tea
    styling: Lipgloss
    prompts: Huh / Bubbles
  features:
    - Colors (SYNAPSE color system)
    - Selection menus with arrow keys
    - Fuzzy search in lists
    - Scrollable lists
    - Multi-select with space
    - Confirmation with enter
    - Live log feed in TUI

installer:
  format: Shell Script (Bash) + Go helper binary
  container: Docker Compose
  # k3s/Helm: not in core installer, for future advanced docs

vault:
  format: Markdown (Obsidian-compatible)
  index: Qdrant (optional, for semantic search)
  compression: LLM-based (choosable self-hosted provider)

skills:
  format: Claude Code Skills Format (Markdown)
  source: skills.sh API
  publishing: Manual via `/skills publish` command (requires user consent)

mcp:
  protocol: Standard MCP (JSON-RPC)
  transport: stdio + HTTP

echo_fallback:
  engine: Ollama
  models: Micro-models only (phi3-mini, llama3.2:1b — CPU-optimized)
  activation: Manual via `/echo` command (debug use, NOT automatic)
  provider: Choosable from registered self-hosted provider list

acp_registry:
  integration: API integration for provider discovery
  credential_types: API-key, subscription-ID, self-hosted
  use: Provider discovery, automatic endpoint setup
```

---

*SYNAPSE feels like a system that breathes — extensible, self-learning, offline-capable. Every agent has a soul. Everything is loggable. Everything is extensible. The name belongs to the user.*
