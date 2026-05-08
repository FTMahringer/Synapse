CREATE TABLE agent_runtime_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id VARCHAR(255) NOT NULL UNIQUE,
    state VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_activated_at TIMESTAMP,
    last_deactivated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agent_runtime_agent_id ON agent_runtime_registry(agent_id);
CREATE INDEX idx_agent_runtime_state ON agent_runtime_registry(state);
