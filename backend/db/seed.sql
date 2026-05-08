-- =============================================================================
-- SYNAPSE — Initial Seed Data
-- This file inserts the minimum required rows to make the platform operational.
-- It is NOT a test fixture. Do not add test or demo data here.
-- Run once against a fresh schema; re-running is idempotent via ON CONFLICT.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- System metadata (singleton row)
-- ---------------------------------------------------------------------------
INSERT INTO system_metadata (name, version, created_at, settings)
VALUES (
    'SYNAPSE',
    '0.1.0',
    NOW(),
    '{}'
);

-- If schema is re-applied, the table is truncated by schema.sql not existing
-- yet; if this seed is run a second time, a unique violation would occur.
-- Wrap in a DO block so it is safe to run idempotently.
-- (system_metadata has no PK; we guard with a NOT EXISTS check.)
-- NOTE: The plain INSERT above is intentional for a first-time run.
--       Comment it out and use the block below for re-entrant execution:
--
-- DO $$
-- BEGIN
--     IF NOT EXISTS (SELECT 1 FROM system_metadata WHERE name = 'SYNAPSE') THEN
--         INSERT INTO system_metadata (name, version, created_at, settings)
--         VALUES ('SYNAPSE', '0.1.0', NOW(), '{}');
--     END IF;
-- END $$;


-- ---------------------------------------------------------------------------
-- Default OWNER user
-- ---------------------------------------------------------------------------
-- The password is intentionally NOT seeded here. The application boot process
-- must prompt the administrator to set a password on first login, or the
-- deployment tool injects a hashed credential via environment variable.
INSERT INTO users (id, username, email, role, settings, created_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@localhost',
    'OWNER',
    '{
        "theme": "dark",
        "dashboard_layout": []
    }',
    NOW()
)
ON CONFLICT (username) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Built-in agents
-- ---------------------------------------------------------------------------

-- Main Agent: the primary conversational agent, backed by Anthropic Claude.
-- Status starts as 'offline'; the runtime sets it to 'online' on activation.
INSERT INTO agents (id, name, type, status, config, created_at)
VALUES (
    'main-agent',
    'Main Agent',
    'main',
    'offline',
    '{
        "provider": "anthropic",
        "model": "claude-sonnet-4-6",
        "system_prompt": "You are the Main Agent of this platform. You assist the user with tasks, coordinate with other agents, and manage conversations across channels.",
        "temperature": 0.7,
        "max_tokens": 8192,
        "tools_enabled": true,
        "memory": {
            "vault_enabled": true,
            "compression_threshold_tokens": 40000
        },
        "capabilities": [
            "conversation",
            "task_management",
            "agent_coordination",
            "file_access"
        ]
    }',
    NOW()
)
ON CONFLICT (id) DO NOTHING;


-- ECHO Agent: a lightweight local agent used for offline/fallback scenarios.
-- Backed by Ollama running phi3-mini; no external API calls required.
INSERT INTO agents (id, name, type, status, config, created_at)
VALUES (
    'echo-agent',
    'ECHO',
    'custom',
    'offline',
    '{
        "provider": "ollama",
        "model": "phi3-mini",
        "base_url": "http://localhost:11434",
        "system_prompt": "You are ECHO, a lightweight local agent. You operate entirely on-device with no external API calls. You handle simple queries, echoing and summarising information when the primary agent is unavailable.",
        "temperature": 0.5,
        "max_tokens": 2048,
        "tools_enabled": false,
        "memory": {
            "vault_enabled": false
        },
        "capabilities": [
            "conversation",
            "echo"
        ],
        "notes": "Requires Ollama running locally with the phi3-mini model pulled."
    }',
    NOW()
)
ON CONFLICT (id) DO NOTHING;


-- =============================================================================
-- End of seed data
-- =============================================================================
