CREATE TABLE agent_memory_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id VARCHAR(255) NOT NULL,
    memory_key VARCHAR(500) NOT NULL,
    value TEXT NOT NULL,
    namespace VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(agent_id, memory_key)
);

CREATE INDEX idx_agent_memory_agent ON agent_memory_entries(agent_id);
CREATE INDEX idx_agent_memory_namespace ON agent_memory_entries(agent_id, namespace);
