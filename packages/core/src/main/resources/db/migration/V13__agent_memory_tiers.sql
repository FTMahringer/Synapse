-- Add three-tier memory lifecycle metadata

ALTER TABLE agent_memory_entries
    ADD COLUMN IF NOT EXISTS tier VARCHAR(32) NOT NULL DEFAULT 'SHORT_TERM',
    ADD COLUMN IF NOT EXISTS promoted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS promotion_reason VARCHAR(32),
    ADD COLUMN IF NOT EXISTS last_accessed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS access_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS retention_until TIMESTAMP,
    ADD COLUMN IF NOT EXISTS source_entry_ids JSONB;

UPDATE agent_memory_entries
SET tier = 'SHORT_TERM'
WHERE tier IS NULL;

CREATE INDEX IF NOT EXISTS idx_agent_memory_tier ON agent_memory_entries(tier);
CREATE INDEX IF NOT EXISTS idx_agent_memory_agent_tier ON agent_memory_entries(agent_id, tier);
CREATE INDEX IF NOT EXISTS idx_agent_memory_tier_updated_at ON agent_memory_entries(tier, updated_at DESC);
