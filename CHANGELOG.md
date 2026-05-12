# Changelog

All notable changes to the SYNAPSE project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

---

## [v2.5.7-dev] - 2026-05-12

**Plugin Ecosystem — Official Plugin Library**

### Added
- 4 official plugins published to `synapse-plugins` repository:
  - `telegram-channel` — Channel implementation with long-polling and webhook support
  - `anthropic-provider` — ModelProvider for Claude (API key + ACP auth, streaming, tool calling, vision)
  - `openai-provider` — ModelProvider for GPT (streaming, tool calling, organization scoping)
  - `ollama-provider` — ModelProvider for local Ollama inference (zero external API calls)
- Each plugin: manifest.yml, pom.xml, module-info.java, README.md, full implementation using only `java.net`
- No external dependencies beyond `synapse-plugin-api` — proves JPMS isolation boundary

### Fixed
- nginx WebSocket proxy configuration (`/ws/` location block with upgrade headers)
- Vite dev server WebSocket proxy config for local development

### Changed
- Dashboard version bumped to `2.5.7-dev`
- Core version bumped to `2.5.7-dev`

---

## [v2.5.6-dev] - 2026-05-12

**Dashboard — Marketplace UI**

### Added
- Enhanced `StoreView.vue` with full marketplace browsing experience:
  - Search bar filtering by name, description, and tags
  - Source filter tabs: All / Official / Community
  - Type filter tabs: All / Plugins / Bundles
  - Tag cloud filter with active state toggle
  - Card-based grid layout with hover effects
  - Detail modal with full metadata (author, license, description, tags, min Synapse version)
  - Bundle validation button with inline result display
  - Community source install confirmation flow with checkbox
- New CSS styles: `.store-grid`, `.store-card`, `.store-search`, `.tag-chip`, `.modal-overlay`, `.modal-card`, `.modal-confirm`, `.modal-validation`
- Backend `GET /api/store/{id}` endpoint for single entry lookup
- `StoreRegistryService.findById()` with caching support

### Changed
- `StoreEntry` frontend type extended with `license` and `minSynapse` fields
- Dashboard version bumped to `2.5.6-dev`
- Core version bumped to `2.5.6-dev`

---

## [v2.5.5-dev] - 2026-05-12

**Plugin Ecosystem — CLI Tooling**

### Added
- `synapse plugins` command family expanded with 14 subcommands:
  - `list` — list installed plugins with status, loader state, trust tier
  - `info <pluginId>` — detailed plugin info (name, type, version, status, loader state, trust tier, dependencies, errors)
  - `load <pluginId>` — load a plugin into the JVM
  - `unload <pluginId>` — unload a plugin from the JVM
  - `reload <pluginId>` — reload a plugin
  - `enable <pluginId>` — enable a plugin
  - `disable <pluginId>` — disable a plugin
  - `uninstall <pluginId>` — uninstall a plugin
  - `install <manifest-json>` — install a plugin from manifest JSON
  - `validate <jarPath>` — validate a plugin JAR (bytecode scan for forbidden references)
  - `resolve-deps <pluginId>` — resolve dependency chain for a plugin
  - `logs <pluginId>` — show last system logs scoped to a plugin (default 50 entries)
  - `status` — show plugin loader status (loaded plugins with type info)
  - `orphans` — list orphaned staging JARs
  - `promote` — promote all staging JARs to system/
  - `publish <pluginId>` — print plugin publishing guidance (official/community repo submission)

---

## [v2.5.4-dev] - 2026-05-12

**Plugin Ecosystem — Sandboxing & Security**

### Added
- `BytecodeScanner` — ASM-based bytecode scanner that walks all classes in a plugin JAR at install time
  - Rejects forbidden references: `sun.*`, `com.sun.*`, Spring internals, JPA/Hibernate, Redis/Lettuce, PostgreSQL driver, core SYNAPSE classes
  - Allowed packages: `dev.synapse.plugin.api.*`, `java.*`, `javax.*`, `jdk.*`, `org.slf4j.*`
  - Scans class references, method invocations, field accesses, annotations, descriptors
- `PluginSandboxService` — sandbox enforcement service:
  - `scanJar()` — runs bytecode scan and returns violations
  - `validateJpmsIsolation()` — confirms plugin module cannot resolve core modules
  - `runLifecycleHookWithTimeout()` — executes `onLoad()`/`onUnload()` with trust-tier timeouts
    - Official: 30s lifecycle, 60s message handler, 1000 logs/min
    - Community: 10s lifecycle, 30s message handler, 300 logs/min
  - Marks plugin `ERROR` + disables on lifecycle hook timeout
