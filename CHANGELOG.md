# Changelog

All notable project changes are tracked here once they become part of a roadmap milestone.

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
