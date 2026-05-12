-- Plugin sandbox state tracking for v2.5.4-dev
-- Adds bytecode scan results and sandbox flags to the plugins table

ALTER TABLE plugins
    ADD COLUMN IF NOT EXISTS scan_clean BOOLEAN,
    ADD COLUMN IF NOT EXISTS scan_violations JSONB,
    ADD COLUMN IF NOT EXISTS sandbox_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS lifecycle_timeout_ms BIGINT,
    ADD COLUMN IF NOT EXISTS message_timeout_ms BIGINT,
    ADD COLUMN IF NOT EXISTS max_logs_per_minute INTEGER;

COMMENT ON COLUMN plugins.scan_clean IS
    'Result of last ASM bytecode scan: true = clean, false = violations found, null = not scanned';
COMMENT ON COLUMN plugins.scan_violations IS
    'JSON array of bytecode scan violations (classFile, reference, type)';
COMMENT ON COLUMN plugins.sandbox_enabled IS
    'Whether sandbox restrictions are active for this plugin';
COMMENT ON COLUMN plugins.lifecycle_timeout_ms IS
    'Configured lifecycle hook timeout in milliseconds';
COMMENT ON COLUMN plugins.message_timeout_ms IS
    'Configured message handler timeout in milliseconds';
COMMENT ON COLUMN plugins.max_logs_per_minute IS
    'Configured max log entries per minute';

CREATE INDEX IF NOT EXISTS idx_plugins_scan_clean ON plugins (scan_clean);
CREATE INDEX IF NOT EXISTS idx_plugins_sandbox_enabled ON plugins (sandbox_enabled);
