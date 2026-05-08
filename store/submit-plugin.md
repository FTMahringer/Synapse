# Submit a Plugin or Bundle

This guide explains how plugins, skills, MCP servers, themes, and bundles enter the {SYSTEM_NAME} store ecosystem.

## Store Paths

There are two primary submission paths:

- Official Store: curated by the core maintainers, invite-only, reviewed for security and long-term support.
- Community Store: open pull-request workflow, clearly labeled as community content in the UI and CLI.

Direct URL installs are not submissions. They are local operator choices and remain marked as unverified.

## Official Store Submission

Official Store content is accepted by maintainer invitation.

Requirements:

- Complete `manifest.yml` with valid schema fields
- Permissive license such as MIT, Apache-2.0, or BSD-3-Clause
- Clear README with setup, credentials, permissions, and limitations
- No hidden telemetry
- No undeclared network calls
- No plaintext secret storage
- Java 25-compatible implementation for JVM plugins
- Logging events documented for install, uninstall, invoke, failure, and update paths

Review flow:

1. Maintainer requests or approves an official submission.
2. Contributor opens a pull request against the main repository.
3. Maintainers review manifest, code, permissions, and docs.
4. Security-sensitive behavior is tested manually.
5. After merge, the plugin appears in the Official Store on the next release.

## Community Bundle Submission

Community bundles are submitted to `synapse-community/bundles`.

Requirements:

- One YAML file per bundle
- Filename matches the bundle `id`
- At least one plugin reference
- Valid semver range for every plugin
- SPDX license
- Clear description and post-install configuration notes
- No bundled executable code

Review flow:

1. Fork `https://github.com/synapse-community/bundles`.
2. Add or update `bundles/<bundle-id>.yml`.
3. Run the bundle validation workflow.
4. Open a pull request against `main`.
5. Community maintainers review schema, referenced plugin IDs, and clarity.
6. After merge, the bundle appears after the next community store cache refresh.

## Skill Publishing

Skills can be published through the `/skills publish` command.

Rules:

- Publishing is always manual and user initiated.
- The user must confirm the registry and scope before upload.
- Agents may propose a skill package, but they cannot publish it automatically.
- The skill must follow the Claude Code Skills format.
- Required permissions must be declared in the skill manifest.

## Manual Plugin Path

Operators can install a plugin from a local path or direct Git URL.

The manual path still runs validation:

- Manifest schema check
- Type check: channel, model, skill, mcp, or theme
- Version compatibility check
- Required config check
- Permission summary before activation

Manual installs are marked with source `direct_url` or `local` and are not treated as Official Store content.

## Logging

Store submission and install flows log these events:

- `store.plugin.submitted`
- `store.bundle.submitted`
- `store.validation.started`
- `store.validation.failed`
- `store.validation.passed`
- `store.install.requested`
- `store.install.confirmed`
- `store.install.completed`
- `store.install.failed`

Each log payload includes source, plugin or bundle ID, version, user ID, validation result, and correlation ID.
