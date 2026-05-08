# {SYSTEM_NAME} Store Concept

## Overview

The {SYSTEM_NAME} Store is the central marketplace for discovering, installing, and managing plugins, bundles, models, skills, MCP tools, and themes. It supports multiple source types and provides a unified interface for all plugin-related operations.

---

## Store Source Types

### 1. Official Store (Private)

- Maintained exclusively by the {SYSTEM_NAME} core team.
- All entries carry a **verified badge** to distinguish them from community contributions.
- Plugins undergo mandatory security review, API compatibility checks, and performance validation before listing.
- Not open for external submissions.
- Accessed via the internal official store API endpoint configured at install time.

### 2. Community Store (Public)

- Hosted at **`synapse-community/bundles`** on GitHub (public repository).
- Anyone can submit plugins and bundles via **Pull Request**.
- CI validation pipeline runs on every PR: checks plugin ID uniqueness, validates semantic versioning, verifies declared dependencies exist, runs schema validation.
- Merged PRs are automatically indexed and become available in the store UI within the configured cache refresh interval.
- Plugins are NOT reviewed for security by the core team — users install at their own discretion.

### 3. skills.sh

- A dedicated plugin registry with its own API and infrastructure, operated independently.
- {SYSTEM_NAME} connects directly to the skills.sh API to query and install plugins.
- No PR process — plugin authors publish directly to skills.sh.
- Accessed via direct API integration, separate from the community GitHub repo.

### 4. ACP Registry (Agent Communication Protocol Registry)

- A provider discovery registry for ACP-compatible agents and services.
- Used to discover third-party AI providers, tool registries, and compatible agent endpoints.
- Primarily used for model and MCP tool discovery rather than standard plugins.

### 5. Direct URL (GitHub / GitLab)

- Install any plugin directly from a public or private GitHub or GitLab repository URL.
- Useful for private plugins, development builds, forks of community plugins, or unreleased versions.
- {SYSTEM_NAME} fetches the plugin manifest from the repository root and proceeds with the standard install flow.
- No store listing required — direct URL bypasses all store caches.

---

## Plugin Statistics

Each plugin in the store tracks the following statistics:

| Field | Description |
|---|---|
| `downloads` | Total number of installs across all {SYSTEM_NAME} instances that report to the store |
| `stars` | Aggregate user ratings (1–5 stars), shown as average and total count |
| `bundle_count` | Number of published bundles that include this plugin |
| `latest_version` | Most recent published semantic version (e.g. `1.4.2`) |
| `update_date` | ISO 8601 timestamp of the most recent version release |
| `compatibility` | Version range specifying compatible {SYSTEM_NAME} versions (e.g. `>=1.0.0 <2.0.0`), expressed as `synapse_version` range |

Stats are updated during the store cache refresh cycle and displayed in plugin detail cards.

---

## Filtering and Discovery

The store UI and API support the following filter dimensions:

### By Category

| Category | Description |
|---|---|
| `channel` | Messaging integrations (Telegram, Discord, Slack, etc.) |
| `model` | AI model provider connectors |
| `skill` | Reusable agent skill modules |
| `mcp` | Model Context Protocol tool servers |
| `bundle` | Curated collections of plugins |
| `theme` | Dashboard visual themes |

### By Popularity

- `sort=stars` — sort by average star rating
- `sort=downloads` — sort by total install count
- `sort=updated` — sort by most recently updated

### By Compatibility

- Filter by your current {SYSTEM_NAME} version (auto-populated from system info).
- Incompatible plugins are shown with a warning badge and cannot be installed unless compatibility check is overridden.

### By Type / Source

- Filter by source: `official`, `community`, `skills.sh`, `acp`, `direct`
- Filter by verified status: `verified=true` shows only official and verified community plugins

---

## Store UI Pages

### `/store` — Main Store Page

The main store page is divided into the following sections:

**Category Navigation**
- Sidebar or top-bar tabs for each category: All, Channels, Models, Skills, MCP, Bundles, Themes.
- Each category shows item count and a "Featured" section curated by the {SYSTEM_NAME} team.

**Installed Plugins Panel**
- Lists all currently installed plugins.
- Plugins with available updates display an orange update indicator badge.
- Each entry shows: plugin name, current version, installed-from source, quick-uninstall button.

**Plugin Cards**
Each plugin card displays:
- Plugin name and author
- Short description
- Category badge and source badge (official/community/skills.sh)
- Star rating (visual stars + numeric average)
- Download count
- Bundle count (number of bundles that include it)
- Latest version and last updated date
- Compatibility status for current {SYSTEM_NAME} version
- "Install" / "Installed" / "Update Available" action button

**Bundle Creator**
- Accessible from the Bundles tab via "Create New Bundle".
- See `bundle-system.md` for the full bundle creation flow.

---

## Install Flow

The full plugin installation flow, whether initiated from the store UI or via CLI command:

### Step 1: Initiation

- **Store UI**: User clicks "Install" on a plugin card.
- **CLI**: User runs `synapse install [plugin-id]` or `synapse install --url [url]`.
- **Dashboard chat**: User types `/install [plugin-id]` in the chat panel.

