# Changelog

All notable changes to the SYNAPSE project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [v2.2.4-dev] - 2026-05-10

### Performance
- Tuned HikariCP defaults and environment-driven pool settings
- Tuned Redis Lettuce pool settings and timeouts
- Added pooled outbound HTTP client configuration for provider services

### Documentation
- Updated release and workflow guidance for milestone sequencing and docs-coupled releases

---

## [v2.0.0-hotfix] - 2026-05-10

**Security hotfix for v2.0.0 release.**

### Security
- Updated `org.bouncycastle:bcprov-jdk18on` from 1.78.1 to 1.84
  - Fixed CVE-2026-5598 (HIGH): Covert timing channel vulnerability
  - Fixed CVE-2026-0636 (MEDIUM): LDAP injection vulnerability
- Enabled CSRF protection with `CookieCsrfTokenRepository`
  - Added `CsrfTokenRequestAttributeHandler` for SPA support
  - Exempted public endpoints from CSRF requirements
  - Configured for stateless JWT + SPA architecture
- Added `permissions: contents: read` to all GitHub Actions workflows
  - frontend-ci.yml
  - compose-smoke-test.yml
  - migration-ci.yml

---

## [v2.0.0] - 2026-05-10

**Milestone:** SYNAPSE v2 - Operational AI Platform

This is the first production release of SYNAPSE as a working AI platform. V2 transforms the v1.0.0 runtime shell into an operational multi-user system with authentication, model provider execution, conversation management, agent orchestration, realtime event delivery, plugin lifecycle, dashboard management, and CLI tooling.

### Added
- Multi-user authentication & authorization with JWT
- Password hashing with Argon2id
- Secrets encryption service for API keys
- Security context helpers and filters
- Model provider integrations (OpenAI, Anthropic, Ollama)
- Provider usage logging and analytics
- Conversation & message management
- Agent orchestration system
- Agent teams and collaboration
- AI Firm dispatch system
- Agent memory vault with vector storage
- Agent heartbeat monitoring
- Realtime event delivery (SSE + WebSocket)
- Redis Streams for event fanout
- Plugin lifecycle management
- Plugin safety validation
- Store registry synchronization
- Bundle installation system
- Vue.js dashboard management UI
- Authenticated routing and navigation
- Multiple management screens (conversations, agents, providers, plugins, logs, settings)
- Go CLI runtime with Cobra
- CLI commands for auth, agents, providers, plugins, conversations, logs, health
- Conversation streaming in CLI with TUI
- Docker Compose smoke test workflow
- Frontend CI workflow (Vue build/typecheck)
- Migration validation workflow
- 31 unit tests for core services
- Integration test framework with TestContainers
- Comprehensive release documentation

### Changed
- Backend restructured into feature modules (v1.10.0)
- Eliminated flat `service/` package
- Organized into domain-driven packages: `agents/`, `conversation/`, `tasks/`, `users/`, `providers/`, `plugin/`
- Updated all service imports and dependencies
- Improved Docker Compose configuration
- Enhanced environment variable handling

### Fixed
- UUID/String type mismatches in agents
- Redis Streams API configuration
- @EnableAsync proxy conflicts
- Plugin_stats migration conflict
- TypeScript HeadersInit type errors
- Bubbletea dependency compile errors
- Missing imports in various services
- Docker Compose startup issues

### Development Versions
- v1.10.6-dev: Documentation updates
- v1.10.5-dev: Release notes compilation
- v1.10.4-dev: CI/CD workflows
- v1.10.3-dev: Integration test framework
- v1.10.2-dev: Backend unit tests
- v1.10.1-dev: Docker Compose validation

---

## [v1.10.0] - 2026-05-10

**Milestone:** Backend Restructure

Major refactoring of the Java backend to improve modularity and maintainability.

### Changed
- Restructured backend packages from flat `service/` to feature-based organization
- Created feature modules: `agents/`, `conversation/`, `tasks/`, `users/`
- Moved services to feature-specific `service/` subpackages
- Updated all imports across the codebase

### Development Versions
- v1.9.5-dev: Docker Compose validation
- v1.9.4-dev: Delete service/ package, distribute remaining services
- v1.9.3-dev: Move agent services to agents/service/
- v1.9.2-dev: Move tasks/ and users/ packages
- v1.9.1-dev: Backend restructure audit

