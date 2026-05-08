CREATE TABLE routing_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL,
    message_id UUID NOT NULL,
    decision VARCHAR(50) NOT NULL,
    target_agent_id VARCHAR(255),
    target_team_id UUID,
    target_project_id UUID,
    reasoning TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_routing_logs_conversation ON routing_logs(conversation_id);
CREATE INDEX idx_routing_logs_message ON routing_logs(message_id);
CREATE INDEX idx_routing_logs_decision ON routing_logs(decision);
