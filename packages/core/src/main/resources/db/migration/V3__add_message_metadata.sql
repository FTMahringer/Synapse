-- Add metadata columns to messages table for provider tracking

ALTER TABLE messages ADD COLUMN IF NOT EXISTS provider_id UUID REFERENCES model_providers(id) ON DELETE SET NULL;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS model_name TEXT;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS latency_ms BIGINT;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS prompt_tokens INT;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS completion_tokens INT;

CREATE INDEX IF NOT EXISTS idx_messages_provider_id ON messages (provider_id) WHERE provider_id IS NOT NULL;

COMMENT ON COLUMN messages.provider_id IS 'Model provider used to generate this message (NULL for user messages)';
COMMENT ON COLUMN messages.model_name IS 'Specific model name used (e.g., llama3.2, gpt-4)';
COMMENT ON COLUMN messages.latency_ms IS 'Response latency in milliseconds';
COMMENT ON COLUMN messages.prompt_tokens IS 'Number of tokens in the prompt';
COMMENT ON COLUMN messages.completion_tokens IS 'Number of tokens in the completion';
