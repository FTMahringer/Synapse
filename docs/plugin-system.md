# Plugin System

## Overview

The SYNAPSE plugin system is the primary extension mechanism for {SYSTEM_NAME}. Every external connection — whether to a messaging platform, an AI model provider, a callable tool, or an external context server — is a plugin. Plugins are self-contained: they ship their own manifest, their own configuration schema, and their own lifecycle hooks. The core runtime never needs to be modified to add new capabilities.

Plugins are discovered by scanning the `plugins/` directory on startup, or on demand when the operator runs `synapse plugins reload` or triggers "Load from Disk" in the Dashboard. Once discovered, a plugin moves through a defined lifecycle from install to active use to uninstall.

---

## Plugin Types

### channel

A **channel plugin** connects an external communication platform to the SYNAPSE message bus. It is responsible for:

- Listening for inbound events from the platform (webhook, long-polling, WebSocket, etc.)
- Normalising each inbound event to a `ChannelEvent` object and publishing it to the `stream:channel.inbound` Redis stream
- Receiving outbound `ChannelResponse` objects from the message bus and formatting and delivering them to the platform

Examples: Telegram, Discord, Slack, XMPP, Matrix, email (IMAP/SMTP), custom webhook receiver.

A channel plugin fires `on_message` when an inbound event arrives and `on_send` before an outbound message is delivered.

### model

A **model plugin** wraps an AI model provider or a locally-running model server. It is responsible for:

- Accepting a structured `ModelRequest` (system prompt, messages, parameters) from the agent runtime
- Forwarding the request to the provider's API or local endpoint
- Streaming or returning the completion as a `ModelResponse`

Examples: OpenAI, Anthropic, Mistral, Ollama, LM Studio, vLLM, any OpenAI-compatible endpoint.

A model plugin fires `on_request` before sending to the provider and `on_response` when the provider reply is received.

### skill

A **skill plugin** exposes a callable capability that agents can invoke during a task. Skills are analogous to tools in the function-calling sense. The plugin registers one or more skill definitions, each with a name, description, and input schema. The agent runtime calls the skill by name and passes a typed argument object; the plugin executes the capability and returns a result.

Examples: web search, code execution sandbox, file read/write, HTTP fetch, calendar access, database query, image generation, PDF parse.

A skill plugin fires `on_start` when loaded and `on_stop` when deactivated. Individual skill calls do not have their own hooks — they are plain synchronous or async function calls.

### mcp

An **MCP plugin** integrates a Model Context Protocol server into {SYSTEM_NAME}. It wraps an MCP server process or network endpoint and exposes the tools, resources, and prompts that server provides as native SYNAPSE capabilities. Agents can reference MCP servers in their `config.yml` by name. The MCP plugin manages the connection lifecycle (start process, maintain session, restart on crash) and translates between the MCP wire protocol and the SYNAPSE internal call format.

Examples: filesystem MCP server, browser MCP server, database MCP server, custom internal tools exposed via MCP.

An MCP plugin fires `on_start` when the MCP server process is launched and `on_stop` when it is shut down.

---

## manifest.yml Specification

Every plugin must ship a `manifest.yml` at the root of its plugin directory. This file is the authoritative description of the plugin and is the only file the Plugin Manager reads during discovery and installation.

