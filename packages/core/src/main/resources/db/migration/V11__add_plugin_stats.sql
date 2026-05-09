CREATE TABLE plugin_stats (
    plugin_id VARCHAR(255) PRIMARY KEY,
    install_count BIGINT NOT NULL DEFAULT 0,
    enable_count BIGINT NOT NULL DEFAULT 0,
    disable_count BIGINT NOT NULL DEFAULT 0,
    last_used_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
