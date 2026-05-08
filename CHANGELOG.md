# Changelog

All notable project changes are tracked here once they become part of a roadmap milestone.

## v1.0.2-dev - DTOs and Validation

### Added

- Added stable DTO records for all domain entities (UserDTO, AgentDTO, ConversationDTO, MessageDTO, TaskDTO, PluginDTO).
- Added validated request DTOs with Jakarta Bean Validation constraints (CreateUserRequest, CreateAgentRequest, CreateConversationRequest, CreateMessageRequest, CreateTaskRequest).
- Added DtoMapper utility class for bidirectional entity-DTO conversion.
- Added JacksonConfig for ObjectMapper bean with JavaTimeModule support.
- Added Map-based logging method to SystemLogService for structured logging integration.
- Added validation error handler in GlobalExceptionHandler for MethodArgumentNotValidException.
- Added jackson-datatype-jsr310 dependency to pom.xml for Instant/LocalDateTime serialization.

### Notes

- `v1.0.2-dev` completes the second patch step of the v1.1.0-dev milestone (Persistence API Layer).
- DTOs use Jakarta validation annotations for input validation.
- All DTOs use JsonInclude.NON_NULL to omit null fields in responses.
- DtoMapper provides clean separation between domain model and API contracts.

## v1.0.1-dev - Persistence API Layer

### Added

- Added JPA domain entities for users, agents, teams, plugins, conversations, messages, projects, tasks, and task logs with PostgreSQL schema mappings.
- Added Spring Data repositories for all domain entities with query methods for common access patterns.
- Added service layer with @Transactional boundaries for users, agents, conversations, messages, tasks, and plugins.
- Added global exception handling with ApiException base class, ResourceNotFoundException, ValidationException, and ErrorResponse DTO.
- Added correlation ID and trace ID support in exception handling for request tracing.
- Added structured logging for API errors through SystemLogService integration.
- Created repository/service/exception package structure in backend core module.

### Notes

- `v1.0.1-dev` completes the first patch step of the v1.1.0-dev milestone (Persistence API Layer).
- Database-backed CRUD services are now available but not yet exposed through REST controllers.
- File-defined agent bootstrap integration still pending.

## v1.0.0 - Initial Runnable Platform Release

### Added

- Added the complete documentation scaffold for the platform architecture, agent identity model, teams, AI-Firm, plugin system, store, bundles, logging, dashboard theming, installer, CLI contract, ACP registry, MCP, skills, heartbeat, self-learning, multi-user support, Git providers, custom commands, ECHO, and API reference.
- Added root project documentation, contribution guidance, MIT license, build-step checklist, implementation roadmap, and release tracking.
- Added PostgreSQL schema and seed data covering agents, teams, AI-Firm, plugins, channels, model providers, skills, MCP, conversations, messages, projects, tasks, logs, costs, heartbeat, sessions, store cache, users, auth sessions, and Git integrations.
- Added agent identity files for the Main Agent, ECHO debug agent, AI-Firm CEO, and reusable agent/team templates.
- Added plugin templates for channels, model providers, skills, MCP, and a Telegram channel skeleton.
- Added store registry, bundle specification, store specification, and plugin submission guide.
- Added Unix/macOS and Windows installer scripts plus Docker Compose quick/dev and production stacks.
- Added the first Spring Boot backend runtime under `packages/core` with health API, configuration properties, Dockerfile, Flyway migration files, explicit migration execution, file-defined agent listing, and structured logging APIs.
- Added the first Vue/Vite dashboard runtime under `packages/dashboard/frontend` with health, agents, and recent-log panels.
- Added Nginx API proxying for the packaged dashboard container.
- Added release automation support through roadmap-label and milestone-release workflows.

### Fixed

- Corrected README documentation links and project structure references.
- Normalized text file line endings for backend, dashboard, SQL, XML, Vue, TypeScript, and Nginx config files.
- Made the backend health API independent of Actuator internals.
- Updated Docker Compose PostgreSQL 18 volume handling and added service health checks for deterministic startup.
- Filtered runtime agent listing so scaffold templates are not exposed as concrete agents.

### Validation

- Backend Docker image builds successfully.
- Dashboard Docker image builds successfully.
- Docker Compose config validates for quick/dev and production files.
- The local runtime starts PostgreSQL, Redis, Qdrant, backend, and dashboard.
- `GET /api/health` responds through both backend port `8080` and dashboard proxy port `3000`.
- `GET /api/agents` returns concrete runtime agents.
- `GET /api/logs` returns persisted structured startup logs.

### Notes

- `v1.0.0` is the first runnable MVP baseline.
- The release does not yet include live chat execution, model invocation, realtime log streaming, user authentication, plugin installation, or full dashboard management workflows. Those move to the V2 roadmap.

## v1.0.0-dev - Development Baseline

### Added

- Added the v1 implementation roadmap for the runnable Spring Boot backend, Vue dashboard, database migrations, APIs, model provider shell, Compose runtime, and first release hardening.
- Added the first Spring Boot backend skeleton with health API, configuration properties, Maven project file, and Dockerfile.
- Added the first Vue/Vite dashboard skeleton with health API client, operator console shell, and Dockerfile.
- Added structured log categories, log write/query service, `/api/logs`, startup logging, and dashboard recent-log display.
- Added explicit Flyway migration execution during backend startup so the first runnable stack creates its schema before writing runtime logs.
- Added Nginx API proxying for the packaged dashboard container.

