# Changelog

All notable project changes are tracked here once they become part of a roadmap milestone.

## v1.2.5-dev - Anthropic Provider

### Added

- Added AnthropicModels DTOs for Anthropic API requests and responses.
- Added AnthropicProviderService for Anthropic Claude models.
- Added AnthropicProviderController with health and chat endpoints.
- Support for ANTHROPIC provider type.
- GET /api/providers/anthropic/{id}/health for health checks.
- POST /api/providers/anthropic/{id}/chat for chat completions.
- Support for temperature, top_p, top_k, max_tokens parameters.
- API key authentication via x-api-key header.
- Anthropic API version: 2023-06-01.
- Token usage tracking (input_tokens, output_tokens).
- Support for content blocks in responses (text type).
- Structured logging with token usage and latency.

### Notes

- `v1.2.5-dev` continues the v1.3.0-dev milestone (Model Providers).
- Anthropic endpoint: https://api.anthropic.com/v1/messages.
- Messages API format (different from OpenAI).
- Max tokens required parameter for Anthropic API.
- Response includes content blocks with type and text.
- Stop reasons tracked: end_turn, max_tokens, stop_sequence.
- Failed API calls logged without exposing API keys.
- Health check uses minimal test request (10 tokens) with claude-3-5-haiku model.

## v1.2.4-dev - OpenAI-Compatible Provider

### Added

- Added OpenAIModels DTOs for OpenAI API requests and responses.
- Added OpenAIProviderService for OpenAI and OpenAI-compatible providers.
- Added OpenAIProviderController with health, models, and chat endpoints.
- Support for OPENAI and OPENAI_COMPATIBLE provider types.
- GET /api/providers/openai/{id}/health for health checks.
- GET /api/providers/openai/{id}/models for listing available models.
- POST /api/providers/openai/{id}/chat for chat completions.
- Support for temperature, max_tokens, top_p, frequency_penalty, presence_penalty.
- API key authentication via Authorization: Bearer header.
- Default baseUrl: https://api.openai.com (configurable for compatible providers).
- Token usage tracking (prompt_tokens, completion_tokens, total_tokens).
- Structured logging with token usage and latency.

### Notes

- `v1.2.4-dev` continues the v1.3.0-dev milestone (Model Providers).
- Supports both OpenAI and compatible providers (OpenRouter, Azure OpenAI, etc.).
- API key stored encrypted in provider secrets.
- Compatible providers configure custom baseUrl in provider config.
- Standard OpenAI API format: /v1/models, /v1/chat/completions.
- Streaming disabled (stream: false) for synchronous responses.
- Failed API calls logged without exposing API keys.

## v1.2.3-dev - Ollama Chat Completion

### Added

- Added OllamaChat DTOs for chat completion requests and responses.
- Added chatCompletion() method to OllamaProviderService.
- Added POST /api/providers/ollama/{id}/chat endpoint for chat completions.
- Support for temperature, top_p, top_k, num_predict options in chat requests.
- Token counting in responses (prompt_eval_count, eval_count).
- Duration tracking for chat completions (total_duration, load_duration, eval_duration).
- Structured logging for chat completions with token counts and latency.
- Failed chat attempts logged with error details and duration.

### Notes

- `v1.2.3-dev` continues the v1.3.0-dev milestone (Model Providers).
- Ollama chat endpoint: /api/chat (POST).
- Streaming disabled (stream: false) for synchronous responses.
- Request includes model, messages (role + content), and optional options.
- Response includes assistant message, token counts, and timing information.
- Errors logged without exposing sensitive prompt content.

## v1.2.2-dev - Ollama Provider Health and Model Listing

### Added

- Added OllamaProviderService for Ollama provider integration.
- Added OllamaProviderController with health check and model listing endpoints.
- Added OllamaModels DTOs for Ollama API response parsing.
- GET /api/providers/ollama/{id}/health endpoint for provider health checks.
- GET /api/providers/ollama/{id}/models endpoint for listing available Ollama models.
- RestClient integration for HTTP calls to Ollama API.
- Default Ollama baseUrl: http://localhost:11434 (configurable via provider config).
- Structured logging for health checks and model listing operations.
- Model info includes name, size, digest, format, family, and quantization details.