### Step 2: Info Card

Before any installation begins, {SYSTEM_NAME} presents a full information card:

```
Plugin: Telegram Channel
Version: 2.1.0
Author: synapse-community / @username
Source: Community Store

Description:
  Connects {SYSTEM_NAME} to Telegram bots and groups.

Permissions requested:
  - Network: outbound HTTPS to api.telegram.org
  - Storage: read/write to plugin data directory
  - Channels: register as channel provider

Cost impact:
  - No additional API cost (uses Telegram Bot API, free tier)

Stats:
  - Downloads: 14,230
  - Stars: 4.7 / 5 (312 ratings)
  - Included in 8 bundles

Compatibility:
  - Requires {SYSTEM_NAME} >= 1.0.0
  - Compatible with current version (1.2.0) ✓

Install? [Yes] [No] [Details]
```

- **[Details]** opens the full plugin page with changelog, screenshots, README, and dependency graph.
- **[No]** cancels without any changes.
- **[Yes]** proceeds to Step 3.

### Step 3: Guided Configuration Wizard

If the plugin has required configuration fields, a step-by-step wizard is presented:

```
Configuring: Telegram Channel (Step 1 of 2)

Bot Token
  Your Telegram Bot API token from @BotFather.
  Example: 1234567890:ABCdefGHIjklMNOpqrSTUvwxYZ

  > ___________________________

[Back] [Next]
```

- All required fields must be completed before proceeding.
- Optional fields are shown with `[skip]` option.
- Sensitive fields (tokens, passwords) are masked on input and stored in the vault.
- The wizard validates each field format before allowing progression.

### Step 4: Download and Verify

- Plugin package is downloaded from the source.
- SHA-256 checksum is verified against the manifest.
- For official plugins, GPG signature is verified.
- If verification fails, installation is aborted with an error message.

### Step 5: Activation

- Plugin is registered in the database.
- Plugin's `on_install` lifecycle hook is executed.
- Plugin is initialized and started.
- Plugin appears in the active plugin list.

### Step 6: Confirmation

```
✓ Telegram Channel 2.1.0 installed successfully.
  Active and ready. Configure via Settings → Plugins → Telegram Channel.
```

---

## Update Flow

### Notifications

- **Dashboard**: A notification badge appears in the top navigation showing the count of available updates (e.g. "2 updates available"). Clicking opens the Plugins page with updates highlighted.
- **CLI**: Running any `synapse` command while updates are available displays a warning:

```
Warning: 2 plugin updates available. Run `synapse update` to update all, or `synapse update [plugin-id]` for a specific plugin.
```

### Updating

- **CLI**: `synapse update [plugin-id]` updates a specific plugin. `synapse update` updates all plugins.
- **UI**: One-click "Update" button on the installed plugin entry in the store or plugins page.

### Update Process

1. Current version is backed up.
2. New version is downloaded and verified.
3. Plugin's `on_update` hook is called with old and new version numbers.
4. Plugin is reloaded with the new version.
5. Confirmation message is shown.

If the update fails, the previous version is automatically restored and an error is logged in the PLUGIN category.

---

## Uninstall Flow

### Initiation

- **CLI**: `synapse uninstall [plugin-id]`
- **UI**: Settings → Plugins → [plugin] → "Uninstall" button
- **Dashboard chat**: `/uninstall [plugin-id]`

### Confirmation

```
Uninstall Telegram Channel?
  This will remove the plugin and its configuration.
  Conversation history using this channel will be retained.
  
  [Confirm] [Cancel]
```

### Uninstall Process

1. User confirms uninstallation.
2. Plugin's `on_uninstall` lifecycle hook is executed (allows cleanup of external resources, e.g. deregistering webhooks).
3. Plugin entry removed from the database.
4. Plugin files removed from the plugin directory.
5. Vault entries specific to this plugin are cleared.
6. Confirmation:

```
✓ Telegram Channel has been uninstalled.
```

---

## Local Plugin Loading

Plugins can be loaded from a local directory without going through the store:

### Method 1: CLI

```bash
# Place plugin folder in the plugins directory, then:
synapse plugins reload

# Or specify a path directly:
synapse plugins load --path /path/to/my-plugin
```

### Method 2: Dashboard

1. Navigate to **Dashboard → Plugins**.
2. Click **"Load from Disk"** in the top-right toolbar.
3. Enter the absolute path to the plugin directory.
4. {SYSTEM_NAME} reads the `plugin.yml` manifest from that directory.
5. If the manifest is valid, the standard info card and config wizard are presented.
6. Plugin is registered as a `local` source plugin.

Local plugins are marked with a "Local" badge and are not subject to store cache or update checks.

---

## Store Cache

To enable offline browsing and reduce external API load, {SYSTEM_NAME} maintains a local cache of store data.

### Storage

- Cached in the **`store_cache`** PostgreSQL table.
- Schema includes: `source`, `plugin_id`, `data` (JSONB), `cached_at`, `expires_at`.

### Refresh Intervals

