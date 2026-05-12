# synapse-plugin-template & synapse-plugin-examples — Design Spec

**Date:** 2026-05-12
**Repos:** `synapse-plugin-template`, `synapse-plugin-examples`
**Status:** Approved
**Depends on:** v2.6.0 (`synapse-plugin-api` published to GitHub Packages)

---

## 1. Goal

Give plugin developers a zero-friction starting point. `synapse plugin scaffold` creates a
ready-to-build, ready-to-test, CI/CD-wired GitHub repo in seconds. `synapse-plugin-examples`
provides real, working reference implementations for every plugin type.

---

## 2. Repository Overview

Two separate repos, one mounted as a git submodule of the other:

```
synapse-plugin-template/          ← GitHub template repo (scaffold source)
├── channel/                      ← Channel plugin scaffold
├── model-provider/               ← ModelProvider plugin scaffold
├── mcp/                          ← MCP Server scaffold (v2.7.0)
├── skills/                       ← Skills Bundle scaffold (v2.7.0)
├── examples/                     ← git submodule → synapse-plugin-examples
└── README.md                     ← top-level: "pick a type, run scaffold"

synapse-plugin-examples/          ← separate repo
├── discord-channel/              ← real Discord bot (Channel)
├── openai-provider/              ← OpenAI ModelProvider (API key + ACP)
├── ollama-provider/              ← Ollama ModelProvider (local, no key)
├── filesystem-mcp/               ← MCP example (v2.7.0)
└── research-skills-bundle/       ← Skills example (v2.7.0)
```

Versioned independently of Synapse core. Versioning: `v0.x.0` (template + examples follow
`synapse-plugin-api` minor versions).

---

## 3. Plugin ID Convention

Plugin id = `{author}/{plugin-name}` — globally unique, same pattern as GitHub repos.

```yaml
id: ftmahringer/discord-channel    # unique — author + name combination
name: Discord Channel              # display name — not required to be unique
author: ftmahringer
```

Scaffold pre-fills `id` automatically from the author and plugin name fields. No manual id entry.
Registry enforces uniqueness on the `id` field.

---

## 4. Scaffold Contents

### 4.1 Java Types (Channel + ModelProvider)

Each subdirectory contains:

```
manifest.yml
  — pre-filled with entered values (id, name, author, version, license)
  — correct config_schema for the type (see section 5 for auth modes)
  — correct hooks declared
  — store metadata with placeholder tags + icon

ExamplePlugin.java
  — implements the correct interface (Channel or ModelProvider)
  — all methods present with TODO comments explaining what goes where
  — compiles clean against synapse-plugin-api

package-info.java
  — JPMS module declaration: requires synapse.plugin.api only

build.gradle.kts
  — declares synapse-plugin-api from GitHub Packages
  — JUnit 5 + Mockito test dependencies
  — shadowJar / fatJar packaging task
  — deleted by scaffold if Maven chosen

pom.xml
  — same as above in Maven format
  — deleted by scaffold if Gradle chosen

src/test/java/com/example/
  ExamplePluginTest.java
    — JUnit 5 unit tests
    — PluginContext mocked via Mockito
    — tests each lifecycle method
  ExamplePluginIntegrationTest.java
    — spins up Docker test container (plugin-test-env image)
    — installs plugin via plugin loader
    — verifies onLoad() called, plugin appears in registry
    — verifies config injection works

test.sh
  — runs unit + integration tests inside Docker
  — exit 0 = all pass; exit 1 = fail with clear output

.github/workflows/
  test.yml
    — trigger: push to any branch + PR
    — steps: build → unit tests → integration tests via Docker
    — builds synapse/plugin-test-env:<type> image
    — pushes to Docker Hub on main branch push

  validate.yml
    — trigger: PR opened / updated
    — runs `synapse plugin validate` (manifest check + bytecode scan)
    — blocks PR merge on any violation
    — posts violation report as PR comment

  release.yml
    — trigger: tag push matching v*.*.*
    — steps: validate → test → `synapse plugin package` → sign JAR
    — creates GitHub release with signed JAR attached
    — generates release notes from commits since last tag

  publish.yml
    — trigger: manual workflow dispatch
    — steps: package → sign → prepare PR against /synapse-plugins-community
    — STUB in v2.6.0: prints submission instructions until publish command is implemented

.gitignore
  — Java / Gradle / Maven standard ignores
  — .env, *.secret

README.md
  — Prerequisites (Java 25+, Docker, synapse CLI)
  — Quick start: fill manifest → implement → ./test.sh → push tag
  — Link to synapse-plugin-examples for reference
```

### 4.2 Declarative Types (MCP + Skills — v2.7.0)

Same structure but no Java source. Contains:
```
manifest.yml              — fully commented template for the type
.github/workflows/
  validate.yml            — validates manifest YAML schema on PR
  release.yml             — tags release, attaches manifest as release asset
README.md                 — Quick start for declarative types
```

---

## 5. Auth Modes for Model Providers

Model providers declare which authentication modes they support. Both API key and ACP
subscription login are first-class options — not stubs.

