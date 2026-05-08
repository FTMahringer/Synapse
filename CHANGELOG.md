# Changelog

All notable project changes are tracked here once they become part of a roadmap milestone.

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
