# Design Spec: Read-Only Swagger UI + Plugin API Sidebar Reference

## Context

Current single-page OpenAPI rendering solved file sprawl but still feels visually off:

- not using full available width
- overwhelming on first load
- insufficiently structured endpoint discovery

Additionally, plugin API reference must be separated from core operational API and placed under the existing Plugins documentation navigation.

## Approved Scope

1. Replace current REST API renderer with a Swagger-like, read-only accordion interface.
2. Keep examples visible per endpoint/response, sourced from OpenAPI spec (not live calls).
3. Ensure full-width rendering for API reference pages.
4. Add a separate Plugin API reference under the existing Plugins sidebar (not under core API sidebar).

## Approach (Approved)

Use a local Swagger UI embedding approach with static assets and two separate specs:

- `core-api.yaml` for platform operational endpoints
- `plugin-api.yaml` for plugin-facing contracts

Each spec is rendered as a read-only reference page using the same UI initializer and styling profile.

## Design

### 1) UI Rendering Model

Create/keep a single reusable Swagger host component/page mode with these runtime options:

- `docExpansion: 'none'` (accordion/collapsed by default)
- `supportedSubmitMethods: []` (read-only, no try-it-out)
- `defaultModelsExpandDepth: -1` (hide large schema panel by default)
- `deepLinking: true`

Load assets locally from repository-managed/static path to avoid CDN drift and rendering inconsistencies.

### 2) Layout / Width Behavior

Apply page-scoped CSS so API docs use available content width and avoid narrow-column rendering artifacts.

Rules:

- keep Docusaurus global layout intact
- only widen API reference container(s)
- avoid side effects on unrelated docs pages

### 3) Example Output Strategy

Populate OpenAPI response examples directly in spec files:

- `content -> application/json -> examples`
- key success and relevant error responses

Coverage targets for core API examples:

- health/readiness
- memory CRUD/promote
- collaboration sessions/messages/delegations/context
- planning goals/plans/refine/next-step
- tools list/get/execute

Add shared error schema/examples and reference consistently.

### 4) Spec Separation

Maintain two explicit specs:

- `static/openapi/core-api.yaml`
- `static/openapi/plugin-api.yaml`

Benefits:

- cleaner ownership boundaries
- easier plugin contract evolution without touching core operational reference
- less cognitive load per page

### 5) Navigation Placement

- Core REST reference stays in `API Reference` sidebar.
- Plugin API reference is added under the existing `Plugins` sidebar as a dedicated category entry (peer-level with existing plugin sections).

This preserves user expectation: plugin contract docs live in plugin docs.

## Data Flow / Ownership

1. Authors update OpenAPI YAML files.
2. Swagger host page reads static spec URL.
3. UI renders operation groups and examples directly from spec.
4. No generated multi-file docs tree is committed.

## Error Handling / Risks

### Risk: parser/runtime spec errors in UI
Mitigation:

- validate YAML syntax before commit
- keep schema/example consistency tight
- include build check and manual open-page check

### Risk: style regressions from full-width CSS
Mitigation:

- scope CSS to API reference wrappers only
- verify both light/dark theme rendering

### Risk: duplicated or drifting plugin endpoint docs
Mitigation:

- plugin API spec is authoritative source
- plugin MDX pages should link/reference spec-rendered content rather than duplicating endpoint details

## Testing / Validation

1. `synapse-docs` build succeeds.
2. Core API page renders read-only accordion behavior.
3. Try-it-out controls are absent.
4. Examples are visible for key endpoints.
5. Plugin API page renders under Plugins sidebar placement.
6. Raw spec links resolve correctly under docs baseUrl.

## Out of Scope

- Changing backend endpoint behavior.
- Introducing live request execution from docs.
- Full OpenAPI codegen or SDK generation pipeline.

