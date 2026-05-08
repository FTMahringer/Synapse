# SYNAPSE Bundle System — Complete Specification

> A bundle is an installable package that groups multiple plugins with shared configuration into a single deployable unit. Bundles are community-contributed and managed via a public GitHub repository using a pull-request workflow.

---

## 1. What Is a Bundle?

A bundle is a curated collection of SYNAPSE plugins packaged together with configuration notes and metadata. Installing a bundle installs all contained plugins in sequence, allowing users to set up a complete, pre-configured workflow in one step.

**Example use cases:**
- A "Developer Setup" bundle: Telegram channel + Anthropic model + a code-review skill.
- A "Home Automation" bundle: Discord channel + Ollama local model + a home-assistant MCP server.
- A "Research Assistant" bundle: OpenAI model + a web-search skill + a knowledge-vault MCP.

Bundles do not ship code — they reference existing plugin IDs from the known stores. They describe which plugins to install and in what configuration context. All actual plugin code is fetched from the plugin's own source (Official Store, skills.sh, etc.) at install time.

---

## 2. Bundle Format

Each bundle is defined as a YAML file. The filename must match the bundle `id`: `[bundle-id].yml`.

### 2.1 Full Bundle Schema

```yaml
# Required fields
id: developer-setup                    # Unique identifier, kebab-case, no spaces
name: "Developer Setup Bundle"         # Human-readable display name
version: 1.0.0                         # Semver (MAJOR.MINOR.PATCH)
author: username                       # GitHub username of bundle author
description: |
  Pre-configured developer environment with Telegram notifications,
  Claude as the model provider, and a GitHub MCP integration.

# Required: at least 1 plugin
plugins:
  - id: telegram-channel               # Must match a known plugin ID in a store source
    version: ">=1.0.0"                 # Semver range (required, must be valid semver range)
  - id: anthropic
    version: ">=1.0.0"
  - id: github-mcp
    version: ">=0.2.0"

# Recommended fields
tags:                                  # Free-form tags for filtering (lowercase, no spaces)
  - development
  - coding
  - github

license: MIT                           # SPDX identifier — required for community submission

config_notes: |
  After install:
  1. Set your Telegram bot_token in Settings → Channels → Telegram.
  2. Set your Anthropic API key in Settings → Models → Anthropic.
  3. Set your GitHub PAT in Settings → MCP → GitHub.

# Optional fields
min_synapse: ">=0.1.0"                # Minimum SYNAPSE version required by the bundle
icon: "🛠️"                            # Emoji icon (optional, shown in store UI)
repository: "https://github.com/username/my-bundle-source"  # Optional source link
```

### 2.2 Field Reference

| Field           | Required | Type         | Description                                                          |
|-----------------|----------|--------------|----------------------------------------------------------------------|
| `id`            | Yes      | string       | Unique bundle identifier. Kebab-case. Must match filename.           |
| `name`          | Yes      | string       | Display name shown in store UI.                                      |
| `version`       | Yes      | semver       | Bundle version. Bump on changes.                                     |
| `author`        | Yes      | string       | GitHub username of the primary author.                               |
| `description`   | Yes      | string       | Markdown-supported description (multi-line allowed).                 |
| `plugins`       | Yes      | list         | At least one plugin reference. Each must have `id` and `version`.   |
| `plugins[].id`  | Yes      | string       | Must resolve to a known plugin ID in at least one store source.      |
| `plugins[].version` | Yes  | semver range | Must be a valid semver range string (e.g. `>=1.0.0`, `^2.1.0`).   |
| `tags`          | No       | list(string) | Lowercase, no spaces. Used for store filtering.                      |
| `license`       | Yes*     | string       | SPDX license identifier. Required for community submission.          |
| `config_notes`  | No       | string       | Plain text post-install instructions shown to the user.              |
| `min_synapse`   | No       | semver range | If omitted, assumes compatible with all versions.                    |
| `icon`          | No       | string       | Single emoji character.                                              |
| `repository`    | No       | string       | URL to the bundle's own source/documentation repo.                   |

---

