-- =============================================================================
-- SYNAPSE — PostgreSQL Schema
-- PostgreSQL 18+  |  UUID generation via gen_random_uuid()
-- All timestamps are stored as TIMESTAMPTZ (UTC)
-- JSONB used for flexible/extensible configuration blobs
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Extensions
-- ---------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid() fallback
CREATE EXTENSION IF NOT EXISTS "pg_trgm";    -- trigram indexes for text search


-- =============================================================================
-- SYSTEM
-- =============================================================================

-- Singleton-style table; application enforces a single row via upsert.
-- Stores platform-wide metadata and global settings blob.
CREATE TABLE IF NOT EXISTS system_metadata (
    id          BOOL        PRIMARY KEY DEFAULT TRUE CHECK (id),
    name        TEXT        NOT NULL,
    version     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    settings    JSONB       NOT NULL DEFAULT '{}'
);

COMMENT ON TABLE system_metadata IS
    'Singleton row holding platform identity, version, and global settings. '
    'The id CHECK constraint allows only one TRUE row.';


-- Append-only audit / event log for the entire platform.
-- Structured logging: level, category, and a free-form payload blob.
CREATE TABLE IF NOT EXISTS system_logs (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp       TIMESTAMPTZ NOT NULL    DEFAULT NOW(),
    level           TEXT        NOT NULL    CHECK (level IN ('DEBUG','INFO','WARN','ERROR','FATAL')),
    category        TEXT        NOT NULL,
    source          JSONB       NOT NULL    DEFAULT '{}',
    event           TEXT        NOT NULL,
    payload         JSONB                   DEFAULT '{}',
    correlation_id  UUID,
    trace_id        UUID
);

COMMENT ON TABLE system_logs IS
    'Append-only, structured platform event log. Never update or delete rows; '
    'use partitioning or archival for long-term retention.';