```yaml
auth_modes:
  - api_key       # standard key-based auth
  - acp           # Anthropic Claude Platform subscription login

config_schema:
  api_key:
    type: string
    required: false
    secret: true
    description: "API key. Required if not using ACP subscription login."

  acp_subscription_id:
    type: string
    required: false
    secret: true
    description: "ACP subscription ID. Use instead of api_key for ACP-registered providers."
```

`PluginContext` exposes `authMode()` so implementations check which auth was supplied without
branching on raw config keys. At least one of `api_key` or `acp_subscription_id` must be set —
validated by the loader before `configure()` is called.

---

## 6. `synapse plugin scaffold` TUI Flow

```
? Plugin type:       [Channel] / [Model Provider] / [MCP Server] / [Skills Bundle]
? Plugin name:       ________________________
? Author:            ________________________   (becomes the id namespace)
? License:           [MIT] / Apache-2.0 / other
? Build system:      [Gradle] / [Maven]         (Java types only)
? GitHub org/user:   ________________________

→ Creates GitHub repo from synapse-plugin-template/<type> subdirectory
→ Renames skeleton class to match plugin name (PascalCase)
→ Pre-fills manifest.yml: id = {author}/{plugin-name}, all entered fields
→ Deletes unused build file (Gradle or Maven)
→ Commits initial scaffold with message: "chore: scaffold {author}/{plugin-name}"
→ Prints:
    Repo created: https://github.com/{org}/{plugin-name}
    Next steps:
      1. Implement src/main/java/.../{PluginName}.java
      2. Fill in manifest.yml config_schema
      3. Run: ./test.sh
      4. Push a version tag: git tag v1.0.0 && git push --tags
```

---

## 7. Example Plugins

All examples in `synapse-plugin-examples/` are real, working plugins — not toys. Each serves as
both a community reference and a proof-of-system for its plugin type.

### 7.1 discord-channel

| Field | Detail |
|---|---|
| id | `synapse-team/discord-channel` |
| Type | Channel |
| Transport | Webhook (primary), long-polling fallback |
| Config | `bot_token` (secret), `webhook_secret` (optional) |
| Auth | API key only |
| Demonstrates | `onInstall` webhook registration, `onMessage` routing, `sendMessage` with Markdown, `onUninstall` webhook cleanup, threading model with virtual threads |

Works with no code changes — add `bot_token`, install, done. Includes `DEVELOPMENT.md` with
step-by-step Discord bot setup (create app, enable intents, copy token).

### 7.2 openai-provider

| Field | Detail |
|---|---|
| id | `synapse-team/openai-provider` |
| Type | Model Provider |
| Config | `api_key` (secret), `acp_subscription_id` (secret, optional), `base_url` (optional — Azure/proxy support) |
| Auth | API key + ACP subscription login |
| Demonstrates | `configure`, `complete`, `stream` via SSE, `listModels` (dynamic from API), `getCapabilities`, tool calling, error handling + retry with exponential backoff |

Full `ModelProvider` interface coverage including streaming and function calling. ACP auth
path demonstrates the `authMode()` pattern from `PluginContext`.

### 7.3 ollama-provider

| Field | Detail |
|---|---|
| id | `synapse-team/ollama-provider` |
| Type | Model Provider |
| Config | `base_url` (default: `http://localhost:11434`) — no API key required |
| Auth | None |
| Demonstrates | Same interface as openai-provider but adapts a plain REST API without an official SDK; local model listing; streaming via chunked HTTP |

Zero-cost testing path. Includes `DEVELOPMENT.md`: install Ollama, `ollama pull llama3`, done.
Shows how to write a provider without a vendor SDK.

### 7.4 Shared Example Conventions

- Every example has `DEVELOPMENT.md` — setup steps specific to that plugin
- Every example passes the full scaffold test suite
- Every example is signed and published to `/synapse-plugins`
- Examples target the same `synapse-plugin-api` version as the current milestone
- Examples submodule pinned to a tag in `synapse-plugin-template`; update manually when new
  examples added

---

## 8. Release Timeline

| Milestone | Template version | Examples version | Content |
|---|---|---|---|
| `synapse-plugin-api` published (v2.5.1-dev) | — | — | API JAR available on GitHub Packages |
| v2.6.0 | **v0.1.0** | **v0.1.0** | `channel/` + `model-provider/` scaffolds; `discord-channel`, `openai-provider`, `ollama-provider` |
| v2.7.0 | **v0.2.0** | **v0.2.0** | `mcp/` + `skills/` scaffolds; `filesystem-mcp`, `research-skills-bundle` |

---

## 9. Future / Deferred

| Item | Notes |
|---|---|
| Python scaffold (`python/` subdirectory) | After v2.9.0 Python runtime ships |
| Node.js scaffold (`nodejs/` subdirectory) | After v2.9.0 Node.js runtime ships |
| `synapse plugin publish` full implementation | Deferred — needs deep design |
| More example plugins | Community contributions after v2.6.0 ships |
