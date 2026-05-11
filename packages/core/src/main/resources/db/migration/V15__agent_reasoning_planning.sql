-- Reasoning and planning persistence for token-efficient plan reuse

CREATE TABLE planning_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id VARCHAR(255) NOT NULL,
    collaboration_session_id UUID REFERENCES collaboration_sessions(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    goal_statement TEXT NOT NULL,
    created_by_agent_id VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE planning_artifacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id UUID NOT NULL REFERENCES planning_goals(id) ON DELETE CASCADE,
    plan_version INT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    compact_summary TEXT NOT NULL,
    steps_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    reasoning_chain_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    total_steps INT NOT NULL DEFAULT 0,
    completed_steps INT NOT NULL DEFAULT 0,
    created_by_agent_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(goal_id, plan_version)
);

CREATE INDEX idx_planning_goals_team ON planning_goals(team_id, created_at DESC);
CREATE INDEX idx_planning_goals_status ON planning_goals(status);
CREATE INDEX idx_planning_artifacts_goal ON planning_artifacts(goal_id, created_at DESC);
CREATE INDEX idx_planning_artifacts_status ON planning_artifacts(status);
