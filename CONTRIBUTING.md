# Contributing to {SYSTEM_NAME}

Thank you for your interest in contributing to {SYSTEM_NAME}. This guide covers everything you need to get started — from submitting a bug fix to publishing a plugin or skill to the community store.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How to Contribute (Fork, Branch, PR)](#how-to-contribute)
3. [Plugin Submission Process](#plugin-submission-process)
4. [Bundle Submission](#bundle-submission)
5. [Skill Publishing](#skill-publishing)
6. [Code Style](#code-style)
7. [Issue Reporting](#issue-reporting)
8. [Community Store vs Official Store](#community-store-vs-official-store)

---

## Code of Conduct

This project follows a standard contributor code of conduct. Be respectful, constructive, and inclusive. Harassment, discrimination, or bad-faith contributions will not be tolerated.

---

## How to Contribute

### 1. Fork the Repository

Click **Fork** on GitHub to create your own copy of the repository under your account.

### 2. Clone Your Fork

```bash
git clone https://github.com/YOUR_USERNAME/synapse.git
cd synapse
```

### 3. Create a Feature Branch

Always create a branch from `main`. Use a descriptive name that reflects your change:

```bash
# For new features
git checkout -b feature/add-discord-channel-plugin

# For bug fixes
git checkout -b fix/heartbeat-reconnect-loop

# For documentation changes
git checkout -b docs/update-plugin-guide

# For refactoring
git checkout -b refactor/agent-team-scheduler
```

Branch names must be lowercase and use hyphens, not underscores or spaces.

### 4. Make Your Changes

- Follow the [Code Style](#code-style) guidelines for your language.
- Keep commits focused. One logical change per commit.
- Write clear commit messages in the imperative mood: `Add Discord channel plugin`, not `Added Discord channel plugin` or `Adding Discord channel plugin`.
- If you are fixing an issue, reference it in the commit message: `Fix heartbeat reconnect loop (#142)`.

### 5. Run Tests Before Pushing

```bash
# Backend (Java)
cd backend && ./mvnw verify

# Frontend (Vue)
cd frontend && npm run test

# CLI (Go)
cd cli && go test ./...
```

Do not submit a PR with failing tests. If your change requires new tests, write them.

### 6. Open a Pull Request

Push your branch to your fork and open a pull request against the `main` branch of the upstream repository.

**PR requirements:**

- Fill out the PR template completely. Do not delete sections.
- Link to any related issues (`Closes #123` or `Refs #456`).
- Provide a clear description of what the change does and why.
- If the change affects the UI, include before/after screenshots.
- If the change affects plugin APIs or agent behavior, update the relevant docs in `docs/`.
- At least one maintainer must approve before merging.
- All CI checks must pass (lint, tests, build).

**PR size:** Keep PRs focused and reasonably sized. A PR that touches 50 files across unrelated areas will be asked to be split up.

---

## Plugin Submission Process

Plugins extend {SYSTEM_NAME} in four categories: channels, model providers, skills, and MCP servers. Each category has specific requirements.

### General Plugin Requirements

All plugins must:

- Include a `plugin.json` manifest at the root of the plugin directory (see `docs/plugins.md` for the schema).
- Be licensed under a permissive open-source license (MIT, Apache 2.0, or BSD).
- Not phone home or exfiltrate user data without explicit, documented user consent.
- Not modify core platform internals — only use the official plugin API.
- Include a README that documents configuration options, required credentials, and usage.
- Pass the automated plugin validation CI job.

### Channel Plugins

Channel plugins route messages between {SYSTEM_NAME} agents and external platforms (e.g., Telegram, Discord, WhatsApp).

Requirements:

- Implement the `ChannelPlugin` interface from `backend/plugin-api/src/main/java/synapse/plugin/channel/ChannelPlugin.java`.
- Handle message normalization — incoming messages must be converted to the internal `AgentMessage` format.
- Support webhook and polling modes where the platform allows both.
- Never store message content beyond what is needed for active session context.
- Document all required API keys and OAuth scopes in the plugin README.

### Model Provider Plugins

Model provider plugins connect {SYSTEM_NAME} to LLM backends (e.g., OpenAI, Anthropic, DeepSeek, Ollama).

Requirements:

- Implement the `ModelProviderPlugin` interface from `backend/plugin-api/src/main/java/synapse/plugin/model/ModelProviderPlugin.java`.
- Support streaming responses via the standard `ModelStream` abstraction.
- Expose a capability map so the platform knows which features the provider supports (e.g., function calling, vision, embeddings).
- Handle provider-specific rate limiting and retry logic internally — do not let exceptions bubble up to the agent layer.
- Document pricing and any data retention policies of the upstream provider in the plugin README.

### Skill Plugins

Skill plugins add reusable callable capabilities to agents. {SYSTEM_NAME} uses the Claude Code Skills format.

Requirements:

- Follow the Claude Code Skills format exactly. The manifest, entry point, and tool schema must conform to the specification in `docs/plugins.md#skills`.
- Skills must be stateless where possible. If a skill requires persistent state, use the platform's key-value store API — never write to the filesystem directly.
- Skills must declare all permissions they require in the manifest. The platform will prompt the user to grant permissions at install time.
- Include example invocations in the README.

### MCP Server Plugins

MCP server plugins integrate any MCP-compatible external tool server with {SYSTEM_NAME} agents.

Requirements:

- Provide a valid MCP server manifest (`mcp.json`) that the platform can use to auto-discover tools.
- If the MCP server requires a separate process, provide a launcher script and document system requirements.
- All tool calls through the MCP server must be logged to the platform's audit log.

### Submitting a Plugin

1. Place your plugin in the `plugins/` directory of a fork.
2. Open a PR to the main repository with the prefix `[plugin]` in the title: `[plugin] Add Telegram channel`.
3. A maintainer from the plugins team will review the manifest, code, and README.
4. Once approved, the plugin is merged and becomes available to install from the official store after the next release.

---

## Bundle Submission

A bundle is a curated collection of plugins, skills, and configuration presets packaged together for a specific use case (e.g., "Developer Workspace Bundle", "Customer Support Bundle").

Bundles are not submitted to the main repository. They are submitted to the community repository:

**`synapse-community/bundles`** — `https://github.com/synapse-community/bundles`

### Bundle Submission Process

1. Fork `synapse-community/bundles`.
2. Create a directory under `bundles/YOUR_BUNDLE_NAME/`.
3. Add the following files:
   - `bundle.json` — Bundle manifest (name, version, description, author, license, list of included plugins and skills with versions).
   - `README.md` — What the bundle does, who it is for, and how to configure it.
   - `preview.png` — A screenshot or diagram of the bundle in action (optional but strongly encouraged).
4. Open a PR to `synapse-community/bundles`. The community reviews and votes; maintainers of that repository make the final call on merging.

Once merged, the bundle appears in the {SYSTEM_NAME} community store immediately (no platform release cycle required).

**Versioning:** Bundles must use semantic versioning (`MAJOR.MINOR.PATCH`). When you update a bundle, increment the version in `bundle.json` and open a new PR.

---

## Skill Publishing

Skills can be published directly from within {SYSTEM_NAME} using the built-in publish command. This is the recommended flow for individual skills that are not part of a larger plugin.

### Publishing a Skill via the Platform

From the CLI or dashboard, run:

```
/skills publish <skill-name>
```

The platform will:

1. Validate the skill manifest and entry point.
2. Show you a full preview of what will be published (manifest, tool schema, README).
3. **Explicitly ask for your consent** — you must confirm before anything is uploaded.
4. Package and upload the skill to the community store under your account.

**Consent is mandatory.** The platform will never publish a skill without your explicit confirmation. If you cancel the prompt, nothing is uploaded.

### Skill Versioning

When you publish an update to an existing skill, bump the `version` field in the skill manifest before running `/skills publish`. The store keeps all published versions; users can pin to a specific version.

### Skill Ownership and Takedowns

You retain ownership of skills you publish. You can unpublish a skill at any time:

```
/skills unpublish <skill-name>
```

Unpublishing removes the skill from the store listing. Users who have already installed the skill are not affected, but they will no longer receive updates.

---

## Code Style

### Java 25+ (Backend)

- Use Java 25+ language features where they improve clarity: records, sealed classes, pattern matching, virtual threads.
- Follow Google Java Style Guide with the following overrides:
  - Indent with 4 spaces (not 2).
  - Maximum line length: 120 characters.
- All public APIs must have Javadoc. Internal classes should have comments for non-obvious logic.
- Use `var` for local variables where the type is obvious from the right-hand side. Do not use `var` for method parameters or fields.
- Prefer immutable data: use records and `List.of()` / `Map.of()` where applicable.
- Async code must use Spring's reactive stack or virtual threads consistently — do not mix blocking and non-blocking styles in the same service.
- Spring Boot configuration classes must use `@ConfigurationProperties` with typed records. Do not use `@Value` for anything other than simple string overrides.

Run the formatter before committing:

```bash
cd backend && ./mvnw spotless:apply
```

### Go (CLI)

- Follow the official Go formatting standard. Run `gofmt -w .` before committing.
- Follow `go vet` and `staticcheck` with zero warnings.
- Error handling: always handle errors explicitly. Do not use `_` to discard errors unless there is a documented reason in a comment on the same line.
- Use the `errors` and `fmt.Errorf` packages for error wrapping. Prefer `%w` over `%v` for errors that callers may need to inspect.
- Bubble Tea components must be kept small and focused. Complex state logic belongs in a separate model file, not inline in the `Update` function.
- Package names must be short, lowercase, and singular.
- All exported symbols must have Go doc comments.

Run linting before committing:

```bash
cd cli && golangci-lint run ./...
```

### Vue 3 (Frontend)

- Use the Composition API with `<script setup>` syntax exclusively. Do not use the Options API.
- TypeScript is required for all `.vue` files and `.ts` modules. Do not use `any` — if a type is unknown, use `unknown` and narrow it properly.
- Follow the Vue 3 Style Guide (Priority A and B rules are mandatory).
- Component names must be PascalCase in files and in templates.
- Props must be typed with `defineProps<{...}>()`. Do not use the object-syntax props declaration.
- Use Pinia for all shared state. Do not use `provide`/`inject` as a replacement for a proper store.
- CSS: use scoped styles in components. Global styles go in `src/assets/styles/`. Use CSS custom properties for all color values — reference the platform color system (see `src/assets/styles/colors.css`).
- All user-facing strings must go through the i18n system (`vue-i18n`). Do not hardcode display text.

Run linting and type-checking before committing:

```bash
cd frontend && npm run lint && npm run type-check
```

---

## Issue Reporting

### Before Opening an Issue

- Search existing issues (open and closed) to check if the problem is already known.
- Check the `docs/` folder — your question may already be answered.
- If you are not sure whether something is a bug or a usage question, start a Discussion instead of opening an Issue.

### Bug Reports

Use the **Bug Report** issue template. Provide:

- A clear, specific title.
- Steps to reproduce the problem (numbered list, minimal — the fewer steps the better).
- What you expected to happen.
- What actually happened (include logs, error messages, and stack traces if available).
- Your environment: OS, Java version, Go version, Node version, Docker version, and the {SYSTEM_NAME} version or commit hash.
- Whether the bug is reproducible consistently or intermittent.

Attach logs from `logs/synapse.log` or the relevant Docker service (`docker compose logs backend`). Redact any credentials or personal data before posting.

### Feature Requests

Use the **Feature Request** issue template. Describe:

- The problem you are trying to solve (not just the solution you want).
- How you currently work around the problem (if at all).
- Why this would benefit other users of the platform.

Feature requests are not guaranteed to be implemented. They go into a triage queue and are prioritized by maintainers based on alignment with the project roadmap.

### Security Vulnerabilities

Do not open a public GitHub issue for security vulnerabilities. Report them privately by emailing the security contact listed in `SECURITY.md`. You will receive an acknowledgment within 48 hours.

---

## Community Store vs Official Store

{SYSTEM_NAME} has two distinct stores. Understanding the difference matters for contributors and users alike.

### Official Store

- Curated by the {SYSTEM_NAME} maintainers team.
- All plugins, skills, and bundles are reviewed for security, code quality, and adherence to platform APIs before listing.
- Items in the official store are updated in sync with platform releases.
- To submit to the official store, open a PR to the main repository (see [Plugin Submission Process](#plugin-submission-process)).
- Listings carry a "Verified" badge in the dashboard.

### Community Store

- Powered by `synapse-community/bundles` and community-maintained plugin forks.
- Contributions are reviewed by community maintainers, not the core team.
- No formal security audit is performed beyond automated scanning.
- Items may be updated independently of platform releases.
- To submit to the community store, open a PR to `synapse-community/bundles` (for bundles) or use `/skills publish` (for individual skills).
- Listings carry a "Community" badge in the dashboard.

**Recommendation for users:** Prefer official store items for production deployments. Community store items are excellent for experimentation and specialized use cases, but review the source before installing anything that handles sensitive data.

**Recommendation for contributors:** If you are building something experimental or niche, start in the community store. Once it is stable and has user adoption, you can propose it for promotion to the official store by opening an issue tagged `store-promotion-request`.