```yaml
# ── Identity ────────────────────────────────────────────────────────────────

id: "com.example.myplugin"
# Required. Reverse-domain namespaced unique identifier.
# Must be globally unique within a {SYSTEM_NAME} instance.
# Allowed characters: [a-z0-9.-]  Max length: 128

name: "My Plugin"
# Required. Human-readable display name shown in Dashboard and CLI.

version: "1.2.0"
# Required. Semantic version (MAJOR.MINOR.PATCH).
# PATCH: backwards-compatible bug fix
# MINOR: new backwards-compatible capability
# MAJOR: breaking change

description: "A short sentence describing what this plugin does."
# Required. Shown in plugin list and store browser. Max 256 chars.

author: "Author Name <author@example.com>"
# Required. Used for attribution and support routing.

homepage: "https://example.com/myplugin"
# Optional. URL to documentation or source repository.

license: "MIT"
# Optional. SPDX license identifier.

# ── Type ────────────────────────────────────────────────────────────────────

type: channel
# Required. One of: channel | model | skill | mcp
# Determines which hooks are called and which runtime API is available.

# ── Compatibility ───────────────────────────────────────────────────────────

synapse_version: ">=1.0.0 <2.0.0"
# Required. Semver range of SYNAPSE core versions this plugin is compatible with.
# The Plugin Manager rejects installation if the running version is outside this range.

# ── Entry Point ─────────────────────────────────────────────────────────────

entrypoint: "main.py"
# Required. Relative path (from manifest.yml) to the file that is executed
# when the plugin is started. Supported runtimes are determined by file extension:
#   .py  → Python 3.11+
#   .js  → Node.js 20+
#   .ts  → Deno or ts-node (configured per instance)
#   .jar → JVM (loaded by the backend classloader)
#   .so  → Native shared library (advanced; requires explicit permission)
# For mcp type, this is the command to launch the MCP server process.

runtime: "python3.11"
# Optional. Overrides runtime auto-detection. Allowed values depend on
# the runtimes configured in {SYSTEM_NAME}'s system settings.

# ── Permissions ─────────────────────────────────────────────────────────────

permissions:
  - network.outbound
  # Plugin may make outbound HTTP/TCP connections.

  - network.inbound
  # Plugin may open a listening port (e.g. webhook receiver).

  - filesystem.read
  # Plugin may read files from the data directory and plugin directory.

  - filesystem.write
  # Plugin may write files to the plugin's data directory.

  - database.read
  # Plugin may execute SELECT queries against the synapse schema.

  - database.write
  # Plugin may INSERT/UPDATE/DELETE in its own plugin-namespaced tables.

  - agents.read
  # Plugin may query agent identities and status.

  - agents.message
  # Plugin may inject messages into the agent message bus.

  - system.config.read
  # Plugin may read non-secret system configuration values.

# The operator must accept all listed permissions at install time.
# Permissions not declared here cannot be used at runtime.

# ── Dependencies ─────────────────────────────────────────────────────────────

dependencies:
  plugins:
    - id: "com.example.base-plugin"
      version: ">=2.0.0"
      # Optional list of other plugins that must be installed and active
      # before this plugin can be activated.

  system:
    - redis
    - postgres
    # Optional list of system services this plugin requires.
    # Allowed values: redis | postgres | filesystem

# ── Configuration Schema ─────────────────────────────────────────────────────

config:
  - key: api_url
    type: string
    label: "API Base URL"
    description: "Base URL of the remote service, without trailing slash."
    required: true
    default: "https://api.example.com"
    secret: false
    # type: string — free text; stored as-is

  - key: api_key
    type: string
    label: "API Key"
    description: "Authentication key for the remote service."
    required: true
    secret: true
    # secret: true — value is encrypted at rest; never returned in API responses

  - key: max_retries
    type: integer
    label: "Maximum Retries"
    description: "Number of times to retry a failed request before giving up."
    required: false
    default: 3
    min: 0
    max: 10
    # type: integer — validated as whole number; min/max optional

  - key: enable_debug
    type: boolean
    label: "Enable Debug Logging"
    description: "Emit verbose debug events to the SYNAPSE event log."
    required: false
    default: false
    # type: boolean — rendered as toggle; stored as true/false

  - key: allowed_channels
    type: list
    label: "Allowed Channels"
    description: "List of channel IDs this plugin responds to. Empty = all channels."
    required: false
    default: []
    item_type: string
    # type: list — each element is validated against item_type
    # item_type: string | integer | boolean

# ── Tags ────────────────────────────────────────────────────────────────────

tags:
  - messaging
  - official
# Optional. Used for filtering in the plugin store and Dashboard.

# ── Icon ────────────────────────────────────────────────────────────────────

icon: "icon.png"
# Optional. Relative path to a PNG icon (min 64×64, max 512×512).
# Displayed in Dashboard and CLI plugin list.

# ── Changelog ───────────────────────────────────────────────────────────────

changelog: "CHANGELOG.md"
# Optional. Relative path to a changelog file shown in the Dashboard detail view.
```

---

## Plugin Lifecycle

