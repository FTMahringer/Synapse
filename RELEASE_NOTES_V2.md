# v2.0.0 Release Notes

**Release Date**: TBD  
**Milestone**: SYNAPSE v2 - Operational AI Platform

This is the first production release of SYNAPSE as a working AI platform. V2 transforms the v1.0.0 runtime shell into an operational multi-user system with authentication, model provider execution, conversation management, agent orchestration, realtime event delivery, plugin lifecycle, dashboard management, and CLI tooling.

---

## What's New in V2

### 🔐 Multi-User Authentication & Authorization
- User accounts with OWNER, ADMIN, USER, and VIEWER roles
- JWT-based authentication with access and refresh tokens
- Argon2id password hashing for secure credential storage
- Role-based API access control
- Session management with token revocation

### 🤖 Model Provider Integration
- Support for Ollama (local), OpenAI, and Anthropic providers
- Provider registry with encrypted secret storage (AES-256-GCM)
- Model listing and health checks for all providers
- Cost and latency tracking
- Provider test endpoint for validation

### 💬 Chat Runtime
- Conversation creation and management
- Message persistence with full history
- Main Agent prompt assembly from identity files
- Model provider routing and execution
- Response streaming support
- Token usage and cost tracking

### 🎭 Agent Orchestration
- Agent runtime registry with activation states
- Main Agent routing with deterministic rules
- Team leader/member dispatch
- AI-Firm project/task dispatch integration
- Agent memory vault read/write hooks
- Heartbeat monitoring for active agents
- Dashboard visibility into agent states

### 📡 Realtime Event Delivery
- Server-Sent Events (SSE) for live logs
- WebSocket endpoint for conversation updates
- Redis Streams-backed event bus
- Automatic reconnection with backoff
- Polling fallback for unreliable connections

### 🔌 Plugin & Store Runtime
- Plugin manifest parser and validator
- Install, enable, disable, uninstall workflows
- Store registry sync from YAML definitions
- Bundle validation and safety rules
- Plugin stats tracking
- Community plugin safety warnings

### 🎨 Dashboard Management UI
- Vue 3 + TypeScript single-page application
- Role-aware navigation
- Full management screens for:
  - Providers
  - Conversations
  - Agents and Teams
  - Plugins and Store
  - Logs and Observability
  - Settings and Users
- Live log streaming
- Conversation event updates
- Responsive design (desktop and mobile)

### 🖥️ Go CLI & TUI
- Command-line interface for all platform APIs
- Health, logs, agents, providers, plugin commands
- Conversation send and stream
- Bubble Tea TUI overview
- Config profile handling
- ANSI formatted output

### 🏗️ Backend Restructure
- Organized Spring Boot packages by feature
- Eliminated flat `service/` dumping ground
- Clear separation: `api/` (controllers) and `service/` (business logic)
- Feature modules: agents, auth, conversation, tasks, users, provider, plugin, realtime
- All files in correct package paths
- Zero circular dependencies

### ✅ Testing & CI/CD
- 31 unit tests covering auth, providers, and conversations
- TestContainers-based integration testing framework
- GitHub Actions workflows for:
  - Frontend build and type checking
  - Flyway migration validation
  - Full Docker Compose smoke tests
- All tests passing on clean builds

---

## Milestone Breakdown

### v1.1.0 - Persistence API Layer
- Repository/service boundaries for all core entities
- Stable DTOs and validation classes
- Agent and team CRUD with file bootstrap
- Settings and system metadata APIs
- Task and task log APIs
- API error model with correlation IDs
- Structured logging for mutating endpoints

### v1.2.0 - Auth and Users
- Password hashing with Argon2id
- User bootstrap and owner account initialization
- Login, refresh, logout, and session revocation APIs
- JWT verification middleware
- Role annotations (OWNER, ADMIN, USER, VIEWER)
- Role-scoped API enforcement
- Dashboard login and role-aware navigation
- Audit logs for auth events

### v1.3.0 - Model Providers
- Provider configuration with encrypted secret storage
- Ollama provider: health, models, chat completion
- OpenAI-compatible provider path
- Anthropic provider integration
- Provider cost and latency logging
- Test endpoint (no persistence by default)

### v1.4.0 - Chat Runtime
- Conversation create/list/detail APIs
- Message send API with persistence
- Main Agent prompt assembly
- Model provider execution
- Response metadata persistence (tokens, latency, provider)
- Cancellation and timeout handling
- Dashboard conversation view

### v1.5.0 - Agent Orchestration
- Agent runtime registry and activation states
- Main Agent router service with routing logs
- Team leader/member dispatch contract
- AI-Firm project/task dispatch entry point
- Agent memory vault hooks
- Heartbeat records
- Dashboard agent management

### v1.6.0 - Realtime Runtime
- Internal event publisher abstraction
- Redis Streams-backed log fanout
- SSE endpoint for live logs
- WebSocket endpoint for conversation events
- Dashboard live log panel
- Dashboard streaming conversation updates
- Reconnect, backoff, polling fallback

