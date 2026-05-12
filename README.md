# SYNAPSE

> **Your AI. Your Rules. Your Stack.**

**Current Release**: v2.5.0 (Security Hardening)

SYNAPSE is a self-hosted, fully extensible AI platform that puts you in complete control of your AI environment. Run it on your own infrastructure, connect any model provider, build custom agent teams, and extend everything through a powerful plugin system — without ever depending on a third-party cloud.

---

## Feature Highlights

- **Extensible Plugin System** — Add channels (Telegram, Discord, WhatsApp), model providers (OpenAI, Anthropic, DeepSeek, Ollama), skills (Claude Code Skills format), and MCP servers. Install community bundles or publish your own.
- **Agent Teams** — Compose multiple specialized AI agents into collaborative teams. Each team handles distinct domains and delegates tasks intelligently.
- **AI-Firm (Project Management Layer)** — An optional single orchestration agent that manages projects across teams, tracks goals, and coordinates long-running workflows.
- **Heartbeat & Health Monitoring** — Built-in heartbeat system keeps all agents and services in sync. Get notified when something goes offline.
- **Self-Learning & Skill Publishing** — Agents learn from interactions and can publish reusable skills to the community store (always with explicit user consent).
- **Multi-User Support** — Full role-based access control. Multiple users can interact with the platform simultaneously with isolated contexts.
- **Plugin & Bundle Store** — Browse and install community-contributed plugins, bundles, and skill packs from the integrated store. Separate community store from the curated official store.
- **Advanced Agent Workflows** — Collaboration sessions, inter-agent delegation, shared context, and goal-based planning with versioned plan artifacts.
- **Native Java Tools Runtime** — Built-in tool discovery and execution (`tool_registry_inspect`, `plugin_contract_validate`) with caching and timeout controls.
- **Central Hardening Policies** — Unified guardrails for delegation/planning/tooling with token-budget and concise-mode signaling.
- **CLI + Web Dashboard** — A Go-based Bubble Tea TUI for power users and a full Vue 3 web dashboard for everyone else.
- **ECHO — Offline Debug Fallback** — When all other agents are unavailable, ECHO activates (debug mode only, manual invocation). Never be left completely without assistance.

---

## Quick Start

### Prerequisites

- **Docker & Docker Compose** (recommended) **or** local development tools:
  - JDK 25+
  - Maven 3.9+
  - Node.js 22+
  - Go 1.26+
  - PostgreSQL 18+
  - Redis 8+
- Supported OS: Linux, macOS, or Windows (WSL2 recommended)

### Docker Compose Setup (Recommended)

```bash
# Clone the repository
git clone https://github.com/FTMahringer/Synapse.git
cd Synapse

# Prepare environment variables
cp .env.example .env

# Start all services
docker compose -f installer/compose/docker-compose.yml --env-file .env up -d

# Check service health
docker compose -f installer/compose/docker-compose.yml ps

# View logs
docker compose -f installer/compose/docker-compose.yml logs -f backend
```

Services will be available at:
- **Backend API**: http://localhost:8080
- **Dashboard**: http://localhost:3000
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **Qdrant**: localhost:6333

First-time setup will automatically:
1. Create PostgreSQL database
2. Run all Flyway migrations (current schema: v15)
3. Initialize system metadata
4. Sync plugin store registry
5. Start backend and dashboard

### Environment Configuration

Key environment variables are documented in `.env.example` (copy it to `.env` and adjust values):

```bash
# Required for production
JWT_SECRET=your-256-bit-secret-here
SECRETS_ENCRYPTION_KEY=your-32-byte-key-here

# Optional override (defaults to current application build version)
SYNAPSE_VERSION=v2.4.0
POSTGRES_PASSWORD=your-secure-password
ECHO_ENABLED=false
```

### Manual Development Setup

```bash
# Clone the repository
git clone https://github.com/FTMahringer/Synapse.git
cd Synapse

# Backend (requires PostgreSQL and Redis running)
cd packages/core
mvn spring-boot:run

# Frontend
cd packages/dashboard/frontend
npm install
npm run dev

# CLI
cd packages/cli
go run main.go health
```

---

## Architecture Overview

{SYSTEM_NAME} is organized into four distinct layers. Each layer is optional except the Main Agent, which is the core of the platform.