```
DISCOVERY
  {SYSTEM_NAME} scans plugins/ directory on startup or on reload command.
  For each subdirectory, manifest.yml is read and parsed.
  Invalid manifests are logged as PLUGIN:DISCOVERY_ERROR and skipped.
  Valid manifests are registered as discovered plugins (status = discovered).
      │
      ▼
INSTALL
  Triggered by: Main Agent command, Dashboard button, CLI, or POST /api/plugins/install.
  Steps:
    1. Fetch plugin artefacts (if remote source) or verify local files.
    2. Validate manifest against installed SYNAPSE version (synapse_version range).
    3. Check declared permissions — operator must accept.
    4. Check plugin dependencies are satisfied.
    5. Copy artefacts to plugins/{id}/ directory.
    6. Execute on_install hook.
    7. Create DB record (status = installed).
    8. Log PLUGIN:INSTALL event.
      │
      ▼
CONFIGURE
  Triggered automatically after install if required config fields are present,
  or on operator demand via Dashboard → Configure / CLI `synapse plugins configure <id>`.
  Steps:
    1. Present config schema to operator (Dashboard form or CLI wizard).
    2. Operator fills required fields; optional fields use defaults.
    3. Secret values are encrypted with AES-256-GCM before storage.
    4. Config saved to DB. Status remains installed.
      │
      ▼
ACTIVATE
  Triggered by: operator toggling plugin ON, or auto-activate if configured.
  Steps:
    1. Verify all required config fields are present.
    2. Execute on_start hook — plugin initialises connections, opens ports, etc.
    3. Update DB record (status = active).
    4. Log PLUGIN:ENABLE event.
      │
      ▼
USE
  Active plugin receives hook calls from the runtime:
    channel  → on_message (inbound), on_send (outbound)
    model    → on_request (before provider call), on_response (after provider call)
    skill    → direct function call (no hook prefix; logged per call)
    mcp      → proxied tool/resource/prompt calls through MCP session
      │
      ▼
DEACTIVATE (graceful)
  Triggered by: operator toggling plugin OFF, or system shutdown.
  Steps:
    1. Execute on_stop hook — plugin closes connections, flushes buffers, etc.
    2. Update DB record (status = installed).
    3. Log PLUGIN:DISABLE event.
      │
      ▼
UNINSTALL
  Triggered by: Main Agent command, Dashboard button, CLI, or DELETE /api/plugins/:id.
  Steps:
    1. Deactivate if currently active (on_stop hook).
    2. Execute on_uninstall hook — plugin cleans up any external registrations.
    3. Remove plugin artefacts from plugins/{id}/ directory.
    4. Delete DB record and associated config values.
    5. Log PLUGIN:UNINSTALL event.
```

---

## Hook System

Hooks are called by the SYNAPSE runtime at defined points in the message and lifecycle flow. A plugin implements hooks by exporting the corresponding function from its entrypoint file.

| Hook | Plugin Types | When It Fires | Blocking |
|------|-------------|---------------|----------|
| `on_install` | all | After artefacts are copied to disk, before DB record is created | Yes — failure aborts install |
| `on_uninstall` | all | Before artefacts are deleted from disk | Yes — failure is logged but does not block uninstall |
| `on_start` | all | When plugin is activated | Yes — failure rolls back to installed status |
| `on_stop` | all | When plugin is deactivated or system shuts down | Yes — timeout of 10 s then forced |
| `on_message` | channel | When an inbound event arrives from the external platform | Yes — hook must return a normalised ChannelEvent or null to drop |
| `on_send` | channel | Before an outbound message is delivered to the platform | Yes — hook may modify or suppress the message |
| `on_request` | model | Before the prompt is sent to the model provider | Yes — hook may modify the request (e.g. add system instructions) |
| `on_response` | model | After the provider returns a completion | Yes — hook may modify the response before it reaches the agent |

Hooks must return within their configured timeout (default 30 s for lifecycle hooks, 5 s for message-path hooks). A hook that times out is interrupted and a PLUGIN:HOOK_TIMEOUT event is logged.

Hook function signatures (Python example):

```python
def on_install(context: InstallContext) -> None:
    # context.config      — dict of configured values (empty at install time)
    # context.data_dir    — pathlib.Path to plugin's writable data directory
    # context.logger      — structured logger (emits PLUGIN category events)
    pass

def on_start(context: RuntimeContext) -> None:
    # context.config      — dict of configured values
    # context.data_dir    — pathlib.Path to plugin's writable data directory
    # context.logger      — structured logger
    # context.bus         — message bus client (for channel plugins)
    pass

def on_message(event: ChannelEvent, context: RuntimeContext) -> ChannelEvent | None:
    # Return modified event, original event, or None to drop.
    return event

def on_send(response: ChannelResponse, context: RuntimeContext) -> ChannelResponse | None:
    # Return modified response or None to suppress delivery.
    return response

def on_request(request: ModelRequest, context: RuntimeContext) -> ModelRequest:
    return request

def on_response(response: ModelResponse, context: RuntimeContext) -> ModelResponse:
    return response

def on_stop(context: RuntimeContext) -> None:
    pass

def on_uninstall(context: InstallContext) -> None:
    pass
```

---

## Two Creation Paths

### Path 1: Via Main Agent (Guided)

The Main Agent guides the operator through discovering, installing, and configuring a plugin using natural language commands.

**Command forms accepted:**
- `/install telegram` — install by name from the plugin registry
- `/install https://github.com/example/synapse-telegram` — install from a Git URL
- `/install /path/to/local/plugin` — install from a local directory
- `"Install the Telegram channel plugin"` — natural language form