### Fixed

- Updated Docker Compose PostgreSQL 18 volume handling and service health checks.
- Filtered runtime agent listing so scaffold templates are not exposed as concrete agents.

### Notes

- `v1.0.0-dev` marked the active implementation track before the `v1.0.0` release.

## v0.10.0 - Hardening

### Added

- Added the hardening report with file-count verification, critical-file status, validation notes, and quality-rule notes.

### Fixed

- Corrected README documentation links to match the actual documentation file names.
- Corrected the README project structure to point at `installer/compose/` instead of a root Compose file.

### Notes

- `v0.10.0` closes the initial implementation-roadmap pass.

## v0.9.0 - Runtime Delivery

### Added

- Added runtime delivery documentation for WebSocket, SSE, polling fallback, dashboard blocks, operator paths, and failure rules.
- Documented that runtime transport failures never activate ECHO automatically.

### Notes

- `v0.9.0` closes the runtime delivery milestone.
- The next milestone is `v0.10.0`, focused on hardening and final quality checks.

## v0.8.0 - CLI Contract

### Added

- Added the CLI reference with global flags, command tree, TUI views, Main Agent path, manual path, and logging behavior.
- Linked the API reference to the dedicated CLI contract.

### Notes

- `v0.8.0` closes the CLI specification milestone.
- The next milestone is `v0.9.0`, focused on runtime delivery and observability.

## v0.7.0 - Installer

### Added

- Added Unix/macOS shell installer with interactive prompts and Docker Compose startup.
- Added Windows PowerShell installer with equivalent prompt and Compose behavior.
- Added quick/dev Docker Compose file for PostgreSQL, Redis, Qdrant, and optional Ollama.
- Added production Docker Compose file with restart policies and internal networking.

### Notes

- `v0.7.0` closes the installer milestone.
- The next milestone is `v0.8.0`, focused on the CLI command contract.

## v0.6.0 - Documentation Set

### Added

- Added the missing subsystem docs for agents, teams, AI-Firm, heartbeat, skills, MCP, ACP, bundles, multi-user, logging, theming, ECHO, git providers, custom commands, and API reference.
- Documented Main Agent and manual paths for creatable and configurable subsystems.
- Added logging categories, API endpoints, and operator guidance required by the build-step checklist.

### Notes

- `v0.6.0` closes the documentation milestone.
- The next milestone is `v0.7.0`, focused on installers and Compose bootstrap files.

## v0.5.0 - Store and Bundle System

### Added

- Added an example store registry with official, community, skills.sh, and ACP sources.
- Added example plugin, bundle, and statistics entries for store cache structure.
- Added the plugin and bundle submission guide covering Official Store, Community Store, skills publishing, manual installs, and logging.
- Completed the store file count required by the original build steps.

### Notes

- `v0.5.0` closes the store milestone.
- The next milestone is `v0.6.0`, focused on completing the documentation set.

## v0.4.0 - Plugin Templates

### Added

- Added the Ollama model provider manifest.
- Added the skills plugin manifest template and Claude Code Skills format template.
- Added the MCP server manifest template with stdio, HTTP, tool policy, and logging fields.
- Completed the plugin template file count required by the original build steps.

### Notes

- `v0.4.0` closes the plugin template milestone.
- The next milestone is `v0.5.0`, focused on the store and bundle system.

## v0.3.0 - Agent Identity Layer

### Added

- Added the AI-Firm example configuration with Paperclip mode routing, singleton constraints, and AI_FIRM logging events.
- Added Firm CEO identity, soul, and system prompt files.
- Completed the missing Step 10 agent files from the original build steps.

### Notes

- `v0.3.0` closes the agent identity milestone.
- The next milestone is `v0.4.0`, focused on plugin templates and provider integration.

## v0.2.0 - Backend Foundation

### Added

- Added stronger backend schema invariants for singleton metadata, installed plugin uniqueness, channel relationships, session-linked heartbeat records, and store cache source uniqueness.
- Added idempotent seed data for system metadata and default runtime settings.
- Added explicit manual-only ECHO debug activation to the seed configuration.
- Expanded the vault specification with path-safety and compression failure handling rules.

### Notes

- `v0.2.0` closes the backend foundation milestone.
- The next milestone is `v0.3.0`, focused on the agent identity layer.

## v0.1.0 - Foundation

### Added

- Added the implementation roadmap as the source of truth for build order, versioning, and roadmap labels.
- Added GitHub Actions automation to sync fixed roadmap labels from the roadmap file.
- Added release process documentation for patch commits, milestone commits, labels, and tags.
- Added roadmap issue and pull request templates.
- Added repository line-ending rules for stable cross-platform diffs.

### Notes

- `v0.1.0` is the first implementation milestone after the scaffold baseline.
- The next milestone is `v0.2.0`, focused on the backend foundation.

## v0.0.0 - Scaffold Baseline

### Added

- Added the initial project plan and build-step plan.
- Added root project files, agent files, backend database and vault specs, partial docs, plugin templates, and store specs.
