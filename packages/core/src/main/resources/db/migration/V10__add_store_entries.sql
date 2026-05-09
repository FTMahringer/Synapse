CREATE TABLE store_entries (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    source VARCHAR(255) NOT NULL,
    version VARCHAR(100) NOT NULL,
    author VARCHAR(255),
    license VARCHAR(100),
    description TEXT,
    min_synapse VARCHAR(100),
    tags JSONB,
    meta JSONB,
    synced_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_store_entries_type ON store_entries(type);
CREATE INDEX idx_store_entries_source ON store_entries(source);
