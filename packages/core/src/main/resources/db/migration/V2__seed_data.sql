-- =============================================================================
-- SYNAPSE — Initial Seed Data
-- This file inserts the minimum required rows to make the platform operational.
-- It is NOT a test fixture. Do not add test or demo data here.
-- Run once against a fresh schema; re-running is idempotent via ON CONFLICT.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- System metadata (singleton row)
-- ---------------------------------------------------------------------------
INSERT INTO system_metadata (id, name, version, created_at, updated_at, settings)
VALUES (
    TRUE,
    'SYNAPSE',
    '0.2.0',
    NOW(),
    NOW(),
    '{
        "system_name": "{SYSTEM_NAME}",
        "default_locale": "en",
        "logging": {
            "storage": "postgresql",
            "delivery": ["websocket", "sse"],
            "stream": "redis"
        },
        "memory": {
            "vault_enabled": true,
            "compression_provider": "ollama",
            "compression_model": "llama3.2",
            "compression_threshold_tokens": 40000
        },
        "echo": {
            "enabled": true,
            "activation": "manual",
            "debug_only": true
        },
        "store": {
            "official_enabled": true,
            "community_enabled": true
        }
    }'
)
ON CONFLICT (id) DO UPDATE SET
    version = EXCLUDED.version,
    updated_at = NOW(),
    settings = system_metadata.settings || EXCLUDED.settings;


-- ---------------------------------------------------------------------------
-- Default OWNER user
-- ---------------------------------------------------------------------------
-- Default password: "admin" (MUST be changed on first login)
-- Password hash generated with Argon2id: $argon2id$v=19$m=65536,t=3,p=1$...
INSERT INTO users (id, username, email, password_hash, role, settings, created_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@localhost',
    '$argon2id$v=19$m=65536,t=3,p=1$WGFiY2RlZmdoaWprbG1ubw$7YJ8Kx8xZ3q5ZSQGXY/nHh7BZ2vGx3c8nQx8pF7DxYo',
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


-- ECHO Agent: a lightweight local debug agent invoked manually by the user.
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
        "system_prompt": "You are ECHO, a lightweight local debug agent. You operate entirely on-device with no external API calls. You only run when the user explicitly invokes the /echo command. You diagnose local platform state, summarise visible context, and avoid acting as an automatic fallback assistant.",
        "temperature": 0.5,
        "max_tokens": 2048,
        "tools_enabled": false,
        "activation": "manual",
        "debug_only": true,
        "memory": {
            "vault_enabled": false
        },
        "capabilities": [
            "diagnostics",
            "summarisation",
            "local_context_echo"
        ],
        "notes": "Requires Ollama running locally with the phi3-mini model pulled."
    }',
    NOW()
)
ON CONFLICT (id) DO NOTHING;


-- =============================================================================
-- End of seed data
-- =============================================================================