- `PluginSandboxController` — REST API:
  - `POST /api/plugins/sandbox/scan` — scan a JAR for forbidden references
  - `GET /api/plugins/{id}/sandbox/limits` — get resource limits for a plugin
- `PluginLoaderService` integration:
  - JPMS isolation validation before `onLoad()`
  - `onLoad()` and `onUnload()` wrapped with timeout guardrails
- `PluginLifecycleService` integration: sets sandbox defaults during install based on trust tier
- `Plugin` entity updated with sandbox fields: `scanClean`, `scanViolations`, `sandboxEnabled`, `lifecycleTimeoutMs`, `messageTimeoutMs`, `maxLogsPerMinute`
- `PluginDTO` and `DtoMapper` updated to expose sandbox fields
- ASM dependency (`org.ow2.asm:asm:9.7.1`, `asm-tree:9.7.1`) added to `packages/core/pom.xml`
- Database migration `V20__plugin_sandbox.sql` — adds sandbox state columns to `plugins` table

---

## [v2.5.3-dev] - 2026-05-12

**Plugin Ecosystem — Dependency Resolver & Conflict Detection**

### Added
- `PluginDependency` — parsed dependency declaration from manifest (`requires.plugins[]` hard deps, `requires.soft_requires[]` soft deps)
- `VersionConstraint` — semver constraint parser supporting `*`, `>=`, `>`, `<=`, `<`, `^`, `~` operators
- `DependencyGraph` — directed graph with cycle detection (DFS) and topological sort for install ordering
- `DependencyResolutionException` — typed resolution failures: `MISSING_DEPENDENCY`, `VERSION_MISMATCH`, `CYCLE_DETECTED`, `SLOT_CLASH`, `ALREADY_INSTALLED_OLDER`, `CONFIG_SCHEMA_INCOMPATIBLE`
- `PluginDependencyResolver` — core resolver service:
  - Parses manifest dependencies recursively (transitive hard deps)
  - Detects cycles before any install begins
  - Version-aware conflict checks: newer → update prompt; older → block; satisfied → proceed
  - Slot clash detection against `ChannelRegistry` and `ModelProviderRegistry`
- `PluginUpdateService` — plugin update flow: unload old → delete JAR → stage new → load → register → promote to system
  - Config schema migration check: blocks update if new required fields are unfilled
- `PluginLoaderController` new endpoints:
  - `POST /api/plugins/{id}/resolve-deps` — resolve and return dependency chain
  - `POST /api/plugins/{id}/update` — update plugin to new JAR
  - `POST /api/plugins/check-slot-clash` — check manifest for slot conflicts
- `PluginLifecycleService` integration: dependency resolution runs during `install()`, stores dep list on plugin entity
- `PluginManifest` updated with `dependencies` and `softDependencies` fields
- `PluginDTO` and `DtoMapper` updated to expose `dependencies` list
- Database migration `V19__plugin_dependencies.sql` — `plugin_dependencies` table with resolution state tracking

---

## [v2.5.2-dev] - 2026-05-12

**Plugin Ecosystem — Plugin Loader & Storage**

### Added
- `PluginStorageService` — manages `system/` and `staging/` plugin directories under `$SYNAPSE_HOME/plugins/`
- `PluginLoaderService` — core loader creating isolated `URLClassLoader` + JPMS `ModuleLayer` per plugin
  - ServiceLoader-based plugin discovery from JAR manifest
  - `PluginContext` injection with scoped logger, config, event bus, bounded executor, auth mode
  - Lifecycle hook execution: `onLoad()` → active → `onUnload()`
  - In-memory tracking of loaded plugins with thread-safe registry
- `ChannelRegistry` — runtime registry for loaded Channel plugins with unique `channel_id` slot enforcement
- `ModelProviderRegistry` — runtime registry for loaded ModelProvider plugins with unique `provider_id` slot enforcement
- `PluginContextFactory` — creates injected PluginContext instances with:
  - `PluginLogger` implementation routing to SystemLogService
  - `PluginConfig` implementation backed by manifest `config_schema`
  - `PluginEventBus` implementation publishing to core EventPublisher
  - Bounded virtual thread `ExecutorService` (Official: 25, Community: 10 threads)
  - `AuthMode` detection from config keys