**Interaction flow:**

```
Operator: /install telegram

{SYSTEM_NAME}: I found the Telegram Channel plugin (v2.3.1) by SYNAPSE Official.
Description: Connects Telegram bots to {SYSTEM_NAME} via the Bot API.
Required permissions: network.outbound, network.inbound

This plugin requires the following configuration:
  • bot_token (required, secret): Telegram Bot API token from @BotFather
  • webhook_url (required): Public HTTPS URL for webhook delivery
  • allowed_user_ids (optional): List of Telegram user IDs allowed to chat

Do you want to install? [yes / no]

Operator: yes

{SYSTEM_NAME}: Downloading... done.
Running on_install hook... done.
Plugin installed. Let me walk you through configuration.

Enter your Telegram Bot API token (from @BotFather):
Operator: 123456789:ABCdef...

Enter your public webhook URL:
Operator: https://synapse.example.com/webhook/telegram

Allowed user IDs (comma-separated, or leave blank for all):
Operator: [blank]

{SYSTEM_NAME}: Configuration saved. Activating plugin...
on_start hook: webhook registered at https://synapse.example.com/webhook/telegram
✓ Telegram Channel plugin is now active.
```

### Path 2: Manual Installation

For operators who prefer direct file control or are developing a new plugin.

**Steps:**

1. Create (or copy) the plugin directory into `plugins/`:

   ```
   plugins/
   └── com.example.myplugin/
       ├── manifest.yml
       ├── main.py          (or main.js, plugin.jar, etc.)
       ├── icon.png         (optional)
       └── CHANGELOG.md     (optional)
   ```

2. Write a valid `manifest.yml` following the specification above.

3. Implement the entrypoint file with the required hooks.

4. Trigger reload using one of:
   - CLI: `synapse plugins reload`
   - Dashboard: Plugins → "Load from Disk" button
   - API: `POST /api/plugins/reload`

5. The Plugin Manager scans the directory, discovers the new manifest, and presents the plugin in the "Discovered" state in the Dashboard and CLI.

6. Configure the plugin through the Dashboard (Plugins → select plugin → Configure) or via:

   ```bash
   synapse plugins configure com.example.myplugin
   ```

7. Activate the plugin through the Dashboard toggle or via:

   ```bash
   synapse plugins enable com.example.myplugin
   ```

---

## Config Schema Field Types

| Type | Description | Extra Fields |
|------|-------------|--------------|
| `string` | Free-text value. Stored as UTF-8 string. | `min_length`, `max_length`, `pattern` (regex) |
| `integer` | Whole number. | `min`, `max` |
| `boolean` | True or false. Rendered as toggle in Dashboard. | — |
| `list` | Ordered list of values. Each element validated against `item_type`. | `item_type` (string / integer / boolean), `min_items`, `max_items` |

All fields support:

| Attribute | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | string | Yes | Internal identifier. Used as config dict key in hooks. `[a-z0-9_]` only. |
| `type` | string | Yes | One of: string, integer, boolean, list |
| `label` | string | Yes | Human-readable name shown in Dashboard and CLI wizard. |
| `description` | string | No | Help text shown below the input field. |
| `required` | boolean | Yes | If true, plugin cannot be activated without this value. |
| `default` | any | No | Used if the field is not filled. Must match `type`. |
| `secret` | boolean | No | If true, value is encrypted at rest and masked in API responses. Default: false. |

---

## Plugin Security

### Permission Model

Plugins operate under a least-privilege model. Each permission must be declared in `manifest.yml` and accepted by the operator at install time. The runtime enforces permissions at the API boundary:

- A plugin calling `context.bus.publish()` without `agents.message` permission receives a `PermissionDeniedError`.
- A plugin opening a socket without `network.outbound` permission has the call intercepted and blocked.
- Violations are logged as `PLUGIN:SECURITY_VIOLATION` with the plugin ID, attempted permission, and call stack.

### Data Isolation

Each plugin has an isolated data directory at `plugins/{id}/data/`. Plugins may only read and write within this directory (and their own plugin directory) unless `filesystem.read` / `filesystem.write` permissions grant broader access. Plugins with `database.write` permission may only write to tables prefixed with their plugin ID (e.g. `plugin_com_example_myplugin_*`).

### Secret Values

Configuration fields with `secret: true` are encrypted with AES-256-GCM before being persisted to PostgreSQL. The encryption key is derived from the instance master secret via HKDF-SHA256. The plaintext value is held in memory only during plugin execution and is never written to logs, returned via the API, or included in backups in plaintext.