## 3. Bundle Creation Flow in the UI

Users create bundles through the SYNAPSE Dashboard without writing YAML manually. The UI generates the YAML and handles the pull-request submission automatically.

### 3.1 Step-by-Step

```
1. Navigate to: Dashboard → Store → Bundles → "Create New Bundle"

2. PLUGIN SELECTION
   → Multi-select list of all installed and available plugins
   → Search field to find plugins by name or category
   → Each selected plugin shows its resolved version range
   → At least 1 plugin must be selected to proceed

3. BUNDLE METADATA
   Form fields:
   → Bundle ID (auto-suggested from name, editable, validated for uniqueness)
   → Display Name
   → Description (rich text editor, rendered as Markdown)
   → Tags (tag input, comma-separated)
   → License (dropdown: MIT, Apache-2.0, GPL-3.0, other)
   → Config Notes (plain text area — guidance for users post-install)
   → Icon (emoji picker, optional)

4. PREVIEW
   → "Preview Bundle YAML" panel shows the generated YAML
   → User can review before submitting
   → Validation runs client-side: ID format, semver ranges, required fields

5. SUBMIT TO COMMUNITY
   → "Submit to Community" button
   → SYNAPSE authenticates with GitHub (OAuth — one-time setup or re-use existing token)
   → SYNAPSE forks synapse-community/bundles (if user hasn't already)
   → Commits [bundle-id].yml to the fork
   → Opens a pull request against synapse-community/bundles/main with:
     - Title: "Add bundle: [Bundle Name] v[version]"
     - Body: auto-generated from bundle description + checklist
   → Dashboard shows: "Pull request submitted: [PR URL]"
   → User can track PR status in Dashboard → Store → Bundles → "My Submissions"

6. AFTER MERGE
   → CI validates and merges PR
   → Next community store cache refresh picks up the new bundle
   → Bundle appears in the store with "Community" label
```

### 3.2 Editing an Existing Bundle

Users who have previously submitted a bundle can update it:

```
Dashboard → Store → Bundles → "My Submissions" → [Bundle] → "Edit"
→ Same form as creation, pre-filled with existing values
→ On submit: opens a new PR against the community repo with the updated YAML
→ PR title: "Update bundle: [Bundle Name] v[old] → v[new]"
```

---

## 4. Community Bundle Repository

The canonical community bundle repository is:

```
https://github.com/synapse-community/bundles
```

### 4.1 Repository Structure

```
synapse-community/bundles/
├── README.md                          # Contribution guide + bundle index
├── .github/
│   ├── workflows/
│   │   └── validate.yml               # CI validation workflow
│   └── PULL_REQUEST_TEMPLATE.md       # PR checklist template
├── bundles/
│   ├── developer-setup.yml
│   ├── home-automation.yml
│   ├── research-assistant.yml
│   └── [bundle-id].yml                # One file per bundle
└── schema/
    └── bundle.schema.json             # JSON Schema for YAML validation
```

### 4.2 PR-Based Review Workflow

All bundle submissions go through pull requests. This is the only path to adding or updating a bundle in the community store.

```
Contributor submits PR (via SYNAPSE UI or manually)
  ↓
CI runs automatically (see §6)
  ↓
If CI passes: PR is open for community review
  ↓
Maintainer reviews: checks description accuracy, usefulness, no malicious content
  ↓
Maintainer merges (squash merge preferred)
  ↓
SYNAPSE instances pick up on next community store cache refresh
```

### 4.3 Maintainer Guidelines