- `StartupPluginScanner` — startup scan of `system/` JARs with:
  - Crash recovery: detects orphaned `staging/` JARs and logs warnings
  - DB record matching, disabled plugin skipping
  - Automatic registry registration (Channel → onInstall, ModelProvider → configure)
- `PluginShutdownHook` — graceful shutdown: unload all plugins, promote `staging/` → `system/`
- `PluginLoaderController` — REST API for loader operations:
  - `GET /api/plugins/loader/status` — list loaded plugins with runtime info
  - `POST /api/plugins/{id}/load` — load a plugin from stored JAR
  - `POST /api/plugins/{id}/unload` — unload a plugin
  - `POST /api/plugins/{id}/reload` — reload a plugin
  - `GET /api/plugins/loader/orphans` — list orphaned staging JARs
  - `POST /api/plugins/loader/promote` — promote staging JARs to system
- Database migration `V18__plugin_loader_state.sql` — adds to `plugins` table:
  - `storage_tier` (SYSTEM / STAGING)
  - `loader_state` (UNLOADED / LOADING / LOADED / ERROR)
  - `error_message` — diagnostic on load failure
  - `loaded_at` — timestamp of last successful load
  - `api_version` — required synapse-plugin-api version
  - `trust_tier` (OFFICIAL / COMMUNITY)
- `Plugin` entity updated with new enums: `StorageTier`, `LoaderState`, `TrustTier`
- `PluginDTO` and `DtoMapper` updated to expose loader state fields
- `PluginLifecycleService` integrated with `PluginStorageService` for JAR deletion on uninstall
- OpenAPI spec `plugin-api.yaml` updated with Plugin Loader endpoints and loader state schemas

### Fixed
- **Compose Smoke Test compilation failure**: Added `synapse-plugin-api` dependency to `packages/core/pom.xml`
- **Docker build**: Updated `Dockerfile` to build `synapse-plugin-api` first, updated `docker-compose.yml` build context to repo root
- **CodeQL path-injection alerts** (2x): Added `isValidJarName()` validation in `PluginStorageService` to reject path traversal
- **Startup failure — `system_logs` table not ready**: Made `SystemLogService.write()` catch `DataAccessException` and fall back to stderr when table doesn't exist yet (pre-migration startup phase)
- **Plugin loader compilation errors**: Fixed `SystemLogService.log()` parameter order (UUID not Throwable), fixed `InboundMessage` accessor methods (`getChannelId()`, `getExternalUserId()`, `getText()`)

### Documentation
- Added `synapse-docs/docs/plugins/development/plugin-loader.mdx` — comprehensive loader architecture guide
- Updated `synapse-docs/sidebars.ts` to include plugin loader documentation
- Updated `synapse-docs/docs/plugins/plugin-api-reference.mdx` with v2.5.2-dev loader notice
- Restored missing `openapi/*.yaml` files in `synapse-docs/openapi/` (copied from `static/openapi/`)

---

## [v2.5.1-dev] - 2026-05-12

**Plugin Ecosystem — Plugin API Module**

### Added
- `synapse-plugin-api` Maven module (`dev.synapse:synapse-plugin-api:1.0.0`) published to GitHub Packages
- JPMS module descriptor (`module synapse.plugin.api`) — exports only `dev.synapse.plugin.api`, nothing else
- `SynapsePlugin` — base interface for all plugin types (`getId`, `getName`, `getVersion`, `onLoad`, `onUnload`)
- `Channel` — bidirectional messaging channel interface (`getChannelId`, `onMessage`, `sendMessage`, `onInstall`, `onUninstall`)
- `ModelProvider` — LLM backend interface (`getProviderId`, `complete`, `stream`, `getCapabilities`, `listModels`, `configure`)
- `PluginContext` — injected context providing scoped `logger`, `config`, `eventBus`, `executor`, `authMode`, `routeMessage`
- `PluginConfig` — typed config wrapper for manifest `config_schema` values with secret field support
- `PluginEventBus` — plugin-to-core event publishing and topic subscription
- `PluginLogger` — scoped logger tagged with plugin id, routed to system log
- `AuthMode` enum — `API_KEY`, `ACP`, `NONE` for credential routing in model providers
- Supporting value types: `InboundMessage`, `OutboundMessage`, `CompletionRequest`, `CompletionResponse`, `StreamHandler`, `ModelCapabilities`, `ModelInfo`, `PluginEvent`
- Updated `synapse-plugin-template`: now depends on `synapse-plugin-api:1.0.0`, drops Spring Boot, includes proper JPMS `module-info.java`, scaffold classes for Channel and ModelProvider, updated `plugin.yaml` manifest format for v2.6.0

