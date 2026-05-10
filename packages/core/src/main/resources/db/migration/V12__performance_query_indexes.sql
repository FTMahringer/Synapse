-- Composite indexes for common sorted and filtered access patterns

CREATE INDEX IF NOT EXISTS idx_conversations_user_started_at
    ON conversations (user_id, started_at DESC);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_created_at
    ON messages (conversation_id, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_tasks_project_created_at
    ON tasks (project_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_model_providers_enabled_type
    ON model_providers (enabled, type);

CREATE INDEX IF NOT EXISTS idx_store_entries_type_source
    ON store_entries (type, source);