| Layer | Role | Multiplicity | Color |
|---|---|---|---|
| **Main Agent** | Primary assistant. Handles all user interactions, routes to teams, manages plugins and skills. Always active. | Exactly 1 | `#7B9FE0` |
| **AI-Firm** | Optional project management orchestrator. Coordinates agent teams, tracks multi-step projects, manages goals and deadlines. | 0 or 1 | `#B07FE8` |
| **Agent Teams** | Optional specialized agent groups. Each team has a domain (e.g., code, research, writing). Teams collaborate via the Main Agent or AI-Firm. | 0 to N | `#E07B5A` |
| **ECHO** | Emergency debug fallback. Activates only in debug mode, only when invoked manually. Provides raw diagnostic output. | 0 or 1 (debug only) | `#4CAF87` |

### Color System

The platform uses a consistent color system across the dashboard, CLI, and logs to identify agent layers at a glance:

| Element | Hex |
|---|---|
| Background | `#0F1117` |
| Surface | `#181C27` |
| Main Agent | `#7B9FE0` |
| AI-Firm | `#B07FE8` |
| Agent Teams | `#E07B5A` |
| ECHO | `#4CAF87` |

### Plugin System

Plugins extend {SYSTEM_NAME} in four categories:

| Category | Examples | Notes |
|---|---|---|
| **Channels** | Telegram, Discord, WhatsApp | Route messages to/from external platforms |
| **Model Providers** | OpenAI, Anthropic, DeepSeek, Ollama | Swap or combine LLM backends per-agent |
| **Skills** | Claude Code Skills format | Reusable callable capabilities for agents |
| **MCP Servers** | Any MCP-compatible server | Extend agents with external tool protocols |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 25+, Spring Boot 4.x |
| **Frontend** | Vue 3, Vite |
| **CLI** | Go, Bubble Tea TUI |
| **Database** | PostgreSQL 18+ |
| **Cache / Pub-Sub** | Redis |
| **Containerization** | Docker, Docker Compose |

---

## Project Structure

```
synapse/
├── packages/core/    # Spring Boot 4.x application (Java 25+)
├── packages/dashboard/frontend/ # Vue 3 + Vite web dashboard
├── packages/cli/     # Go CLI with Bubble Tea TUI
├── backend/          # SQL schema, seed, migrations, vault spec
├── plugins/          # First-party plugin implementations
├── store/            # Store metadata and community bundle index
├── install.sh        # Linux/macOS interactive installer
├── installer/        # compose files and setup scripts
├── packages/         # Shared packages and libraries
├── agents/           # Agent definitions and default configurations
├── docs/             # Full documentation
│   ├── architecture.md
│   ├── plugin-system.md
│   ├── agent-teams-system.md
│   ├── ai-firm-system.md
│   ├── echo-debug-agent.md
│   ├── store-concept.md
│   └── api-reference.md
└── installer/compose/
```

---

## Documentation

Roadmaps and release planning docs live in [`docs/roadmaps/`](./docs/roadmaps/):

- [Implementation Roadmap](./docs/roadmaps/SYNAPSE_IMPLEMENTATION_ROADMAP.md) - Versioned implementation sequence and fixed roadmap labels
- [V3 Roadmap](./docs/roadmaps/SYNAPSE_V3_IMPLEMENTATION_ROADMAP.md) - Current feature-milestone execution path
- [Docs Roadmap](./docs/roadmaps/SYNAPSE_DOCS_ROADMAP.md) - Documentation planning track

Product/API/operator documentation is maintained in the separate docs repository:

- **Live Docs**: https://ftmahringer.github.io/Synapse/
- **Docs Repo**: https://github.com/FTMahringer/Synapse-docs

---

## Contributing

We welcome contributions of all kinds — bug fixes, new plugins, documentation improvements, and community bundles.

See [CONTRIBUTING.md](./CONTRIBUTING.md) for the full guide, including:

- How to fork, branch, and submit a pull request
- Plugin and bundle submission process
- Skill publishing workflow
- Code style requirements
- Issue reporting guidelines
- Community store vs official store distinction

---

## License

{SYSTEM_NAME} is released under the [MIT License](./LICENSE).

Copyright 2026 SYNAPSE Contributors.
