CREATE TABLE agent_heartbeats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id VARCHAR(255) NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    note VARCHAR(500)
);

CREATE INDEX idx_agent_heartbeats_agent ON agent_heartbeats(agent_id);
CREATE INDEX idx_agent_heartbeats_recorded_at ON agent_heartbeats(recorded_at DESC);
