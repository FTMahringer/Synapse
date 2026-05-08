CREATE TABLE firm_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firm_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    dispatched_by_agent_id VARCHAR(255) NOT NULL,
    assigned_team_id VARCHAR(255),
    conversation_id UUID,
    task_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_firm_projects_firm ON firm_projects(firm_id);
CREATE INDEX idx_firm_projects_status ON firm_projects(status);