---

## [v2.5.0] - 2026-05-12

**Milestone: Security Hardening**

### Added
- Comprehensive API security framework with JWT hardening, rate limiting, CORS enforcement, and brute-force protection
- Secrets management system with AES-256 encrypted vault, automated rotation, and secure distribution
- Audit logging infrastructure with tamper-resistant logs, compliance formats, and real-time monitoring
- Compliance framework supporting HIPAA, SOC2 Type II, and GDPR with data residency controls
- Security validation suite including penetration testing, vulnerability scanning, and security runbooks
- Security incident response runbooks with severity levels and recovery procedures
- Security deployment checklists for production hardening

### Security Fixes
- All security vulnerabilities identified during the security audit have been resolved
- Enhanced input validation across all API endpoints
- Improved authentication and authorization boundary checks
- Strengthened secrets handling and storage mechanisms

### Development Versions
- v2.4.2-dev: API Security
- v2.4.3-dev: Secrets Management
- v2.4.4-dev: Audit Logging
- v2.4.5-dev: Compliance Framework
- v2.4.6-dev: Security Validation & Runbooks

---

## [v2.4.6-dev] - 2026-05-11

**Security Hardening — Security Validation & Runbooks**

### Added
- SecurityValidationTest with integration tests for authentication, authorization, rate limiting, CORS, and security headers
- SECURITY_INCIDENT_RESPONSE.md runbook with severity levels, response procedures, recovery procedures, and communication templates
- SECURITY_DEPLOYMENT_CHECKLIST.md with pre-deployment, deployment, and post-deployment security checks

---

## [v2.4.5-dev] - 2026-05-11

**Security Hardening — GDPR Compliance**

### Added
- DataExportService for collecting all user data for GDPR export (profile, conversations, login history)
- DataDeletionService for GDPR right-to-deletion (anonymization and hard delete)
- DataExportController with `GET /api/compliance/export/{userId}`, `POST /api/compliance/anonymize/{userId}`, and `DELETE /api/compliance/delete/{userId}` — all secured by ADMIN role
- `anonymize(UUID id)` and `getUserDataExport(UUID id)` methods in UserService

---

## [v2.4.4-dev] - 2026-05-11

**Security Hardening — Audit Logging**

### Added
- SecurityAuditEvent JPA entity with `security_audit_events` database table
- SecurityAuditEventRepository for paginated queries by user, event type, and time range
- Flyway V17 migration creating the `security_audit_events` table with indexes
- SecurityAuditService for recording login attempts, authorization denials, and user actions
- AuditLogController with `GET /api/audit/events` secured by ADMIN role
- Audit events wired into AuthenticationService (login attempts) and JwtAuthenticationFilter (auth denials)

---

## [v2.4.3-dev] - 2026-05-11

**Security Hardening — Secrets Management**

### Added
- SecretValidator for startup detection of default secrets (warns in dev, fails in production)
- TokenBlacklistService for Redis-backed token revocation with TTL-based auto-cleanup
- JWT ID (JTI) claim added to all tokens for revocation support
- Token blacklist check integrated into JwtService.isTokenValid()
- `revokeToken()` method in JwtService for token revocation
- `logout()` method in AuthenticationService that revokes the current token
- Old refresh token revocation on token refresh
- `POST /api/auth/logout` endpoint returning 204 No Content
- `synapse.security.require-secrets-override` configuration property

---

## [v2.4.2-hotfix] - 2026-05-11

### Fixed
- Fixed RateLimitingFilter compilation error: replaced `HttpServletResponse.SC_TOO_MANY_REQUESTS` (not available in Jakarta Servlet API version used) with numeric 429 constant.

---

## [v2.4.2-dev] - 2026-05-11

**Security Hardening — API Security**

### Added
- CORS configuration via WebConfig (configurable allowed origins, credentials, max age)
- Security headers: X-Content-Type-Options, HSTS, X-Frame-Options, Referrer-Policy, Permissions-Policy
- RateLimitingFilter with configurable requests-per-minute (default 60) and login-specific limit (default 10)
- Brute-force protection: account lockout after 5 failed attempts, 15-minute lockout duration, configurable
- `rate-limiting.*` and `cors.*` configuration properties with environment variable overrides