### v1.7.0 - Plugin and Store Runtime
- Manifest parser/validator for channels, models, skills, MCP plugins
- Plugin install/enable/disable/uninstall APIs
- Store registry sync from `store/registry.yml`
- Bundle validation and install flow
- Plugin stats tracking
- Dashboard plugin/store management
- Safety rules for community plugin installs

### v1.8.0 - Dashboard Management
- Vue Router, Pinia state management, view structure
- Authenticated layout and role-aware navigation
- Providers screen
- Conversations screen
- Agents and teams screen
- Plugins and store screens
- Logs and observability screen
- Settings and user management screens
- Loading, empty, error, and unauthorized state handling

### v1.9.0 - CLI Runtime
- Go module and cobra command shell
- Auth login/logout/session commands
- Health, logs, agents, providers, plugin commands
- Conversation send and stream
- Bubble Tea TUI overview
- Config profile handling

### v1.10.0 - Backend Restructure
- Audit of misplaced files (RESTRUCTURE.md)
- Created `agents/api/` and `agents/service/` sub-packages
- Moved TaskController/UserController to tasks/users
- Moved all services out of flat `service/` package
- Renamed `security/` to `auth/`
- Reorganized `domain/`, `dto/`, `repository/`, `exception/` under `shared/`
- All 11 Flyway migrations pass on clean volume
- Docker Compose starts cleanly

### v1.11.0 - Release Hardening
- **v1.10.1-dev**: Docker Compose validation (all services healthy, migrations pass)
- **v1.10.2-dev**: 31 backend unit tests (auth, providers, conversations)
- **v1.10.3-dev**: TestContainers integration test framework
- **v1.10.4-dev**: CI workflows (frontend, migrations, smoke tests)
- **v1.10.5-dev**: Documentation updates (README, architecture, API docs)
- **v1.10.6-dev**: Compiled v2.0.0 release notes
- **v2.0.0**: Production release tag

---

## Breaking Changes from v1.0.0

- **Authentication Required**: All management APIs now require JWT authentication
- **Package Structure**: Backend packages reorganized — imports must be updated if extending
- **Environment Variables**: `JWT_SECRET` and `SECRETS_ENCRYPTION_KEY` now required
- **ECHO Mode**: Remains manual-only, no automatic fallback

---

## Migration Guide

### From v1.0.0 to v2.0.0

1. **Set Required Environment Variables**:
   ```bash
   JWT_SECRET=your-256-bit-secret-here
   SECRETS_ENCRYPTION_KEY=your-32-byte-key-here
   ```

2. **Database Migrations**:
   - Migrations V1-V11 will run automatically on first startup
   - No manual intervention required

3. **Create Owner Account**:
   - On first startup, create an owner account via bootstrap or API
   - Use owner credentials to create additional users

4. **Update API Calls**:
   - Add `Authorization: Bearer <token>` header to all protected endpoints
   - Login via `/api/auth/login` to obtain tokens

---

## System Requirements

- **Java**: 25+
- **PostgreSQL**: 18+
- **Redis**: 8+
- **Node.js**: 22+ (for dashboard development)
- **Go**: 1.26+ (for CLI)
- **Docker**: 20.10+ (for Compose deployment)

---

## Installation

### Docker Compose (Recommended)

```bash
git clone https://github.com/FTMahringer/Synapse.git
cd Synapse/installer/compose
docker compose up -d
```

Services will be available at:
- Backend API: http://localhost:8080
- Dashboard: http://localhost:3000
- PostgreSQL: localhost:5432
- Redis: localhost:6379

---

## Documentation

- [README](README.md) - Project overview and quick start
- [CONTRIBUTING](CONTRIBUTING.md) - Contribution guidelines
- [SYNAPSE_V2_IMPLEMENTATION_ROADMAP](SYNAPSE_V2_IMPLEMENTATION_ROADMAP.md) - Detailed milestone plan
- [RESTRUCTURE](RESTRUCTURE.md) - Backend package reorganization notes

---

## Known Limitations

- ECHO mode remains debug-only and manual
- Plugin runtime does not yet support auto-updates
- Realtime WebSocket does not support horizontal scaling (single instance only)
- CLI does not yet support interactive TUI for all commands

---

## Contributors

Thanks to all contributors who made V2 possible!

Special thanks to:
- GitHub Copilot for code generation assistance

---

## What's Next

See [SYNAPSE_V2_IMPLEMENTATION_ROADMAP.md](SYNAPSE_V2_IMPLEMENTATION_ROADMAP.md) for future milestones and planned features.

Potential v2.1.0 features:
- WebSocket horizontal scaling with Redis pub/sub
- Plugin auto-update mechanism
- Advanced conversation branching
- Multi-modal message support
- Enhanced observability and metrics

---

**Full Changelog**: v1.0.0...v2.0.0
