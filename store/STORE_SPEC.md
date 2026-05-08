# SYNAPSE Store System — Complete Specification

> This document specifies the full design of the SYNAPSE plugin store: source types, plugin metadata, filtering, install/update/uninstall flows, local loading, caching, API endpoints, and logging.

---

## 1. Store Source Types

SYNAPSE integrates five distinct plugin sources. Each source has its own trust level, refresh interval, and UI presentation. All five are unified under the single `/store` command and Store UI.

### 1.1 Official Store

| Property        | Value                                                    |
|-----------------|----------------------------------------------------------|
| URL             | `https://store.synapse.dev/official` (private external)  |
| Trust Level     | `verified`                                               |
| UI Badge        | "Official" — blue verified badge                         |
| Refresh         | Every 24 hours (configurable)                            |
| Curation        | SYNAPSE core team only — invite-only submission          |
| Plugin Types    | channel, model, skill, mcp, theme                        |

The Official Store is a private repository maintained exclusively by the SYNAPSE core team. All plugins in it have been audited for security, stability, and API correctness. They carry a verified badge in all UI surfaces and CLI output. Users cannot submit to this store directly; see `submit-plugin.md` for the invitation-based process.

### 1.2 Community Store

| Property        | Value                                                        |
|-----------------|--------------------------------------------------------------|
| Repository      | `https://github.com/synapse-community/bundles` (public)      |
| Trust Level     | `community`                                                  |
| UI Badge        | "Community" — grey label                                     |
| Refresh         | Every 6 hours (configurable)                                 |
| Submission      | Pull-request based, open to all users                        |
| Format          | One YAML file per bundle (see `BUNDLE_SPEC.md`)              |

The Community Store is a public GitHub repository. Any user can submit a bundle via pull request. Bundles are validated by CI before merge (schema correctness, referenced plugin IDs, semver ranges). Community-sourced content is always labeled distinctly from Official content — a "Community" label appears on every card, detail page, and CLI listing to prevent confusion.

### 1.3 skills.sh

| Property        | Value                                        |
|-----------------|----------------------------------------------|
| API URL         | `https://skills.sh/api/v1`                   |
| Trust Level     | `community`                                  |
| UI Badge        | "skills.sh"                                  |
| Refresh         | Every 12 hours (configurable)                |
| Submission      | Via `/skills publish` inside SYNAPSE          |

skills.sh is a dedicated platform for AI skills. SYNAPSE queries its public API to list available skills and retrieves metadata (author, description, version, usage count). Stats (downloads, stars) are loaded directly from the skills.sh API — SYNAPSE does not re-host this data. Bundle counts for skills.sh skills are surfaced as best-effort from local correlation.

### 1.4 ACP Registry

| Property        | Value                                               |
|-----------------|-----------------------------------------------------|
| API URL         | `https://registry.acp.dev/api/v1`                   |
| Trust Level     | `verified`                                          |
| UI Badge        | "ACP Registry" — teal badge                         |
| Refresh         | Every 24 hours (configurable)                       |
| Content         | AI provider definitions, agent capability specs     |

The Agent Communication Protocol (ACP) Registry provides a curated directory of AI providers and agent definitions. SYNAPSE taps this registry to offer zero-configuration provider setup: the user selects a provider, enters their credential (API key or subscription ID), and SYNAPSE auto-loads the endpoint, model list, and capabilities from ACP. No plugin file is required for ACP-registered providers. New providers become available automatically when added to the ACP registry without requiring a SYNAPSE update.

### 1.5 Direct URL

| Property        | Value                                                        |
|-----------------|--------------------------------------------------------------|
| Source          | Any GitHub or GitLab repository URL                          |
| Trust Level     | `unverified`                                                 |
| UI Badge        | "Direct URL" — orange warning badge                          |
| Validation      | Manifest schema check only (no code audit)                   |

Users can install a plugin directly from any public GitHub or GitLab URL pointing to a repository containing a valid `manifest.yml`. SYNAPSE downloads the repository, validates the manifest schema, and proceeds through the normal install flow. Direct URL installs are always marked "unverified" in the UI and database. A warning is displayed before install: "This plugin has not been reviewed by SYNAPSE or the community. Install only from sources you trust."

---

## 2. Plugin Statistics

Every plugin in the store exposes the following statistics. These are hosted centrally (official store backend) and synced into the local `plugin_stats` table on each store refresh. skills.sh stats are fetched live from the skills.sh API.

### 2.1 Per-Plugin Metrics

