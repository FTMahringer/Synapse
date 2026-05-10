# Platform Features

**Status**: Designed (detailed specs in `/docs/`)  
**Target**: v2.7.0 – v2.10.0

---

## Multi-User System

**Spec**: [`/docs/multi-user.md`](/docs/multi-user.md)  
**Priority**: High

Multiple users with role-based access and per-user resources. Each user gets their own agents, vaults, and conversation history, isolated from other users.

### Roles

| Role | Capabilities |
|---|---|
| `admin` | Full access; manage users, system settings, all agents |
| `user` | Own agents, conversations, vaults |
| `viewer` | Read-only access to shared agents |

### Key Points

- Users cannot read other users' agent vaults without explicit permission
- Admins can access any vault (all admin reads logged)
- Per-user API key management for model providers
- User creation via admin panel or self-registration (configurable)
- OAuth / SSO support planned

---

## Dashboard Theming

**Spec**: [`/docs/dashboard-theming.md`](/docs/dashboard-theming.md)  
**Priority**: Low

The dashboard supports three theming levels:

1. **User overrides** — CSS custom properties set per-user in profile settings
2. **Dashboard block layout** — Rearrange dashboard panels per user preference
3. **Community themes** — Installable themes from the plugin store

Themes are CSS files following the SYNAPSE theming API (custom property overrides). Theme authors can publish to the store.

---

## Custom Commands

**Spec**: [`/docs/custom-commands.md`](/docs/custom-commands.md)  
**Priority**: Medium

User-defined slash commands stored as YAML definitions, executed by the Main Agent. Custom commands allow users to create repeatable workflows accessible via `/command-name`.

### Format

```yaml
id: my-command
name: My Command
description: What this command does
trigger: /my-command
instructions: |
  Step-by-step instructions for the Main Agent to execute
  when this command is triggered.
```

### Key Points

- Commands stored in `commands/` directory
- Hot-reloaded without restart
- Can accept arguments (positional or named)
- Built-in commands (e.g., `/agents new`, `/teams new`) take precedence
- User-scoped vs system-scoped commands

---

## SYNAPSE Store

**Spec**: [`/docs/store-concept.md`](/docs/store-concept.md)  
**Priority**: Medium  
**Target**: v2.6.0

Central marketplace for discovering, installing, and managing all SYNAPSE extensions. Unified interface across all extension types.

### Extension Types

- Plugins (tool providers, channel connectors, model providers)
- Bundles (curated plugin collections)
- Skills (reusable agent capabilities)
- MCP servers
- Themes
- Community agent templates

### Sources

- **Official**: Curated by the SYNAPSE team
- **Community**: Community-contributed, reviewed
- **Local**: Installed from local file path
- **URL**: Direct URL install

### Key Points

- Single install command: `synapse install {plugin-id}` or via dashboard
- Dependency resolution between plugins
- Version pinning and update management
- Rating and review system (planned)
- Plugin health checks after install
