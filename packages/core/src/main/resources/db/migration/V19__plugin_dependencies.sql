-- Plugin dependency tracking for v2.5.3-dev
-- Stores plugin dependency declarations and resolution state

CREATE TABLE IF NOT EXISTS plugin_dependencies (
    id SERIAL PRIMARY KEY,
    plugin_id TEXT NOT NULL REFERENCES plugins(id) ON DELETE CASCADE,
    dependency_id TEXT NOT NULL,
    version_spec TEXT NOT NULL DEFAULT '*',
    is_soft BOOLEAN NOT NULL DEFAULT FALSE,
    resolution_state TEXT NOT NULL DEFAULT 'PENDING'
        CHECK (resolution_state IN ('PENDING', 'SATISFIED', 'MISSING', 'BLOCKED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(plugin_id, dependency_id)
);

COMMENT ON TABLE plugin_dependencies IS
    'Declared dependencies for each plugin with resolution tracking';
COMMENT ON COLUMN plugin_dependencies.plugin_id IS
    'The plugin that declares this dependency';
COMMENT ON COLUMN plugin_dependencies.dependency_id IS
    'The plugin id that is depended upon';
COMMENT ON COLUMN plugin_dependencies.version_spec IS
    'Semver constraint: *, 1.0.0, >=1.0.0, ^1.0.0, ~1.0.0';
COMMENT ON COLUMN plugin_dependencies.is_soft IS
    'Soft deps load without the dependency present; hard deps block install';
COMMENT ON COLUMN plugin_dependencies.resolution_state IS
    'Current resolution state of this dependency edge';

CREATE INDEX IF NOT EXISTS idx_plugin_deps_plugin ON plugin_dependencies (plugin_id);
CREATE INDEX IF NOT EXISTS idx_plugin_deps_dependency ON plugin_dependencies (dependency_id);
CREATE INDEX IF NOT EXISTS idx_plugin_deps_state ON plugin_dependencies (resolution_state);