| Field               | Type    | Description                                                |
|---------------------|---------|------------------------------------------------------------|
| `downloads`         | integer | Total install count across all SYNAPSE instances reporting |
| `stars`             | integer | User ratings (1 star = thumbs-up, aggregated)              |
| `bundle_count`      | integer | Number of community bundles that include this plugin       |
| `latest_version`    | string  | Semver string of the current release (e.g. `1.3.2`)        |
| `min_synapse`       | string  | Minimum compatible SYNAPSE version (e.g. `>=0.1.0`)        |
| `max_synapse`       | string  | Maximum compatible SYNAPSE version if bounded (optional)   |
| `updated_at`        | ISO8601 | Date/time of the latest version release                    |
| `author`            | string  | Plugin author name or organization                         |
| `license`           | string  | SPDX license identifier (e.g. `MIT`, `Apache-2.0`)         |

### 2.2 Database Tables

```sql
-- Cached store index (full plugin list from all sources)
store_cache (
  id              UUID PRIMARY KEY,
  source          TEXT,        -- official | community | skills_sh | acp | direct_url
  plugin_id       TEXT,
  manifest_json   JSONB,
  fetched_at      TIMESTAMPTZ,
  expires_at      TIMESTAMPTZ
)

-- Per-plugin stats (synced from central store or skills.sh API)
plugin_stats (
  plugin_id       TEXT PRIMARY KEY,
  downloads       INTEGER DEFAULT 0,
  stars           INTEGER DEFAULT 0,
  bundle_count    INTEGER DEFAULT 0,
  latest_version  TEXT,
  min_synapse     TEXT,
  max_synapse     TEXT,
  updated_at      TIMESTAMPTZ,
  synced_at       TIMESTAMPTZ
)
```

---

## 3. Filtering Options

The Store UI and CLI both support the same filtering parameters.

### 3.1 Filter Dimensions

| Filter         | Values                                              | Notes                                              |
|----------------|-----------------------------------------------------|----------------------------------------------------|
| `sort`         | `popularity`, `downloads`, `stars`, `newest`        | Default: `popularity` (combined stars + downloads) |
| `category`     | `channel`, `model`, `skill`, `mcp`, `bundle`, `theme` | Multi-select allowed                             |
| `source`       | `official`, `community`, `skills_sh`, `acp`, `direct_url` | Multi-select allowed                        |
| `compatibility`| SYNAPSE version string                              | Shows only plugins compatible with current version |
| `search`       | Free text                                           | Matches name, description, tags, author            |
| `has_update`   | boolean                                             | Filter to plugins with available updates           |

### 3.2 CLI Filter Syntax

```
synapse store list --category channel --sort stars
synapse store list --category model,skill --sort downloads --compatible
synapse store search "telegram"
synapse store list --source official
synapse store list --has-update
```

### 3.3 API Filter Parameters

```
GET /api/store/plugins?category=channel&sort=stars&compatible=true&q=telegram
GET /api/store/plugins?source=official,community&category=bundle
```

---

## 4. Install Flow

The install flow is identical whether triggered by CLI command or Store UI. Both paths converge on the same backend service.

### 4.1 Trigger

**CLI:**
```
synapse install telegram-channel
/install telegram-channel          (via Main Agent chat)
```

**Store UI:**
```
Store → browse or search → click plugin card → "Install" button
```

### 4.2 Step-by-Step Flow