### Fixed
- v1.9.7-hotfix: Missing ModelProviderService import in MessageService
- v1.9.6-hotfix: Missing MainAgentPromptService import in MessageService

---

## [v1.9.0] - 2026-05-09

**Milestone:** CLI Runtime

Complete Go-based CLI implementation for SYNAPSE platform management.

### Added
- Go module initialization with go 1.26
- Cobra command framework
- Auth commands (login, logout, session)
- Agent management commands
- Provider management commands
- Plugin management commands
- Conversation commands (list, send, stream)
- Health check command
- Logs viewing command
- Bubble Tea TUI for conversation streaming
- Config profile handling (.synapse/config.json)
- ANSI-colored output for better UX

### Development Versions
- v1.8.6-dev: Config profile handling
- v1.8.5-dev: Bubble Tea TUI overview
- v1.8.4-dev: Conversation send and stream commands
- v1.8.3-dev: Health, logs, agents, providers, plugin commands
- v1.8.2-dev: Auth login/logout/session commands
- v1.8.1-dev: Go module and cobra command shell

### Fixed
- v1.8.8-hotfix: Drop external TUI deps, ANSI output, go 1.26
- v1.8.7-hotfix: Fix bubbletea dependency compile error

---

## [v1.8.0] - 2026-05-09

**Milestone:** Dashboard Management

Complete Vue.js dashboard implementation with authenticated navigation and management screens.

### Added
- Vue Router integration
- Pinia state management
- Authenticated layout with sidebar navigation
- Role-aware navigation (admin vs user)
- Providers screen (add/edit/test providers)
- Conversations screen (list, create, view conversations)
- Agents and teams screen (agent management, team creation)
- Plugins and store screens (plugin installation, store browsing)
- Logs and observability screen (live system logs)
- Settings and user management screens (profile, users, system settings)

### Development Versions
- v1.7.8-dev: Settings and user management screens
- v1.7.7-dev: Logs and observability screen
- v1.7.6-dev: Plugins and store screens
- v1.7.5-dev: Agents and teams screen
- v1.7.4-dev: Conversations screen
- v1.7.3-dev: Providers screen
- v1.7.2-dev: Authenticated layout and role-aware navigation
- v1.7.1-dev: Vue Router, Pinia, view structure

### Fixed
- v1.7.9-hotfix: TypeScript HeadersInit type error fix

---

## [v1.7.0] - 2026-05-09

**Milestone:** Plugin and Store Runtime

Complete plugin lifecycle management and store integration system.

### Added
- Plugin manifest parser and validator
- Plugin install/enable/disable/uninstall APIs
- Store registry sync from registry.yml
- Bundle validation and install flow
- Plugin stats tracking (installs, usage, errors)
- Dashboard plugin and store management views
- Safety rules for community plugin installs
- Plugin trust levels and safety policies

### Development Versions
- v1.6.7-dev: Safety rules for community plugin installs
- v1.6.6-dev: Dashboard plugin and store management views
- v1.6.5-dev: Plugin stats tracking
- v1.6.4-dev: Bundle validation and install flow
- v1.6.3-dev: Store registry sync from registry.yml
- v1.6.2-dev: Plugin install/enable/disable/uninstall APIs
- v1.6.1-dev: Plugin manifest parser and validator

### Fixed
- v1.6.8-hotfix: Fix V11 migration conflict with V1 plugin_stats

---

## [v1.6.0] - 2026-05-09

**Milestone:** Realtime Runtime

Realtime event delivery system with SSE and WebSocket support.

### Added
- Internal event publisher abstraction
- Redis Streams log fanout
- SSE endpoint for live logs
- WebSocket endpoint for conversation events
- Dashboard live log panel
- Dashboard streaming conversation updates
- Reconnect, backoff, polling fallback mechanisms

### Development Versions
- v1.5.7-dev: Reconnect, backoff, polling fallback
- v1.5.6-dev: Dashboard streaming conversation updates
- v1.5.5-dev: Dashboard live log panel
- v1.5.4-dev: WebSocket endpoint for conversation events
- v1.5.3-dev: SSE endpoint for live logs
- v1.5.2-dev: Redis Streams log fanout
- v1.5.1-dev: Internal event publisher abstraction

### Fixed
- v1.5.9-hotfix: Fix @EnableAsync proxy conflict
- v1.5.8-hotfix: Redis Streams API fix

---

## [v1.5.0] - 2026-05-08

**Milestone:** Agent Orchestration

