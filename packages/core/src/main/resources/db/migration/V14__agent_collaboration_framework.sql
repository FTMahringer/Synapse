-- Agent collaboration framework persistence

CREATE TABLE collaboration_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id VARCHAR(255) NOT NULL,
    initiated_by_agent_id VARCHAR(255) NOT NULL,
    conversation_id UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE collaboration_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES collaboration_sessions(id) ON DELETE CASCADE,
    from_agent_id VARCHAR(255) NOT NULL,
    to_agent_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE collaboration_delegations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES collaboration_sessions(id) ON DELETE CASCADE,
    task_id UUID,
    from_agent_id VARCHAR(255) NOT NULL,
    to_agent_id VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    delegation_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE collaboration_shared_context (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES collaboration_sessions(id) ON DELETE CASCADE,
    context_key VARCHAR(255) NOT NULL,
    context_value TEXT NOT NULL,
    updated_by_agent_id VARCHAR(255) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(session_id, context_key)
);

CREATE INDEX idx_collaboration_sessions_team ON collaboration_sessions(team_id);
CREATE INDEX idx_collaboration_messages_session ON collaboration_messages(session_id, created_at DESC);
CREATE INDEX idx_collaboration_delegations_session ON collaboration_delegations(session_id, created_at DESC);
CREATE INDEX idx_collaboration_context_session ON collaboration_shared_context(session_id, updated_at DESC);