```
1. LOOKUP
   → SYNAPSE resolves the plugin ID against all enabled sources
   → If ambiguous (same ID in multiple sources), prompts user:
     "Found 'telegram-channel' in Official and Community. Which to install?"

2. INFO DISPLAY
   Shows plugin detail screen (CLI: formatted table; UI: detail panel):
   ┌─────────────────────────────────────────────────────────┐
   │  telegram-channel  v1.0.0                  [Official ✓] │
   │  Author: synapse-core  │  License: MIT                  │
   │  ★ 4.2k stars  │  ↓ 18k downloads  │  in 23 bundles   │
   │  Compatible: SYNAPSE >=0.1.0                            │
   │                                                         │
   │  Permissions required:                                  │
   │    - Network: outbound HTTPS to api.telegram.org        │
   │    - Database: read/write installed_channels            │
   │                                                         │
   │  Estimated cost impact: none (free plugin)             │
   │  Description: Connect a Telegram bot as a channel.     │
   └─────────────────────────────────────────────────────────┘

3. CONFIRMATION PROMPT
   "Install telegram-channel v1.0.0? [Yes/No/Details]"
   → Details: shows full manifest, hooks list, changelog

4. DOWNLOAD & VERIFY
   → Download plugin from source URL
   → Verify checksum (SHA-256, signed by source)
   → Reject if checksum fails — log STORE/ERROR

5. GUIDED CONFIGURATION
   → For each field in manifest.yml `config_schema` where `required: true`:
     CLI:  "Enter bot_token (secret, will be encrypted):"
     UI:   Form field with label, type=password if secret=true
   → Optional fields shown with [Enter to skip] note
   → Secret fields are encrypted at rest (AES-256) before DB insert

6. HOOK EXECUTION
   → Runs `on_install` hook if defined in manifest
   → Hook failure: rollback install, log PLUGIN/ERROR

7. DATABASE REGISTRATION
   → Insert into `plugins` table (status: active)
   → Insert into appropriate `installed_*` table with config JSON

8. ACTIVATION
   → Plugin loaded by runtime plugin manager
   → Channel/model/skill registered and ready

9. CONFIRMATION
   CLI:  "telegram-channel v1.0.0 is now active."
   UI:   Green toast notification + plugin appears in active list
   Log:  STORE category, event: plugin.installed
```

---

## 5. Update Flow

### 5.1 Update Detection

SYNAPSE checks for plugin updates on every store cache refresh (configurable interval, default 24h for official, 6h for community). The comparison is between the installed version (from `plugins` table) and `plugin_stats.latest_version`.

### 5.2 Notification

**Dashboard:** A badge counter appears on the "Plugins" navigation item showing the number of available updates. An "Updates available" banner appears at the top of the Plugins page listing each plugin with its current and new version.

**CLI:**
```
synapse plugins check-updates

Output:
  3 updates available:
  telegram-channel   1.0.0  →  1.1.2   [Official]
  anthropic          1.0.0  →  1.2.0   [Official]
  my-community-skill 0.3.1  →  0.4.0   [Community]
```

### 5.3 Update Execution

```
synapse update telegram-channel        (single plugin)
synapse update --all                   (all plugins with updates)
```

**Flow:**
```
1. Show diff: current version → new version, changelog if available
2. Confirm: "Update telegram-channel from 1.0.0 to 1.1.2? [Yes/No]"
3. Download + verify new version
4. Run on_uninstall hook of old version (config preserved)
5. Install new version files
6. Re-apply existing config (validate against new config_schema)
   → If new required fields added: prompt user to fill them in
7. Run on_install hook of new version
8. Activate new version
9. Log: PLUGIN category, event: plugin.updated
10. Confirmation: "telegram-channel updated to 1.1.2."
```

Dashboard "Update All" button runs the same flow for each plugin sequentially with a progress indicator.

---

## 6. Uninstall Flow

```
synapse uninstall telegram-channel
```

**Flow:**
```
1. CONFIRMATION
   "Uninstall telegram-channel? This will stop all Telegram connections. [Yes/No]"
   → UI: shows which agents and channels depend on this plugin

2. DEPENDENCY CHECK
   → Check if any agent configs reference this plugin
   → If dependencies found:
     "Warning: 2 agents use this channel. They will lose Telegram access."
     "Continue? [Yes/No]"

3. HOOK EXECUTION
   → Run on_uninstall hook defined in manifest
   → Hook handles external cleanup (e.g. deregister webhook with Telegram API)
   → Hook failure: log PLUGIN/WARN but continue uninstall

4. DEACTIVATION
   → Plugin removed from runtime plugin manager
   → Channel/model/skill deregistered

5. DATABASE CLEANUP
   → Mark plugin status: uninstalled in `plugins` table
   → Remove rows from `installed_channels` (or equivalent typed table)
   → Config JSON deleted (secrets purged from memory)
   → plugin_stats row retained (for re-install stats display)

6. FILE CLEANUP
   → Plugin files removed from plugin directory

7. CONFIRMATION
   CLI:  "telegram-channel has been uninstalled."
   UI:   Toast notification, plugin removed from active list
   Log:  PLUGIN category, event: plugin.uninstalled
```

---

## 7. Local Plugin Loading

For development, testing, or offline use, SYNAPSE supports loading plugins directly from the local filesystem without going through the store.

### 7.1 Via CLI

```
synapse plugins reload
```

SYNAPSE scans the following directories for new or changed `manifest.yml` files:
- `plugins/channels/`
- `plugins/models/`
- `plugins/skills/`
- `plugins/mcp/`