Complete agent orchestration system with teams, memory, and dispatch.

### Added
- Agent runtime registry
- Main Agent router service
- Team dispatch contract
- AI-Firm project dispatch
- Agent memory vault
- Agent heartbeat records
- Dashboard agent management UI

### Development Versions
- v1.4.7-dev: Dashboard agent management
- v1.4.6-dev: Agent heartbeat records
- v1.4.5-dev: Agent memory vault
- v1.4.4-dev: AI-Firm project dispatch
- v1.4.3-dev: Team dispatch contract
- v1.4.2-dev: Main Agent router
- v1.4.1-dev: Agent runtime registry

### Fixed
- v1.4.8-hotfix: UUID/String type mismatch fix

---

## [v1.4.0] - 2026-05-08

**Milestone:** Chat Runtime

Conversation and message management with model provider integration.

### Added
- Model provider integration for chat
- Response metadata tracking
- Error handling and graceful degradation
- Chat API documentation

### Development Versions
- v1.3.7-dev: Chat API documentation
- v1.3.6-dev: Error handling and graceful degradation
- v1.3.5-dev: Response metadata tracking
- v1.3.4-dev: Model provider integration for chat

### Fixed
- v1.3.9-hotfix: Missing UUID import
- v1.3.8-hotfix: Compilation error fixes
- v1.3.0.1-hotfix: Docker Compose startup fixes

---

## [v1.3.0] - 2026-05-08

**Milestone:** Model Providers

Complete model provider management system with OpenAI, Anthropic, and Ollama support.

### Added
- Model provider CRUD APIs
- Provider configuration with encrypted secrets
- OpenAI integration
- Anthropic integration
- Ollama integration
- Provider testing endpoint
- Usage logging and analytics
- Provider switching logic

---

## [v1.2.0] - 2026-05-08

**Milestone:** Auth and Users

Multi-user authentication and authorization system.

### Added
- JWT authentication with refresh tokens
- User CRUD operations
- Password hashing with Argon2id
- Login/logout endpoints
- Session management
- Security filters and context helpers
- Secret encryption service

### Development Versions
- v1.2.7-dev: Session management
- v1.2.6-dev: Security filters
- v1.2.5-dev: Login/logout endpoints
- v1.2.4-dev: Password hashing
- v1.2.3-dev: JWT service
- v1.2.2-dev: User CRUD
- v1.2.1-dev: User entity and repository

---

## [v1.0.0] - 2026-05-08

**Milestone:** Initial Runnable Platform Release

First working version of SYNAPSE with basic runtime infrastructure.

### Added
- Spring Boot 4.0.0 application bootstrap
- PostgreSQL 18 database integration
- Redis 8 caching integration
- Qdrant vector database integration
- Flyway database migrations
- Docker Compose deployment
- Health check endpoints
- Basic domain models (Agent, Conversation, Message, User, etc.)
- Repository layer with Spring Data JPA
- System logging infrastructure
- Request correlation and logging filters

### Development Versions
- v1.0.10-dev: Logging infrastructure
- v1.0.9-dev: Filters and correlation
- v1.0.8-dev: Repositories
- v1.0.7-dev: Domain models
- v1.0.6-dev: Health endpoints
- v1.0.5-dev: Flyway migrations
- v1.0.4-dev: Qdrant integration
- v1.0.3-dev: Redis integration
- v1.0.0-dev: Spring Boot bootstrap

---

## [v0.10.0] - 2026-05-08

**Milestone:** Hardening

Pre-release hardening and quality assurance.

### Added
- Hardening report documentation
- Security checklist
- Performance baseline tests
- Error handling improvements

---

## [v0.9.0] - 2026-05-08

**Milestone:** Runtime Delivery

Runtime delivery and deployment infrastructure.

### Added
- Runtime delivery documentation
- Deployment scripts
- Installation guides

---

## [v0.8.0] - 2026-05-08

**Milestone:** CLI Contract

CLI command structure and interface definitions.

### Added
- CLI reference documentation
- Custom commands documentation
- Command specifications

---

## [v0.7.0] - 2026-05-08

**Milestone:** Installer

Installation and deployment tooling.

### Added
- Docker Compose configuration
- Installation scripts
- Environment setup documentation

---

## [v0.6.0] - 2026-05-08

**Milestone:** Documentation Set

Comprehensive documentation for all system components.