---

## [v2.4.1-dev] - 2026-05-11

**CLI Component Framework & Interactive Installer**

### Added
- Reusable TUI component framework: BaseComponent, TextInput, SingleSelect, MultiSelect, SearchList, Toggle, Confirm, Progress, Summary, Welcome, Requirements
- Raw terminal input handler with cross-platform arrow-key, Enter, and Space support
- SYNAPSE brand theme package with ANSI color codes
- `synapse install` command with full interactive installation wizard
- OS detection (Windows/Linux/macOS) with package manager identification
- Prerequisites check with version-aware auto-install (Docker, Git, Go)
- Model provider search with API Key and Subscription auth mode support
- Channel plugin multi-select (Telegram, Discord, WhatsApp, Slack, Matrix)
- Skill plugin multi-select (web-search, code-execution, image-generation, etc.)
- Config persistence to `~/.synapse/install.yaml` with pre-filled defaults on re-run
- Bootstrap scripts (`install.sh`, `install.ps1`) that install Go, build CLI, and add `synapse` to PATH
- Spec document: `docs/superpowers/CLI_COMPONENT_FRAMEWORK.md`

### Changed
- `install.sh` now detects OS, installs Go, builds CLI to `~/.local/bin`, and adds to PATH
- `install.ps1` moved to root, installs Go, builds CLI to `%LOCALAPPDATA%\synapse\bin`, and adds to PATH

### Removed
- `packages/cli/Dockerfile` — no longer needed (Go installed by bootstrap scripts)
- `installer/install.ps1` — replaced by root-level `install.ps1`
- `installer/install.tui.ps1` — superseded by Go TUI installer

---

## [v2.4.0] - 2026-05-11

**Milestone:** Advanced Agent Capabilities

### Added
- Team-scoped collaboration sessions with inter-agent messaging, task delegation records, and shared context state.
- Goal-based planning system with versioned planning artifacts, reasoning-chain snapshots, and next-step retrieval.
- Native Java tools framework with runtime tool discovery, execution, timeout guardrails, and deterministic caching.
- Initial built-in native tools:
  - `tool_registry_inspect`
  - `plugin_contract_validate`
- Central hardening policy engine with unified `ALLOW/WARN/BLOCK` decisions across delegation, planning, and tooling.

### Changed
- Advanced agent workflows now enforce centralized guardrails for delegation loops, planning runaway protection, and token-budget pressure handling.
- Runtime and deployment configuration extended with `synapse.tools.*` and `synapse.hardening.*` controls.

### Fixed
- Runtime version metadata alignment via `v2.3.8-hotfix` (health/version reporting now follows current build metadata by default).

### Development Versions
- v2.3.1-dev: roadmap/docs workflow alignment
- v2.3.2-dev: runtime stability hotfix
- v2.3.3-dev: agent memory foundation
- v2.3.4-dev: collaboration framework
- v2.3.5-dev: reasoning & planning
- v2.3.6-dev: native Java tools integration
- v2.3.7-dev: capability hardening
- v2.3.8-hotfix: release/version metadata correction

---

## [v2.3.8-hotfix] - 2026-05-11

### Fixed
- Corrected runtime version metadata defaults so startup/health no longer report legacy `v1.x` values.
- Updated backend artifact version to the current `v2.3.x-hotfix` line for release-consistent runtime reporting.
- Removed hardcoded `SYNAPSE_VERSION` defaults from Compose manifests so runtime version follows application build metadata by default.

---

## [v2.3.7-dev] - 2026-05-11

### Added
- Central hardening policy engine (`AgentHardeningPolicyService`) with unified decision model (`ALLOW`, `WARN`, `BLOCK`) and reason codes
- Delegation guardrails for self-delegation prevention, delegation-loop detection, and max-hop enforcement
- Planning guardrails for max-step and max-refinement enforcement
- Token-budget guardrails with per-phase budgets and automatic concise-mode enforcement near budget limits
- Hardening decision metadata in native tool execution responses (`hardeningReasonCode`, `enforcedMode`)
- New ideas docs:
  - `ideas/RULES_SYSTEM.md`
  - `ideas/ADMIN_REQUEST_NOTIFICATION_CHANNEL.md`