- Maintainers are community volunteers listed in `README.md`.
- Review focus: is the bundle useful and accurately described? Do the referenced plugins exist and work together?
- Maintainers do not review plugin code (that is the responsibility of the plugin's own source store).
- Bundles referencing only Official Store plugins are considered lower-risk.
- Bundles referencing Direct URL plugins must include a note explaining why.

### 4.4 Bundle Removal

If a bundle references plugins that have been removed from all sources, or if a bundle is found to contain misleading information:
- Maintainers open a PR to delete the YAML file.
- The bundle is marked stale in SYNAPSE store caches on next refresh.
- Users who have the bundle installed see a "Bundle unavailable" notice in the Dashboard.

---

## 5. Bundle Install Flow

Bundle installation follows the same flow as single-plugin installation (see `STORE_SPEC.md` §4) but iterates over all contained plugins.

### 5.1 Step-by-Step

```
1. TRIGGER
   synapse install --bundle developer-setup
   (or Store UI: bundle card → "Install Bundle")

2. BUNDLE DETAIL DISPLAY
   ┌──────────────────────────────────────────────────────────┐
   │  Developer Setup Bundle  v1.0.0           [Community]    │
   │  Author: username  │  License: MIT                       │
   │  ★ 892 stars  │  ↓ 3.4k downloads                       │
   │                                                          │
   │  Plugins included:                                       │
   │    ✓ telegram-channel   >=1.0.0   [Official]            │
   │    ✓ anthropic           >=1.0.0   [Official]            │
   │    ✓ github-mcp          >=0.2.0   [Community]           │
   │                                                          │
   │  Already installed:  none                                │
   │  Will install:       3 plugins                           │
   └──────────────────────────────────────────────────────────┘

3. CONFIRMATION
   "Install Developer Setup Bundle? This will install 3 plugins. [Yes/No/Details]"
   → Details: shows each plugin's own detail view inline

4. PLUGIN INSTALLATION (sequential)
   For each plugin in the bundle's `plugins` list:
   a. Check if already installed (matching ID + compatible version)
      → Already installed at compatible version: skip with "[skipped — already installed]"
      → Installed but incompatible version: prompt to upgrade
      → Not installed: proceed with full install flow (STORE_SPEC §4.2 steps 4–8)
   b. Guided configuration for each plugin's required config_schema fields
      (UI: one form per plugin, shown in sequence; CLI: one prompt block per plugin)
   c. Activation of each plugin before moving to the next

5. CONFIG NOTES DISPLAY
   After all plugins installed:
   → Show the bundle's `config_notes` field in a styled notice box
   → CLI: printed with a "Post-install notes:" header
   → UI: modal "Setup Complete" panel with the notes

6. CONFIRMATION
   CLI:  "Developer Setup Bundle installed. 3 plugins active."
   UI:   Green success banner + list of activated plugins
   Log:  STORE category, event: bundle.installed
```

### 5.2 Partial Install Handling

If one plugin in a bundle fails to install:
- SYNAPSE logs the failure (STORE/ERROR).
- Skips the failed plugin and continues with remaining plugins.
- At the end: "Bundle partially installed. Failed: github-mcp (checksum error). 2/3 plugins active."
- User can retry the failed plugin individually via `synapse install github-mcp`.

### 5.3 Already-Installed Plugin Behavior

| Scenario                                   | Behavior                                      |
|--------------------------------------------|-----------------------------------------------|
| Plugin installed, version satisfies range  | Skip — do not reinstall or reconfigure        |
| Plugin installed, version below range min  | Prompt to update to a compatible version      |
| Plugin installed, config incomplete        | Prompt to fill in missing required config     |
| Plugin not installed                       | Full install flow                             |

---

## 6. Bundle Statistics

### 6.1 Bundle-Level Stats

| Field        | Source                                              |
|--------------|-----------------------------------------------------|
| `downloads`  | Central store backend — incremented on each install |
| `stars`      | User ratings collected via store API                |

### 6.2 Aggregated Plugin Stats

Each plugin within a bundle shows its own individual stats (downloads, stars) as fetched from its source store. The bundle's `bundle_count` field on each plugin stat reflects how many community bundles include that plugin.

Stats are displayed in the bundle detail view as:

```
Bundle stats:    ★ 892 stars    ↓ 3.4k downloads
Included plugins:
  telegram-channel:   ★ 4.2k   ↓ 18k
  anthropic:          ★ 7.1k   ↓ 41k
  github-mcp:         ★ 1.3k   ↓ 6.2k
```

---

## 7. Validation Rules

All bundles must pass the following validation rules. These are enforced both by the CI pipeline on the community repo and by the SYNAPSE client when loading bundle data.

### 7.1 Structural Validation

| Rule                                    | Error if violated                                          |
|-----------------------------------------|------------------------------------------------------------|
| `id` matches the filename (minus `.yml`) | "Bundle ID mismatch: id field must equal filename"        |
| `id` is unique across all bundles in repo | "Duplicate bundle ID: [id]"                              |
| `id` is kebab-case only                 | "Invalid bundle ID format: use lowercase kebab-case"       |
| `version` is valid semver               | "Invalid version: must be semver (MAJOR.MINOR.PATCH)"      |
| At least 1 plugin in `plugins`          | "Bundle must contain at least one plugin"                  |
| All `plugins[].version` are valid semver ranges | "Invalid version range for plugin [id]"          |
| `license` is present                    | "License field is required for community submission"        |
| `name` and `description` are non-empty | "Name and description are required"                        |

### 7.2 Reference Validation

| Rule                                              | Error if violated                                     |
|---------------------------------------------------|-------------------------------------------------------|
| All `plugins[].id` values resolve to a known plugin ID in at least one store source | "Unknown plugin ID: [id]" |
| No two entries in `plugins` list share the same `id` | "Duplicate plugin in bundle: [id]"             |

### 7.3 Dependency Validation

| Rule                                              | Behavior                                              |
|---------------------------------------------------|-------------------------------------------------------|
| No circular dependencies between bundles          | Bundles cannot include other bundles — only plugins   |
| No self-reference                                 | A bundle cannot list itself in its plugins            |

Circular dependency check is trivially satisfied by the rule that bundles may only contain plugins, not other bundles. This is enforced by schema validation (the `plugins[].id` must resolve to a plugin type, not a bundle type).

### 7.4 CI Validation Workflow

The GitHub Actions workflow (`.github/workflows/validate.yml`) runs on every pull request:

```yaml
steps:
  - Checkout PR branch
  - Validate YAML syntax for all changed .yml files in bundles/
  - Validate against bundle.schema.json (JSON Schema)
  - Check: id matches filename
  - Check: id is unique across all bundles/
  - Check: all plugins[].id resolve against the Official Store API + community plugin index
  - Check: version and version ranges are valid semver
  - Check: no duplicate plugin IDs within a bundle
  - Report: pass/fail with detailed error messages as PR comment
```

CI must pass before a maintainer will merge a PR. CI failures block the merge via GitHub branch protection rules.

---

## 8. Examples

### 8.1 Minimal Bundle

```yaml
id: ollama-local
name: "Local Ollama Setup"
version: 1.0.0
author: community-contributor
description: "Run SYNAPSE fully offline with a local Ollama model."
license: MIT
plugins:
  - id: ollama
    version: ">=1.0.0"
config_notes: |
  After install: make sure Ollama is running locally (http://localhost:11434).
  Set your preferred model in Settings → Models → Ollama.
tags:
  - offline
  - local
  - privacy
```

### 8.2 Full Bundle

```yaml
id: developer-setup
name: "Developer Setup Bundle"
version: 1.2.0
author: synapse-contributor
description: |
  Pre-configured developer environment combining Telegram notifications,
  Claude as the AI model, and GitHub project integration via MCP.
  Ideal for developers who want AI-assisted project management.
license: MIT
tags:
  - development
  - coding
  - github
  - telegram
min_synapse: ">=0.1.0"
icon: "🛠️"
plugins:
  - id: telegram-channel
    version: ">=1.0.0"
  - id: anthropic
    version: ">=1.0.0"
  - id: github-mcp
    version: ">=0.2.0"
config_notes: |
  After install:
  1. Configure your Telegram bot token in Settings → Channels → Telegram.
     Create a bot via @BotFather if you don't have one.
  2. Add your Anthropic API key in Settings → Models → Anthropic.
  3. Add your GitHub Personal Access Token in Settings → MCP → GitHub.
     Required scopes: repo, read:org
  4. Assign the GitHub MCP server to your Main Agent in Agent → Config.
```