### Notes

- `v1.2.2-dev` continues the v1.3.0-dev milestone (Model Providers).
- Ollama API endpoint: /api/tags for both health check and model listing.
- Health check returns true if Ollama responds, false on error.
- Failed health checks logged as warnings (not errors).
- Provider baseUrl configurable in provider config JSONB field.
- Model listing includes all models available in Ollama instance.

## v1.2.1-dev - Model Provider Configuration with Encrypted Secrets

### Added

- Added ModelProvider domain entity with support for OLLAMA, OPENAI, ANTHROPIC, OPENAI_COMPATIBLE types.
- Added ModelProviderRepository with query methods for enabled providers and by type.
- Added SecretEncryptionService using AES-256-GCM for encrypting API keys and secrets.
- Added ModelProviderService with CRUD operations and secret encryption/decryption.
- Added ModelProviderController with REST endpoints (GET, POST, PATCH, DELETE).
- Added ModelProviderDTO, CreateModelProviderRequest, UpdateModelProviderRequest DTOs.
- Provider configuration stored in JSONB config field for flexibility.
- Secrets encrypted at rest using AES-256-GCM with random IV per encryption.
- Structured logging for provider creation, updates, and deletion.
- GET /api/providers?enabled=true to filter only enabled providers.

### Notes

- `v1.2.1-dev` starts the v1.3.0-dev milestone (Model Providers).
- Encryption key configurable via secrets.encryption-key property (MUST change in production).
- Encryption key must be exactly 32 bytes for AES-256.
- Secrets never exposed in API responses (encrypted_secrets field excluded from DTOs).
- GCM mode provides both confidentiality and authenticity of encrypted secrets.
- Provider config supports flexible JSON structure for provider-specific settings.

## v1.2.0 - Auth and Users (Milestone Release)

**Release Date:** 2026-05-08

This milestone delivers a complete authentication and user management system with modern password hashing and JWT-based stateless authentication.

### Added

- **User Management with Argon2id Password Hashing**
  - Spring Security dependency for authentication infrastructure
  - PasswordHashingService using Argon2id algorithm (64MB memory, 3 iterations, parallelism=1)
  - Argon2id chosen for resistance to GPU cracking and side-channel attacks
  - Bouncy Castle provider for Argon2 implementation
  - Constant-time hash comparison to prevent timing attacks
  - PHC string format: `$argon2id$v=19$m=65536,t=3,p=1$[salt]$[hash]`

- **User CRUD APIs**
  - UserController with REST endpoints (GET, POST, PATCH, DELETE)
  - Enhanced UserService with create(), update(), and updatePassword() methods
  - UpdatePasswordRequest and UpdateUserRequest DTOs
  - Username and email uniqueness validation
  - Structured logging for user operations (create, update, password change, delete)
  - Change tracking for user updates (before/after values)

- **JWT Infrastructure**
  - JJWT library version 0.12.6 for JWT token generation and validation
  - JwtService for access and refresh token management
  - Access tokens valid for 15 minutes (configurable via jwt.access-token-validity-ms)
  - Refresh tokens valid for 7 days (configurable via jwt.refresh-token-validity-ms)
  - Tokens signed with HS256 (HMAC-SHA256) algorithm
  - Token claims: userId, username, role, token type (access/refresh)
  - JWT secret configurable via jwt.secret property

- **Authentication Endpoints**
  - AuthenticationService with login() and refreshToken() methods
  - AuthenticationController with POST /api/auth/login and POST /api/auth/refresh
  - LoginRequest and RefreshTokenRequest DTOs
  - Structured logging for login attempts (success and failure) and token refreshes
  - BadCredentialsException for invalid credentials