Any plugin folder containing a valid `manifest.yml` that is not already registered in the database is loaded and registered. Changed manifests trigger a reload of the affected plugin.

```
synapse plugins reload

Output:
  Scanning plugin directories...
  New:     my-custom-channel (plugins/channels/my-custom-channel)
  Changed: anthropic v1.0.0 → v1.0.1-dev (plugins/models/anthropic)
  Skipped: telegram-channel (no changes)

  2 plugins reloaded.
```

### 7.2 Via Dashboard

Navigate to: **Dashboard → Plugins → "Load from Disk"**

The same scan runs server-side and the results are displayed in a modal with a list of detected changes. The user can selectively apply individual plugin reloads or apply all at once.

### 7.3 Local Plugin Source Label

Locally loaded plugins are labeled "Local" in the UI and CLI output. They do not have store stats (downloads, stars) and do not appear in store search results. They are registered in the `plugins` table with `source: local`.

---

## 8. Store Caching

### 8.1 Purpose

The store cache enables offline browsing of previously fetched plugin metadata. When SYNAPSE cannot reach a store source, it falls back to cached data and displays a "Cached — last updated [time]" notice in the UI.

### 8.2 Cache Table

```sql
store_cache (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  source          TEXT NOT NULL,
  plugin_id       TEXT NOT NULL,
  manifest_json   JSONB NOT NULL,
  fetched_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at      TIMESTAMPTZ NOT NULL,
  UNIQUE (source, plugin_id)
)
```

### 8.3 TTL Configuration

TTL is per-source and configurable in System Settings → Store → Cache TTL:

| Source     | Default TTL  | Minimum TTL |
|------------|--------------|-------------|
| official   | 24 hours     | 1 hour      |
| community  | 6 hours      | 30 minutes  |
| skills_sh  | 12 hours     | 1 hour      |
| acp        | 24 hours     | 1 hour      |

### 8.4 Cache Refresh

- Automatic refresh runs as a background job at the configured intervals.
- Manual refresh: `synapse store refresh` (CLI) or Dashboard → Store → "Refresh Now" button.
- On refresh, rows with matching `(source, plugin_id)` are upserted; rows for plugins no longer present in the source are marked stale (not deleted immediately to preserve offline access for 7 days).

### 8.5 Offline Behavior

When a store source is unreachable:
- The cache is used transparently.
- CLI output includes: `[Using cached data — last updated 2026-05-07T14:30:00Z]`
- Install attempts for non-cached plugins fail with: "Plugin files unavailable offline. Connect to the network and try again."
- Install attempts for cached plugins with downloaded files proceed normally.

---

## 9. Official vs. Community Distinction

SYNAPSE enforces a clear visual and informational separation between Official and Community content at every surface.

### 9.1 UI Treatment

| Element              | Official                              | Community                          |
|----------------------|---------------------------------------|------------------------------------|
| Plugin card badge    | Blue "Official ✓" badge               | Grey "Community" label             |
| Detail page header   | Blue verified banner                  | Grey community notice              |
| Install prompt       | No extra warning                      | "Community plugin — review before install" |
| CLI listing          | `[Official]` tag in green             | `[Community]` tag in grey          |

### 9.2 Trust Level in Database

The `plugins` table stores `trust_level: verified | community | unverified` (sourced from registry). This field drives all UI trust indicators and is never overridden by user action.

---

## 10. API Endpoints

All store API endpoints require authentication (Bearer token). Rate limiting applies: 60 requests/minute per user for browse endpoints, 10 requests/minute for install endpoints.

### 10.1 List Plugins

```
GET /api/store/plugins

Query Parameters:
  q           string    Full-text search (name, description, tags, author)
  category    string    Comma-separated: channel,model,skill,mcp,bundle,theme
  source      string    Comma-separated: official,community,skills_sh,acp
  sort        string    popularity | downloads | stars | newest
  compatible  boolean   Filter to plugins compatible with current SYNAPSE version
  has_update  boolean   Filter to installed plugins with available updates
  page        integer   Page number (default: 1)
  per_page    integer   Results per page (default: 20, max: 100)

Response 200:
{
  "total": 142,
  "page": 1,
  "per_page": 20,
  "plugins": [
    {
      "id": "telegram-channel",
      "name": "Telegram Channel",
      "version": "1.0.0",
      "type": "channel",
      "source": "official",
      "trust_level": "verified",
      "author": "synapse-core",
      "license": "MIT",
      "description": "Connect a Telegram bot as a channel.",
      "tags": ["messaging", "bot"],
      "stats": {
        "downloads": 18432,
        "stars": 4201,
        "bundle_count": 23,
        "updated_at": "2026-04-15T10:00:00Z"
      },
      "compatibility": {
        "min_synapse": ">=0.1.0"
      }
    }
  ]
}
```