CREATE INDEX IF NOT EXISTS idx_system_logs_timestamp       ON system_logs (timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_system_logs_level           ON system_logs (level);
CREATE INDEX IF NOT EXISTS idx_system_logs_category        ON system_logs (category);
CREATE INDEX IF NOT EXISTS idx_system_logs_correlation_id  ON system_logs (correlation_id) WHERE correlation_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_system_logs_trace_id        ON system_logs (trace_id)       WHERE trace_id IS NOT NULL;


-- =============================================================================
-- USERS & AUTH
-- (declared before agents so that downstream FKs can reference users.id)
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    username      TEXT        NOT NULL UNIQUE,
    email         TEXT        NOT NULL UNIQUE,
    password_hash TEXT        NOT NULL,
    role          TEXT        NOT NULL CHECK (role IN ('OWNER','ADMIN','USER','VIEWER')),
    settings      JSONB       NOT NULL DEFAULT '{}',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE users IS
    'Platform user accounts. The OWNER role is reserved for the initial admin; '
    'only one OWNER should exist, enforced by application logic.';

CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_role     ON users (role);


CREATE TABLE IF NOT EXISTS sessions_auth (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  TEXT        NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE sessions_auth IS
    'Hashed authentication tokens (e.g. JWT JTI or opaque tokens). '
    'Raw tokens are never stored; only their hash.';

CREATE INDEX IF NOT EXISTS idx_sessions_auth_user_id    ON sessions_auth (user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_auth_token_hash ON sessions_auth (token_hash);
CREATE INDEX IF NOT EXISTS idx_sessions_auth_expires_at ON sessions_auth (expires_at);


-- =============================================================================
-- AGENTS
-- =============================================================================

CREATE TABLE IF NOT EXISTS agents (
    id          TEXT        PRIMARY KEY,
    name        TEXT        NOT NULL,
    type        TEXT        NOT NULL CHECK (type IN ('main','team-member','team-leader','firm-ceo','firm-agent','custom')),
    status      TEXT        NOT NULL DEFAULT 'offline' CHECK (status IN ('online','offline','echo')),
    config      JSONB       NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE agents IS
    'Agent registry. Text PKs are human-readable slugs (e.g. "main-agent"). '
    'The config JSONB holds provider, model, system prompt, tool bindings, etc.';

CREATE INDEX IF NOT EXISTS idx_agents_type   ON agents (type);
CREATE INDEX IF NOT EXISTS idx_agents_status ON agents (status);


CREATE TABLE IF NOT EXISTS agent_teams (
    id              TEXT        PRIMARY KEY,
    name            TEXT        NOT NULL,
    leader_agent_id TEXT        REFERENCES agents (id) ON DELETE SET NULL,
    config          JSONB       NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE agent_teams IS
    'Named collections of agents with an optional designated leader.';

CREATE INDEX IF NOT EXISTS idx_agent_teams_leader ON agent_teams (leader_agent_id) WHERE leader_agent_id IS NOT NULL;


CREATE TABLE IF NOT EXISTS agent_team_members (
    team_id     TEXT    NOT NULL REFERENCES agent_teams (id) ON DELETE CASCADE,
    agent_id    TEXT    NOT NULL REFERENCES agents      (id) ON DELETE CASCADE,
    role        TEXT    NOT NULL DEFAULT 'member',
    PRIMARY KEY (team_id, agent_id)
);

CREATE INDEX IF NOT EXISTS idx_agent_team_members_agent ON agent_team_members (agent_id);


-- Single-row table representing the AI Firm (the top-level autonomous org).
-- A CHECK constraint rejects any INSERT that would add a second row.
CREATE TABLE IF NOT EXISTS ai_firm (
    id          TEXT        PRIMARY KEY,
    name        TEXT        NOT NULL,
    ceo_agent_id TEXT       REFERENCES agents (id) ON DELETE SET NULL,
    config      JSONB       NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- Enforce singleton: id must always be 'firm' (or any fixed constant).
    -- Application code should upsert on this PK rather than INSERT freely.
    CONSTRAINT ai_firm_singleton CHECK (id = 'firm')
);

COMMENT ON TABLE ai_firm IS
    'Singleton row representing the AI Firm entity. '
    'The CONSTRAINT ai_firm_singleton enforces id = ''firm'' so only one row '
    'can ever exist. Use INSERT ... ON CONFLICT (id) DO UPDATE to mutate it.';


-- =============================================================================
-- PLUGINS
-- =============================================================================

CREATE TABLE IF NOT EXISTS plugins (
    id          TEXT        PRIMARY KEY,
    name        TEXT        NOT NULL,
    type        TEXT        NOT NULL CHECK (type IN ('channel','model','skill','mcp')),
    version     TEXT        NOT NULL,
    status      TEXT        NOT NULL DEFAULT 'installed' CHECK (status IN ('installed','disabled','error')),
    manifest    JSONB       NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE plugins IS
    'Plugin registry. Each row represents one installed plugin package. '
    'Manifest holds the raw plugin.json / plugin.yaml content as JSONB.';

CREATE INDEX IF NOT EXISTS idx_plugins_type   ON plugins (type);
CREATE INDEX IF NOT EXISTS idx_plugins_status ON plugins (status);


CREATE TABLE IF NOT EXISTS installed_channels (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id   TEXT    NOT NULL REFERENCES plugins (id) ON DELETE CASCADE,
    user_id     UUID    NOT NULL REFERENCES users   (id) ON DELETE CASCADE,
    config      JSONB   NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_installed_channels_plugin ON installed_channels (plugin_id);
CREATE INDEX IF NOT EXISTS idx_installed_channels_user   ON installed_channels (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_installed_channels_plugin_user ON installed_channels (plugin_id, user_id);


CREATE TABLE IF NOT EXISTS installed_models (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id       TEXT    NOT NULL REFERENCES plugins (id) ON DELETE CASCADE,
    user_id         UUID    NOT NULL REFERENCES users   (id) ON DELETE CASCADE,
    config          JSONB   NOT NULL DEFAULT '{}',
    is_self_hosted  BOOL    NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE installed_models IS
    'Per-user model installations. is_self_hosted=true when the model is '
    'served locally (e.g. Ollama, vLLM) rather than via an external API.';

CREATE INDEX IF NOT EXISTS idx_installed_models_plugin        ON installed_models (plugin_id);
CREATE INDEX IF NOT EXISTS idx_installed_models_user          ON installed_models (user_id);
CREATE INDEX IF NOT EXISTS idx_installed_models_self_hosted   ON installed_models (is_self_hosted) WHERE is_self_hosted = TRUE;
CREATE UNIQUE INDEX IF NOT EXISTS ux_installed_models_plugin_user ON installed_models (plugin_id, user_id);


CREATE TABLE IF NOT EXISTS installed_skills (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id   TEXT    NOT NULL REFERENCES plugins (id) ON DELETE CASCADE,
    agent_id    TEXT    NOT NULL REFERENCES agents  (id) ON DELETE CASCADE,
    config      JSONB   NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_installed_skills_plugin ON installed_skills (plugin_id);
CREATE INDEX IF NOT EXISTS idx_installed_skills_agent  ON installed_skills (agent_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_installed_skills_plugin_agent ON installed_skills (plugin_id, agent_id);


CREATE TABLE IF NOT EXISTS installed_mcp (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id   TEXT    NOT NULL REFERENCES plugins (id) ON DELETE CASCADE,
    agent_id    TEXT    NOT NULL REFERENCES agents  (id) ON DELETE CASCADE,
    config      JSONB   NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE installed_mcp IS
    'Model Context Protocol (MCP) server bindings, one row per agent–MCP pair.';

CREATE INDEX IF NOT EXISTS idx_installed_mcp_plugin ON installed_mcp (plugin_id);
CREATE INDEX IF NOT EXISTS idx_installed_mcp_agent  ON installed_mcp (agent_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_installed_mcp_plugin_agent ON installed_mcp (plugin_id, agent_id);


-- =============================================================================
-- CUSTOM COMMANDS
-- =============================================================================

CREATE TABLE IF NOT EXISTS custom_commands (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    name            TEXT    NOT NULL UNIQUE,
    description     TEXT    NOT NULL DEFAULT '',
    handler_config  JSONB   NOT NULL DEFAULT '{}',
    created_by      UUID    REFERENCES users (id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE custom_commands IS
    'User-defined slash commands / macros. handler_config describes how the '
    'command is executed (e.g. agent call, webhook, script).';

CREATE INDEX IF NOT EXISTS idx_custom_commands_name       ON custom_commands (name);
CREATE INDEX IF NOT EXISTS idx_custom_commands_created_by ON custom_commands (created_by) WHERE created_by IS NOT NULL;


-- =============================================================================
-- CONVERSATIONS
-- =============================================================================

CREATE TABLE IF NOT EXISTS conversations (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id    TEXT    NOT NULL REFERENCES agents (id) ON DELETE RESTRICT,
    user_id     UUID    NOT NULL REFERENCES users  (id) ON DELETE CASCADE,
    channel_id  UUID    REFERENCES installed_channels (id) ON DELETE SET NULL,
    started_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status      TEXT    NOT NULL DEFAULT 'active' CHECK (status IN ('active','closed'))
);

CREATE INDEX IF NOT EXISTS idx_conversations_agent_id  ON conversations (agent_id);
CREATE INDEX IF NOT EXISTS idx_conversations_user_id   ON conversations (user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_channel_id ON conversations (channel_id) WHERE channel_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_conversations_status    ON conversations (status);
CREATE INDEX IF NOT EXISTS idx_conversations_started_at ON conversations (started_at DESC);


CREATE TABLE IF NOT EXISTS messages (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID    NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    role            TEXT    NOT NULL CHECK (role IN ('user','assistant','system')),
    content         TEXT    NOT NULL,
    tokens          INT     CHECK (tokens >= 0),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages (conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at      ON messages (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_role            ON messages (role);


-- =============================================================================
-- TASKS & PROJECTS
-- =============================================================================

CREATE TABLE IF NOT EXISTS projects (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT    NOT NULL,
    status      TEXT    NOT NULL DEFAULT 'active' CHECK (status IN ('active','done','archived')),
    ai_firm_id  TEXT    REFERENCES ai_firm (id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_projects_status     ON projects (status);
CREATE INDEX IF NOT EXISTS idx_projects_ai_firm_id ON projects (ai_firm_id) WHERE ai_firm_id IS NOT NULL;


CREATE TABLE IF NOT EXISTS tasks (
    id                  UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID    NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    title               TEXT    NOT NULL,
    status              TEXT    NOT NULL DEFAULT 'open' CHECK (status IN ('open','in-progress','done','blocked')),
    assigned_agent_id   TEXT    REFERENCES agents (id) ON DELETE SET NULL,
    size                TEXT    CHECK (size IN ('xs','s','m','l','xl')),
    version             INT     NOT NULL DEFAULT 1 CHECK (version >= 1),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tasks IS
    'Work items within a project. version is an optimistic-locking counter '
    'incremented on every update to detect concurrent modifications.';

CREATE INDEX IF NOT EXISTS idx_tasks_project_id        ON tasks (project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status            ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_agent_id ON tasks (assigned_agent_id) WHERE assigned_agent_id IS NOT NULL;


CREATE TABLE IF NOT EXISTS task_logs (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id     UUID    NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    event       TEXT    NOT NULL,
    payload     JSONB   NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_task_logs_task_id    ON task_logs (task_id);
CREATE INDEX IF NOT EXISTS idx_task_logs_created_at ON task_logs (created_at DESC);


-- =============================================================================
-- TRACKING
-- =============================================================================

CREATE TABLE IF NOT EXISTS agent_costs (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id    TEXT        NOT NULL REFERENCES agents (id) ON DELETE CASCADE,
    provider_id TEXT        NOT NULL,
    tokens_in   BIGINT      NOT NULL DEFAULT 0 CHECK (tokens_in  >= 0),
    tokens_out  BIGINT      NOT NULL DEFAULT 0 CHECK (tokens_out >= 0),
    cost        NUMERIC(12,8) NOT NULL DEFAULT 0 CHECK (cost >= 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE agent_costs IS
    'Per-invocation cost records for LLM provider calls. '
    'Aggregate for billing dashboards and budget enforcement.';

CREATE INDEX IF NOT EXISTS idx_agent_costs_agent_id    ON agent_costs (agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_costs_provider_id ON agent_costs (provider_id);
CREATE INDEX IF NOT EXISTS idx_agent_costs_created_at  ON agent_costs (created_at DESC);

CREATE TABLE IF NOT EXISTS sessions (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id        TEXT    NOT NULL REFERENCES agents (id) ON DELETE CASCADE,
    user_id         UUID    NOT NULL REFERENCES users  (id) ON DELETE CASCADE,
    started_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_activity   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status          TEXT    NOT NULL DEFAULT 'active' CHECK (status IN ('active','idle','closed'))
);

CREATE INDEX IF NOT EXISTS idx_sessions_agent_id      ON sessions (agent_id);
CREATE INDEX IF NOT EXISTS idx_sessions_user_id       ON sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status        ON sessions (status);
CREATE INDEX IF NOT EXISTS idx_sessions_last_activity ON sessions (last_activity DESC);

CREATE TABLE IF NOT EXISTS heartbeat_log (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id        TEXT    NOT NULL REFERENCES agents (id) ON DELETE CASCADE,
    session_id      UUID    NOT NULL REFERENCES sessions (id) ON DELETE CASCADE,
    sent_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cache_saved     BOOL    NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE heartbeat_log IS
    'Periodic keep-alive signals from running agents. '
    'cache_saved=true indicates the agent persisted its context cache during this heartbeat.';

CREATE INDEX IF NOT EXISTS idx_heartbeat_log_agent_id   ON heartbeat_log (agent_id);
CREATE INDEX IF NOT EXISTS idx_heartbeat_log_session_id ON heartbeat_log (session_id);
CREATE INDEX IF NOT EXISTS idx_heartbeat_log_sent_at    ON heartbeat_log (sent_at DESC);


-- =============================================================================
-- STORE
-- =============================================================================

CREATE TABLE IF NOT EXISTS store_cache (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    source          TEXT    NOT NULL CHECK (source IN ('official','community','skills_sh','acp')),
    data            JSONB   NOT NULL DEFAULT '{}',
    last_updated    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE store_cache IS
    'Cached plugin catalogue data fetched from remote sources. '
    'Each row represents one full snapshot from a specific source. '
    'Background sync jobs upsert or replace rows by source.';

CREATE INDEX IF NOT EXISTS idx_store_cache_source       ON store_cache (source);
CREATE INDEX IF NOT EXISTS idx_store_cache_last_updated ON store_cache (last_updated DESC);
CREATE UNIQUE INDEX IF NOT EXISTS ux_store_cache_source ON store_cache (source);


CREATE TABLE IF NOT EXISTS plugin_stats (
    plugin_id       TEXT        PRIMARY KEY REFERENCES plugins (id) ON DELETE CASCADE,
    downloads       BIGINT      NOT NULL DEFAULT 0 CHECK (downloads    >= 0),
    stars           BIGINT      NOT NULL DEFAULT 0 CHECK (stars        >= 0),
    bundle_count    INT         NOT NULL DEFAULT 0 CHECK (bundle_count >= 0),
    synced_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE plugin_stats IS
    'Aggregated marketplace statistics for each installed plugin, '
    'synced from the remote store on a schedule.';


-- =============================================================================
-- USER–AGENT & USER–CHANNEL RELATIONSHIPS
-- =============================================================================

CREATE TABLE IF NOT EXISTS user_agents (
    user_id             UUID    NOT NULL REFERENCES users  (id) ON DELETE CASCADE,
    agent_id            TEXT    NOT NULL REFERENCES agents (id) ON DELETE CASCADE,
    permission_level    TEXT    NOT NULL DEFAULT 'view' CHECK (permission_level IN ('owner','edit','view')),
    PRIMARY KEY (user_id, agent_id)
);

CREATE INDEX IF NOT EXISTS idx_user_agents_agent_id ON user_agents (agent_id);


CREATE TABLE IF NOT EXISTS user_channels (
    user_id     UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    channel_id  UUID    NOT NULL REFERENCES installed_channels (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, channel_id)
);

CREATE INDEX IF NOT EXISTS idx_user_channels_channel_id ON user_channels (channel_id);


-- =============================================================================
-- GIT INTEGRATION (optional feature; tables always created, data optional)
-- =============================================================================

CREATE TABLE IF NOT EXISTS git_providers (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    type            TEXT    NOT NULL CHECK (type IN ('gitlab','github','forgejo','gitea')),
    url             TEXT    NOT NULL,
    user_id         UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_encrypted TEXT    NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE git_providers IS
    'Registered Git hosting provider connections. token_encrypted stores the '
    'OAuth / PAT token encrypted at rest via application-layer encryption; '
    'the raw token is never persisted in plaintext.';

CREATE INDEX IF NOT EXISTS idx_git_providers_user_id ON git_providers (user_id);
CREATE INDEX IF NOT EXISTS idx_git_providers_type    ON git_providers (type);


CREATE TABLE IF NOT EXISTS git_repos (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id     UUID    NOT NULL REFERENCES git_providers (id) ON DELETE CASCADE,
    repo_path       TEXT    NOT NULL,
    project_id      UUID    REFERENCES projects (id) ON DELETE SET NULL,
    linked_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_git_repos_provider_id ON git_repos (provider_id);
CREATE INDEX IF NOT EXISTS idx_git_repos_project_id  ON git_repos (project_id) WHERE project_id IS NOT NULL;


-- =============================================================================
-- MODEL PROVIDERS
-- =============================================================================

CREATE TABLE IF NOT EXISTS model_providers (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name              TEXT        NOT NULL,
    type              TEXT        NOT NULL CHECK (type IN ('OLLAMA','OPENAI','ANTHROPIC','OPENAI_COMPATIBLE')),
    config            JSONB       NOT NULL DEFAULT '{}',
    encrypted_secrets TEXT,
    enabled           BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE model_providers IS
    'Model provider configurations for AI/LLM integrations. '
    'Supports Ollama (local), OpenAI, Anthropic, and OpenAI-compatible providers. '
    'API keys stored encrypted in encrypted_secrets field.';

CREATE INDEX IF NOT EXISTS idx_model_providers_type    ON model_providers (type);
CREATE INDEX IF NOT EXISTS idx_model_providers_enabled ON model_providers (enabled);


CREATE TABLE IF NOT EXISTS provider_usage_logs (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id       UUID        NOT NULL,
    provider_name     TEXT        NOT NULL,
    provider_type     TEXT        NOT NULL,
    model             TEXT        NOT NULL,
    prompt_tokens     INTEGER,
    completion_tokens INTEGER,
    total_tokens      INTEGER,
    latency_ms        BIGINT      NOT NULL,
    success           BOOLEAN     NOT NULL,
    error_message     TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE provider_usage_logs IS
    'Usage and performance logs for model provider API calls. '
    'Tracks token consumption, latency, and success/failure for analytics and cost tracking.';

CREATE INDEX IF NOT EXISTS idx_provider_usage_logs_provider_id ON provider_usage_logs (provider_id);
CREATE INDEX IF NOT EXISTS idx_provider_usage_logs_created_at  ON provider_usage_logs (created_at);
CREATE INDEX IF NOT EXISTS idx_provider_usage_logs_success     ON provider_usage_logs (success);


-- =============================================================================
-- End of schema
-- =============================================================================
