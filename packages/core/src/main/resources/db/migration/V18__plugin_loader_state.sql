-- Plugin loader state tracking for v2.5.2-dev
-- Adds storage tier, loader state, and error tracking to the plugins table

ALTER TABLE plugins
    ADD COLUMN IF NOT EXISTS storage_tier TEXT NOT NULL DEFAULT 'system'
        CHECK (storage_tier IN ('system', 'staging')),
    ADD COLUMN IF NOT EXISTS loader_state TEXT NOT NULL DEFAULT 'UNLOADED'
        CHECK (loader_state IN ('UNLOADED', 'LOADING', 'LOADED', 'ERROR')),
    ADD COLUMN IF NOT EXISTS error_message TEXT,
    ADD COLUMN IF NOT EXISTS loaded_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS api_version TEXT,
    ADD COLUMN IF NOT EXISTS trust_tier TEXT NOT NULL DEFAULT 'COMMUNITY'
        CHECK (trust_tier IN ('OFFICIAL', 'COMMUNITY'));

COMMENT ON COLUMN plugins.storage_tier IS
    'Where the plugin JAR is stored: system (persisted) or staging (runtime-installed)';
COMMENT ON COLUMN plugins.loader_state IS
    'Runtime loader state: UNLOADED, LOADING, LOADED, ERROR';
COMMENT ON COLUMN plugins.error_message IS
    'Diagnostic message when loader_state = ERROR';
COMMENT ON COLUMN plugins.loaded_at IS
    'Timestamp when the plugin was last successfully loaded into the JVM';
COMMENT ON COLUMN plugins.api_version IS
    'synapse-plugin-api version required by this plugin, e.g. >=1.0.0';
COMMENT ON COLUMN plugins.trust_tier IS
    'OFFICIAL (from synapse-plugins repo) or COMMUNITY (from synapse-plugins-community)';

CREATE INDEX IF NOT EXISTS idx_plugins_loader_state ON plugins (loader_state);
CREATE INDEX IF NOT EXISTS idx_plugins_storage_tier ON plugins (storage_tier);
CREATE INDEX IF NOT EXISTS idx_plugins_trust_tier ON plugins (trust_tier);