### 10.2 Get Plugin Detail

```
GET /api/store/plugins/:id

Path Parameters:
  id    string    Plugin ID (e.g. telegram-channel)

Query Parameters:
  source  string  Optional — disambiguate if ID exists in multiple sources

Response 200:
{
  "id": "telegram-channel",
  "name": "Telegram Channel",
  "version": "1.0.0",
  "type": "channel",
  "source": "official",
  "trust_level": "verified",
  "author": "synapse-core",
  "license": "MIT",
  "description": "Connect a Telegram bot as a channel.",
  "tags": ["messaging", "bot"],
  "stats": {
    "downloads": 18432,
    "stars": 4201,
    "bundle_count": 23,
    "updated_at": "2026-04-15T10:00:00Z"
  },
  "compatibility": {
    "min_synapse": ">=0.1.0"
  },
  "config_schema": {
    "bot_token": { "type": "string", "required": true, "secret": true },
    "allowed_users": { "type": "list", "required": false }
  },
  "hooks": ["on_install", "on_uninstall", "on_message", "on_send"],
  "permissions": [
    "network:outbound:api.telegram.org",
    "db:read_write:installed_channels"
  ],
  "changelog": [
    { "version": "1.0.0", "date": "2026-04-15", "notes": "Initial release." }
  ]
}

Response 404:
{ "error": "Plugin not found", "id": "telegram-channel" }
```

### 10.3 Install Plugin

```
POST /api/store/install/:id

Path Parameters:
  id    string    Plugin ID to install

Request Body:
{
  "source": "official",           // optional, for disambiguation
  "config": {
    "bot_token": "encrypted:...", // pre-encrypted by client
    "allowed_users": ["@alice", "@bob"]
  }
}

Response 200:
{
  "status": "installed",
  "plugin_id": "telegram-channel",
  "version": "1.0.0",
  "activated_at": "2026-05-08T09:14:32Z"
}

Response 400:
{
  "error": "Missing required config field: bot_token"
}

Response 409:
{
  "error": "Plugin already installed",
  "installed_version": "1.0.0"
}
```

---

## 11. Logging

All store-related events are logged under the `STORE` category. Log entries follow the standard SYNAPSE log format (see `SYNAPSE_OPUS_PLAN.md` §4.12).

### 11.1 Store Log Events

| Event                      | Level  | Trigger                                              |
|----------------------------|--------|------------------------------------------------------|
| `store.browse`             | DEBUG  | User opens store / runs `synapse store list`         |
| `store.search`             | DEBUG  | User searches with a query string                    |
| `store.plugin_detail`      | DEBUG  | User views a plugin detail page                      |
| `store.install_requested`  | INFO   | User confirms install prompt                         |
| `store.download_started`   | INFO   | Plugin download begins                               |
| `store.download_complete`  | INFO   | Plugin downloaded and checksum verified              |
| `store.download_failed`    | ERROR  | Network error or checksum mismatch                   |
| `store.install_complete`   | INFO   | Plugin installed, activated, DB registered           |
| `store.install_failed`     | ERROR  | Hook failure, config error, or runtime rejection     |
| `store.update_available`   | INFO   | Update detected for installed plugin                 |
| `store.update_complete`    | INFO   | Plugin updated successfully                          |
| `store.uninstall_complete` | INFO   | Plugin uninstalled and cleaned up                    |
| `store.cache_refreshed`    | DEBUG  | Store cache refresh completed for a source           |
| `store.cache_stale`        | WARN   | Cache TTL expired and source unreachable             |

### 11.2 Example Log Entry

```json
{
  "id": "d4c2f8a1-...",
  "timestamp": "2026-05-08T09:14:32Z",
  "level": "INFO",
  "category": "STORE",
  "source": {
    "user_id": "user-123",
    "session_id": "session-xyz"
  },
  "event": "store.install_complete",
  "payload": {
    "plugin_id": "telegram-channel",
    "version": "1.0.0",
    "source": "official",
    "trust_level": "verified",
    "duration_ms": 1840
  },
  "correlation_id": "req-abc-456",
  "trace_id": "trace-789"
}
```
