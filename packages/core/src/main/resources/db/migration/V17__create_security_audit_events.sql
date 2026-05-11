CREATE TABLE security_audit_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    user_id UUID,
    username VARCHAR(255),
    ip_address VARCHAR(45),
    resource VARCHAR(500),
    action VARCHAR(100),
    result VARCHAR(20) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user_id ON security_audit_events(user_id);
CREATE INDEX idx_audit_event_type ON security_audit_events(event_type);
CREATE INDEX idx_audit_created_at ON security_audit_events(created_at);
CREATE INDEX idx_audit_result ON security_audit_events(result);