### Added
- Architecture documentation
- API reference
- System documentation (agents, teams, memory, heartbeat, logging)
- Plugin system documentation
- Store concept documentation
- MCP integration guide
- Skills integration guide
- Git provider integration documentation

---

## [v0.5.0] - 2026-05-08

**Milestone:** Store and Bundle System

Plugin store and bundle management system design.

### Added
- Store concept documentation
- Bundle system documentation
- ACP registry documentation

---

## [v0.4.0] - 2026-05-08

**Milestone:** Plugin Templates

Plugin development templates and examples.

### Added
- Plugin system documentation
- Plugin manifest specifications
- Echo debug agent example

---

## [v0.3.0] - 2026-05-08

**Milestone:** Agent Identity Layer

Agent identity and team management system design.

### Added
- Agent identity system documentation
- Agent teams system documentation
- AI firm system documentation

---

## [v0.2.0] - 2026-05-08

**Milestone:** Backend Foundation

Core backend architecture and infrastructure.

### Added
- Spring Boot project structure
- Database schema design
- API architecture
- Logging system design
- Multi-user support design

---

## [v0.1.0] - 2026-05-08

**Milestone:** Foundation

Project foundation and development infrastructure.

### Added
- Implementation roadmap (SYNAPSE_V1_IMPLEMENTATION_ROADMAP.md)
- GitHub Actions automation for roadmap label sync
- Release process documentation
- Roadmap issue and PR templates
- Repository line-ending rules (.gitattributes)
- Build order and versioning guidelines

---

## Version Index

### Major Releases
- [v2.0.0] - 2026-05-10 - Operational AI Platform
- [v1.10.0] - 2026-05-10 - Backend Restructure
- [v1.9.0] - 2026-05-09 - CLI Runtime
- [v1.8.0] - 2026-05-09 - Dashboard Management
- [v1.7.0] - 2026-05-09 - Plugin and Store Runtime
- [v1.6.0] - 2026-05-09 - Realtime Runtime
- [v1.5.0] - 2026-05-08 - Agent Orchestration
- [v1.4.0] - 2026-05-08 - Chat Runtime
- [v1.3.0] - 2026-05-08 - Model Providers
- [v1.2.0] - 2026-05-08 - Auth and Users
- [v1.0.0] - 2026-05-08 - Initial Runnable Platform
- [v0.10.0] - 2026-05-08 - Hardening
- [v0.9.0] - 2026-05-08 - Runtime Delivery
- [v0.8.0] - 2026-05-08 - CLI Contract
- [v0.7.0] - 2026-05-08 - Installer
- [v0.6.0] - 2026-05-08 - Documentation Set
- [v0.5.0] - 2026-05-08 - Store and Bundle System
- [v0.4.0] - 2026-05-08 - Plugin Templates
- [v0.3.0] - 2026-05-08 - Agent Identity Layer
- [v0.2.0] - 2026-05-08 - Backend Foundation
- [v0.1.0] - 2026-05-08 - Foundation

### Development Versions
See individual milestone sections for complete development version (v*.*.x-dev) history.

### Hotfixes
- v2.0.0-hotfix
- v1.9.7-hotfix, v1.9.6-hotfix
- v1.8.8-hotfix, v1.8.7-hotfix
- v1.7.9-hotfix
- v1.6.8-hotfix
- v1.5.9-hotfix, v1.5.8-hotfix
- v1.4.8-hotfix
- v1.3.9-hotfix, v1.3.8-hotfix, v1.3.0.1-hotfix

---

[v2.0.0-hotfix]: https://github.com/FTMahringer/Synapse/releases/tag/v2.0.0-hotfix
[v2.0.0]: https://github.com/FTMahringer/Synapse/releases/tag/v2.0.0
[v1.10.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.10.0
[v1.9.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.9.0
[v1.8.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.8.0
[v1.7.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.7.0
[v1.6.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.6.0
[v1.5.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.5.0
[v1.4.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.4.0
[v1.3.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.3.0
[v1.2.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.2.0
[v1.0.0]: https://github.com/FTMahringer/Synapse/releases/tag/v1.0.0
[v0.10.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.10.0
[v0.9.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.9.0
[v0.8.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.8.0
[v0.7.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.7.0
[v0.6.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.6.0
[v0.5.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.5.0
[v0.4.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.4.0
[v0.3.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.3.0
[v0.2.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.2.0
[v0.1.0]: https://github.com/FTMahringer/Synapse/releases/tag/v0.1.0