---

## Versioning

Plugins use **Semantic Versioning** (semver 2.0.0):

- `PATCH` increment: bug fix, no behaviour change visible to the operator or agents.
- `MINOR` increment: new capability added, existing config schema is backward-compatible.
- `MAJOR` increment: breaking change to behaviour or config schema. Operators must review release notes before upgrading.

The `synapse_version` field in `manifest.yml` defines a semver range using npm-style syntax:

| Range | Meaning |
|-------|---------|
| `>=1.0.0 <2.0.0` | Any 1.x.x release |
| `^1.2.0` | >=1.2.0 <2.0.0 |
| `~1.2.0` | >=1.2.0 <1.3.0 |
| `1.5.3` | Exact version only |

When a SYNAPSE update is installed, incompatible plugins (outside the new version's supported range) are automatically deactivated and flagged for update. They are not uninstalled — the operator can update or remove them.

---

## Logging

Plugins emit structured events to the SYNAPSE event log under the `PLUGIN` category. The following events are defined:

| Event | Level | Description |
|-------|-------|-------------|
| `PLUGIN:INSTALL` | INFO | Plugin successfully installed. Fields: id, name, version, source. |
| `PLUGIN:INSTALL_ERROR` | ERROR | Install failed. Fields: id, reason, step (download/hook/db). |
| `PLUGIN:CONFIGURE` | INFO | Plugin configuration updated. Fields: id, changed_keys (secret values redacted). |
| `PLUGIN:ENABLE` | INFO | Plugin activated (on_start completed). Fields: id. |
| `PLUGIN:DISABLE` | INFO | Plugin deactivated (on_stop completed). Fields: id. |
| `PLUGIN:UNINSTALL` | INFO | Plugin uninstalled and artefacts removed. Fields: id. |
| `PLUGIN:DISCOVERY_ERROR` | WARN | manifest.yml invalid during scan. Fields: path, reason. |
| `PLUGIN:HOOK_TIMEOUT` | WARN | Hook did not return within timeout. Fields: id, hook, timeout_ms. |
| `PLUGIN:HOOK_ERROR` | ERROR | Hook raised an unhandled exception. Fields: id, hook, error. |
| `PLUGIN:SECURITY_VIOLATION` | ERROR | Plugin attempted to use an undeclared permission. Fields: id, permission, call. |
| `PLUGIN:UPDATE` | INFO | Plugin version updated. Fields: id, from_version, to_version. |

Plugins may emit their own events using `context.logger.info("MESSAGE", category="PLUGIN", **extra_fields)`. Custom events are stored in the event log alongside system events.

---

## REST API

### List Installed Plugins

```
GET /api/plugins
Authorization: Bearer <token>

Response 200:
[
  {
    "id": "com.example.myplugin",
    "name": "My Plugin",
    "version": "1.2.0",
    "type": "channel",
    "status": "active",
    "description": "...",
    "author": "...",
    "installed_at": "2026-01-15T10:30:00Z"
  },
  ...
]
```

### Install a Plugin

```
POST /api/plugins/install
Authorization: Bearer <token>
Content-Type: application/json

{
  "source": "https://github.com/example/synapse-myplugin",
  // OR
  "source": "local:/plugins/com.example.myplugin",
  // OR
  "source": "registry:com.example.myplugin@1.2.0"
}

Response 201:
{
  "id": "com.example.myplugin",
  "status": "installed",
  "config_schema": [ ... ]   // array of config field specs
}
```

### Configure a Plugin

```
POST /api/plugins/com.example.myplugin/configure
Authorization: Bearer <token>
Content-Type: application/json

{
  "api_url": "https://api.example.com",
  "api_key": "secret-value",
  "max_retries": 5
}

Response 200:
{
  "id": "com.example.myplugin",
  "status": "installed",
  "config": {
    "api_url": "https://api.example.com",
    "api_key": "***",          // secret masked
    "max_retries": 5
  }
}
```

### Activate / Deactivate a Plugin

```
POST /api/plugins/com.example.myplugin/enable
POST /api/plugins/com.example.myplugin/disable
Authorization: Bearer <token>

Response 200:
{
  "id": "com.example.myplugin",
  "status": "active"   // or "installed"
}
```

### Uninstall a Plugin

```
DELETE /api/plugins/com.example.myplugin
Authorization: Bearer <token>

Response 204: No Content
```

### Reload Plugins from Disk

```
POST /api/plugins/reload
Authorization: Bearer <token>

Response 200:
{
  "discovered": 3,
  "newly_found": ["com.example.newplugin"],
  "errors": []
}
```