Configurable per source in system settings:

| Source | Default Refresh Interval |
|---|---|
| Official | 24 hours |
| Community | 6 hours |
| skills.sh | 12 hours |
| ACP Registry | 24 hours |

- Manual refresh: **Settings → Store → "Refresh Cache Now"** or `synapse store refresh`.
- Cache refresh is performed as a background job on the configured interval.

### Offline Browsing

- When the external store API is unreachable, {SYSTEM_NAME} serves plugin data from the cache.
- An "Offline — showing cached data" banner is displayed in the store UI.
- Installation of cached plugins is still possible if the plugin package URL is reachable or the package is already downloaded.

---

## Store API

All store API endpoints require authentication (JWT Bearer token in `Authorization` header).

### `GET /api/store/plugins`

Retrieve a list of plugins from the store.

**Query Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `type` | string | Filter by category: `channel`, `model`, `skill`, `mcp`, `bundle`, `theme` |
| `source` | string | Filter by source: `official`, `community`, `skills.sh`, `acp` |
| `sort` | string | Sort order: `stars`, `downloads`, `updated`, `name` |
| `compatible` | boolean | If `true`, only return plugins compatible with current {SYSTEM_NAME} version |
| `q` | string | Full-text search query |
| `page` | integer | Page number (default: 1) |
| `limit` | integer | Results per page (default: 20, max: 100) |

**Response:**

```json
{
  "data": [
    {
      "id": "telegram-channel",
      "name": "Telegram Channel",
      "description": "Connects {SYSTEM_NAME} to Telegram bots and groups.",
      "version": "2.1.0",
      "author": "synapse-community",
      "source": "community",
      "category": "channel",
      "verified": false,
      "stats": {
        "downloads": 14230,
        "stars": 4.7,
        "star_count": 312,
        "bundle_count": 8
      },
      "compatibility": {
        "synapse_version": ">=1.0.0 <2.0.0",
        "compatible": true
      },
      "latest_version": "2.1.0",
      "update_date": "2026-04-15T10:30:00Z"
    }
  ],
  "total": 47,
  "page": 1,
  "limit": 20
}
```

---

### `GET /api/store/plugins/:id`

Retrieve full details for a single plugin.

**Response:**

```json
{
  "id": "telegram-channel",
  "name": "Telegram Channel",
  "description": "Connects {SYSTEM_NAME} to Telegram bots and groups.",
  "long_description": "Full README content as markdown...",
  "version": "2.1.0",
  "author": "synapse-community",
  "source": "community",
  "category": "channel",
  "verified": false,
  "permissions": [
    "network:outbound:api.telegram.org",
    "storage:plugin-data",
    "channels:register"
  ],
  "config_schema": {
    "bot_token": { "type": "string", "required": true, "secret": true },
    "webhook_url": { "type": "string", "required": false }
  },
  "dependencies": [],
  "stats": {
    "downloads": 14230,
    "stars": 4.7,
    "star_count": 312,
    "bundle_count": 8
  },
  "compatibility": {
    "synapse_version": ">=1.0.0 <2.0.0",
    "compatible": true
  },
  "changelog": [
    { "version": "2.1.0", "date": "2026-04-15", "notes": "Added webhook support." }
  ],
  "latest_version": "2.1.0",
  "update_date": "2026-04-15T10:30:00Z",
  "repository_url": "https://github.com/synapse-community/telegram-channel"
}
```

**Error Codes:**

| Code | Status | Description |
|---|---|---|
| `PLUGIN_NOT_FOUND` | 404 | No plugin with the given ID exists in the store |
| `STORE_UNAVAILABLE` | 503 | Store source is unreachable and cache is empty |

---

### `POST /api/store/install/:id`

Initiate installation of a plugin.

**Request Body:**

```json
{
  "source": "community",
  "config": {
    "bot_token": "1234567890:ABCdef..."
  }
}
```

**Response:**

```json
{
  "status": "installing",
  "plugin_id": "telegram-channel",
  "version": "2.1.0",
  "job_id": "install-job-abc123"
}
```

Installation is asynchronous. Monitor progress via WebSocket `/ws/logs` filtering on `PLUGIN` category.

**Error Codes:**

| Code | Status | Description |
|---|---|---|
| `ALREADY_INSTALLED` | 409 | Plugin is already installed |
| `INCOMPATIBLE_VERSION` | 422 | Plugin is not compatible with current {SYSTEM_NAME} version |
| `CHECKSUM_FAILED` | 422 | Downloaded package failed integrity check |
| `CONFIG_INVALID` | 422 | Provided configuration does not satisfy plugin schema |

---

### `DELETE /api/store/plugins/:id`

Uninstall a plugin.

**Response:**

```json
{
  "status": "uninstalled",
  "plugin_id": "telegram-channel"
}
```

**Error Codes:**

| Code | Status | Description |
|---|---|---|
| `PLUGIN_NOT_FOUND` | 404 | No plugin with this ID is installed |
| `UNINSTALL_HOOK_FAILED` | 500 | Plugin's `on_uninstall` hook returned an error |