- **JWT Authentication and Authorization**
  - JwtAuthenticationFilter extracting and validating JWT from Authorization header
  - SecurityContextHelper utility for accessing current user context
  - Method-level security enabled with @EnableMethodSecurity
  - All /api/** endpoints require authentication except /api/auth/** and /api/health
  - Bearer token format: `Authorization: Bearer <access_token>`
  - Spring Security context populated with userId, username, and role
  - GrantedAuthority with ROLE_ prefix for role-based authorization
  - JwtAuthenticationDetails attached to authentication for user metadata access

- **Security Configuration**
  - SecurityConfig with CSRF disabled and stateless session management
  - Public endpoints: /api/auth/**, /api/health, /actuator/**
  - All other endpoints require valid JWT access token
  - Method security annotations (@PreAuthorize, @Secured) functional

### Notes

- This milestone includes patches v1.0.7-dev through v1.0.10-dev
- BREAKING CHANGE: All API endpoints now require authentication (except auth and health)
- JWT secret MUST be changed in production (default is for development only)
- Argon2id 64MB memory requirement makes brute-force attacks computationally expensive
- Only access tokens accepted for authentication (refresh tokens only for token refresh endpoint)

## v1.0.10-dev - JWT Authentication and Authorization

### Added

- Added JwtAuthenticationFilter to extract and validate JWT tokens from Authorization header.
- Added SecurityContextHelper utility for accessing current user context.
- Enhanced SecurityConfig to enforce authentication on all endpoints except auth and health.
- Enabled method-level security with @EnableMethodSecurity annotation.
- JWT filter extracts userId, username, and role from access tokens into Spring Security context.
- Access tokens populate GrantedAuthority with ROLE_ prefix for role-based authorization.
- JwtAuthenticationDetails attached to authentication for accessing user metadata.

### Notes

- `v1.0.10-dev` completes the v1.2.0-dev milestone (Auth and Users).
- All /api/** endpoints now require authentication except /api/auth/** and /api/health.
- Bearer token format: `Authorization: Bearer <access_token>`.
- Invalid or expired tokens silently skip authentication (401 returned by framework).
- SecurityContextHelper provides getCurrentUserId(), getCurrentUsername(), getCurrentUserRole().
- Method security annotations (@PreAuthorize, @Secured) now functional.
- Only access tokens accepted (refresh tokens rejected in filter).

## v1.0.9-dev - Switch to Argon2 Password Hashing

### Changed

- Replaced BCrypt with Argon2id for password hashing (more secure and modern).
- Added Bouncy Castle dependency (bcprov-jdk18on) for Argon2 implementation.
- Updated PasswordHashingService to use Argon2id with 64MB memory, 3 iterations, parallelism=1.
- Hash format: $argon2id$v=19$m=65536,t=3,p=1$[salt]$[hash] (PHC string format).
- Constant-time comparison for hash verification to prevent timing attacks.

### Notes

- `v1.0.9-dev` continues the v1.2.0-dev milestone (Auth and Users).
- Argon2id chosen for resistance to both GPU cracking and side-channel attacks.
- 64MB memory requirement makes brute-force attacks computationally expensive.
- Existing BCrypt hashes from v1.0.7-dev incompatible (users must reset passwords).
- Salt is 16 bytes, hash output is 32 bytes.

## v1.0.8-dev - JWT Infrastructure

### Added

- Added JJWT dependencies (jjwt-api, jjwt-impl, jjwt-jackson) version 0.12.6 for JWT support.
- Added JwtService for generating and validating JWT access and refresh tokens.
- Added AuthenticationService with login() and refreshToken() methods.
- Added AuthenticationController with POST /api/auth/login and POST /api/auth/refresh endpoints.
- Added LoginRequest and RefreshTokenRequest DTOs.
- Added structured logging for login attempts (success and failure) and token refreshes.
- JWT access tokens valid for 15 minutes (900000ms) by default.
- JWT refresh tokens valid for 7 days (604800000ms) by default.
- Tokens include userId, username, role, and token type claims.

### Notes

- `v1.0.8-dev` continues the v1.2.0-dev milestone (Auth and Users).
- JWT secret configurable via jwt.secret property (default included for dev, MUST change in production).
- Token validity periods configurable via jwt.access-token-validity-ms and jwt.refresh-token-validity-ms.
- Access tokens contain username and role claims for authorization.
- Refresh tokens are minimal (only userId and type) for security.
- Failed login attempts logged with reason for security monitoring.
- Tokens signed with HS256 (HMAC-SHA256) algorithm.

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
