# Changelog

All notable project changes are tracked here once they become part of a roadmap milestone.

## v1.0.7-dev - User CRUD with Password Hashing

### Added

- Added spring-boot-starter-security dependency for password hashing and security infrastructure.
- Added PasswordHashingService using BCrypt with strength 12 for password hashing and verification.
- Added SecurityConfig with disabled CSRF and stateless session management (JWT preparation).
- Enhanced UserService with create(), update(), and updatePassword() methods with password hashing.
- Added UserController with REST endpoints (GET, POST, PATCH users, PATCH password, DELETE).
- Added UpdatePasswordRequest and UpdateUserRequest DTOs.
- Added structured logging for user creation, updates, password changes, and deletion.
- Username and email uniqueness validation on create and update operations.

### Notes

- `v1.0.7-dev` starts the v1.2.0-dev milestone (Auth and Users).
- BCrypt strength set to 12 for balance between security and performance.
- SecurityConfig currently permits all requests (authentication enforcement in v1.0.10-dev).
- PATCH /api/users/{id} supports partial updates (only provided fields updated).
- PATCH /api/users/{id}/password allows password changes without exposing hash.
- User updates track before/after values for username, email, and role.

## v1.0.6-dev - API Error Model and Request Correlation

### Added

- Added CorrelationIdFilter to generate and track correlation IDs across requests.
- Added RequestLoggingFilter for structured HTTP request/response logging.
- Added HTTP log category to LogCategory enum.
- Enhanced GlobalExceptionHandler to extract correlation IDs from MDC (SLF4J Mapped Diagnostic Context).
- Added generic Exception handler for unhandled exceptions with structured logging.
- Added X-Correlation-ID header to all HTTP responses.
- Request correlation IDs automatically propagated to system logs and error responses.
- HTTP request logging includes method, path, status, duration, and correlation ID.

### Notes

- `v1.0.6-dev` completes the sixth and final patch step of the v1.1.0-dev milestone (Persistence API Layer).
- Correlation IDs provided in X-Correlation-ID request header are preserved; otherwise auto-generated.
- CorrelationIdFilter uses SLF4J MDC to make correlation ID available throughout request lifecycle.
- RequestLoggingFilter excludes health check and actuator endpoints from logging.
- Error responses now include both correlationId and traceId for debugging.
- Generic exception handler catches all unhandled exceptions with stack trace logging.

## v1.0.5-dev - Task and Task Log APIs

### Added

- Added TaskController with REST endpoints (GET, POST, PATCH, DELETE tasks, GET task logs).
- Added TaskLogDTO and UpdateTaskRequest DTOs for task API contracts.
- Enhanced TaskService with structured logging and automatic task log creation.
- Added task log tracking for all task mutations (create, update, delete).
- Added change tracking in task updates to log only modified fields.
- Added GET /api/tasks endpoint with optional projectId query parameter filter.
- Added GET /api/tasks/{id}/logs endpoint to retrieve task event history.
- Task logs automatically created on task creation and updates with change details.

### Notes

- `v1.0.5-dev` completes the fifth patch step of the v1.1.0-dev milestone (Persistence API Layer).
- PATCH /api/tasks/{id} supports partial updates (only provided fields updated).
- Task updates track before/after values for title, status, assignedAgentId, and size.
- Task logs provide audit trail of all task lifecycle events.
- GET /api/tasks?projectId={uuid} filters tasks by project.

## v1.0.4-dev - Settings and System Metadata APIs

### Added

- Added SystemMetadata domain entity with singleton pattern (id constrained to TRUE).
- Added SystemMetadataRepository for system_metadata table access.
- Added SystemMetadataService with metadata CRUD and settings merge operations.
- Added SystemMetadataController with REST endpoints (GET /api/system/metadata, PUT /api/system/metadata, GET /api/system/settings, PATCH /api/system/settings).
- Added SystemMetadataDTO and UpdateSystemMetadataRequest DTOs.
- Added structured logging for metadata and settings updates.
- Settings PATCH endpoint merges incoming settings with existing settings rather than replacing.

### Notes

- `v1.0.4-dev` completes the fourth patch step of the v1.1.0-dev milestone (Persistence API Layer).
- System metadata is a singleton table with id=true constraint.
- GET /api/system/metadata returns platform name, version, and full settings.
- PATCH /api/system/settings merges partial updates into existing settings.
- Default metadata created on first access with name="SYNAPSE", version="1.0.0".

## v1.0.3-dev - Agent and Team CRUD with File Bootstrap

### Added

- Added AgentManagementService with hybrid file+database agent loading strategy.
- Added AgentManagementController with REST endpoints for agents CRUD (GET, POST, PUT, DELETE).
- Added POST /api/agents/_sync endpoint to import file-based agents into database.
- Added AgentTeamService with CRUD operations for teams.
- Added AgentTeamController with REST endpoints for teams (GET, POST, PUT, DELETE).
- Added AgentTeamDTO and CreateAgentTeamRequest DTOs for team API contracts.
- Added API log category to LogCategory enum for request/response logging.
- Added structured logging for all agent and team mutating operations.
- Added DtoMapper methods for AgentTeam entity-DTO conversion.

### Notes

- `v1.0.3-dev` completes the third patch step of the v1.1.0-dev milestone (Persistence API Layer).
- File-based agents (from `agents/` directory) are merged with database agents in listAllAgents().
- File agents are marked with `"source": "file"` in their config field.
- Agents can be synced from file definitions into database via POST /_sync endpoint.
- Database agents take precedence over file-based agents with same ID.

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
