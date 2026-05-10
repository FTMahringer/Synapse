# Integrations

**Status**: Designed (detailed specs in `/docs/`)  
**Target**: v2.5.0 – v2.6.0

---

## MCP Integration

**Spec**: [`/docs/mcp-integration.md`](/docs/mcp-integration.md)  
**Priority**: High

MCP (Model Context Protocol) servers expose external tools to agents via JSON-RPC over stdio or HTTP. Agents declare MCP servers in their `config.yml`; the runtime manages server lifecycle and tool discovery automatically.

### Transports

- **stdio**: Local process, launched by SYNAPSE, communicates over stdin/stdout
- **HTTP**: Remote or local HTTP server with SSE or polling transport

### Key Points

- Tools from MCP servers appear alongside native tools in the agent's context
- Each MCP server is scoped to the agent that declares it
- Server startup/shutdown logged to `MCP` category
- Community MCP plugins installable from the store

---

## ACP Registry

**Spec**: [`/docs/acp-registry.md`](/docs/acp-registry.md)  
**Priority**: High

ACP (Agent Communication Protocol) Registry discovers provider definitions, endpoints, model lists, and credential modes without requiring a local plugin file for standard providers.

### Credential Types

- API key (header or query param)
- OAuth 2.0 client credentials
- Bearer token
- Custom header

### Key Points

- Registry entries fetched and cached on startup
- Allows configuring Claude, OpenAI, Mistral, Ollama, and other providers without manual plugin files
- Custom/self-hosted providers can be registered manually
- Credentials stored encrypted via the secrets management system

---

## Git Provider Integration

**Spec**: [`/docs/git-provider-integration.md`](/docs/git-provider-integration.md)  
**Priority**: Medium

Git provider integration is optional — SYNAPSE runs without it. When configured, agents gain access to repository operations and CI/CD context.

### Providers

- GitHub
- GitLab
- Forgejo / Gitea
- Bitbucket

### Capabilities

- Read repository structure and file contents
- Create/update issues and pull requests
- Trigger and monitor CI/CD pipelines
- Webhook receiver for event-driven agent activation
- Runner integration (see Infrastructure ideas)

---

## Skills Integration

**Spec**: [`/docs/skills-integration.md`](/docs/skills-integration.md)  
**Priority**: High

Skills are reusable agent capabilities using the Claude Code Skills format. They can be installed from the store, loaded manually, or published to skills.sh with explicit user consent.

### Sources

- **Store**: Browse and install from SYNAPSE plugin store
- **Local file**: Drop a `.md` skill file into `plugins/skills/`
- **skills.sh**: Public skills registry; install with `/skills install {id}`

### Lifecycle

1. Skill file placed in `plugins/skills/`
2. Agent declares skill usage in `config.yml`
3. Runtime loads skill on next agent reload
4. Skill appears in agent context as an available capability

### Publishing

Publishing to skills.sh requires explicit user action via `/skills publish {skill-id}`. No automatic publishing path exists. User sees a preview and must confirm before the skill.sh API is called.

### Key Points

- Skills are markdown files — version-controllable, diffable
- Agent self-learning loop can draft new skills from detected patterns (user approval required)
- skills.sh integration requires an API token in System Settings → Integrations