### Changed
- Collaboration, planning, and native tool execution now route policy decisions through the same central hardening engine
- Runtime configuration extended with `synapse.hardening.*` keys for delegation, planning, and token policy thresholds

### Validation
- Added policy-focused test coverage for delegation loop blocking, planning caps, and token warning/block behavior

---

## [v2.3.6-dev] - 2026-05-11

### Added
- Native Java tool framework with extensible tool contract (`NativeJavaTool`)
- Tool registry/discovery service with duplicate-id protection and metadata endpoints
- Unified JVM tool execution service with timeout guardrails and structured execution logging
- Deterministic tool-result caching with normalized cache keys and TTL controls
- Built-in tool: `tool_registry_inspect` for runtime tool discovery and metadata inspection
- Built-in tool: `plugin_contract_validate` for plugin manifest contract + safety policy validation
- New API surface under `/api/tools` for tool listing, metadata, and execution

### Changed
- Extended runtime configuration with `synapse.tools.*` settings for enablement, timeout, and cache defaults

---

## [v2.3.5-dev] - 2026-05-11

### Added
- Goal-based planning persistence with team-scoped planning goals
- Versioned planning artifacts that store compact plan summaries, step lists, and provider-generated reasoning-chain snapshots
- Plan refinement flow that supersedes old plan versions and keeps an auditable planning history
- Goal next-step endpoint for low-token execution (reuse stored plans instead of regenerating full plans each turn)
- New migration: `V15__agent_reasoning_planning.sql`
- New REST API under `/api/teams/{teamId}/planning` for goals, plans, refinement, and next-step retrieval

### Changed
- Planning data model optimized for token savings by persisting compact summaries and structured plan/reasoning artifacts for reuse

---

## [v2.3.4-dev] - 2026-05-10

### Added
- Agent collaboration framework with dedicated collaboration sessions scoped to teams
- Inter-agent messaging protocol with typed message events (`DIRECTIVE`, `CONTEXT`, `STATUS`, `RESULT`)
- Task delegation records between agents inside a collaboration session
- Shared collaboration context store with versioned context keys for team-wide state
- New migration: `V14__agent_collaboration_framework.sql`
- New REST API under `/api/teams/{teamId}/collaboration` for sessions, messages, delegations, and shared context

### Validation
- Team/session ownership checks and active-session enforcement on all collaboration operations
- Membership validation for message senders/recipients and delegation/shared-context updates

---

## [v2.3.3-dev] - 2026-05-10

### Added
- Three-tier agent memory model: `SHORT_TERM`, `KNOWLEDGE`, `ARCHIVE`
- Memory lifecycle metadata fields (promotion/access/retention/source linkage)
- Memory promotion endpoint and tier-filtered memory retrieval API support
- Scheduled memory lifecycle jobs:
  - monthly knowledge compaction
  - bi-monthly archive cleanup scaffold
- New migration: `V13__agent_memory_tiers.sql`

### Changed
- Agent memory reads now track access counts and auto-promote frequently reused short-term entries to knowledge
- Memory list endpoint now supports optional `tier` filtering

### Fixed
- Enforced valid tier transition rules to prevent invalid archive writes and regressions

---

## [v2.3.2-dev] - 2026-05-10

### Fixed
- Added missing `commons-pool2` dependency required by Redis Lettuce pooling
- Resolved backend startup failure in Compose smoke test after enabling Redis connection pooling

---

## [v2.3.1-dev] - 2026-05-10

### Documentation
- Expanded roadmap milestone granularity with added hardening/validation steps across `v2.x.0` tracks
- Updated AGENT workflow rules for docs-coupled dev cycles
- Added `synapse-docs` version mapping and pre-release requirements for docs updates

---

## [v2.3.0] - 2026-05-10

**Milestone:** Performance & Caching

### Added
- Query optimization and pagination for high-volume API surfaces
- Redis-backed cache layer for conversation, provider, user, and plugin/store metadata
- HTTP response compression and JSON serialization optimizations
- Connection pooling for PostgreSQL, Redis, and outbound provider HTTP calls

### Development Versions
- v2.2.1-dev: Query Optimization
- v2.2.2-dev: Redis Caching Layer
- v2.2.3-dev: Response Compression
- v2.2.4-dev: Connection Pooling

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
- Implementation roadmap (docs/roadmaps/SYNAPSE_V1_IMPLEMENTATION_ROADMAP.md)
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
