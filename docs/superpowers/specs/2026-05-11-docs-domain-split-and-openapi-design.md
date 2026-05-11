# Design Spec: Docs Domain Split + OpenAPI REST Reference

## Context

The documentation set has grown and key pages are now too large to maintain comfortably:

- `synapse-docs/docs/deployment/environment-variables.mdx` (~1k+ lines)
- `synapse-docs/docs/api/rest-api.mdx` (large monolithic REST reference)

The goal is to improve readability and maintainability while keeping docs navigation intuitive.

## Approved Scope

1. Split environment-variable documentation into domain-focused pages.
2. Present those pages as a grouped sidebar dropdown in Docusaurus.
3. Replace manual REST reference style with true OpenAPI-powered docs as a first-class docs nav item.
4. Deliver as two separate commits and one push:
   - Commit A: env-vars restructure
   - Commit B: OpenAPI integration

## Approach (Approved)

Use a docs-native split + OpenAPI plugin integration:

- Keep docs content modular and grouped by operational domain.
- Configure Docusaurus to render OpenAPI spec content directly as navigable docs.
- Keep conceptual API docs (`api/overview`) and route detailed endpoint docs to generated OpenAPI pages.

This approach was selected over iframe/static Swagger embedding and over keeping manual REST markdown.

## Design

### 1) Environment Variables Information Architecture

Create a domain-based docs folder:

- `deployment/environment-variables/index.mdx`
- `deployment/environment-variables/core-system.mdx`
- `deployment/environment-variables/database.mdx`
- `deployment/environment-variables/redis.mdx`
- `deployment/environment-variables/qdrant.mdx`
- `deployment/environment-variables/security.mdx`
- `deployment/environment-variables/agent-store.mdx`
- `deployment/environment-variables/logging.mdx`
- `deployment/environment-variables/performance-hardening.mdx`
- `deployment/environment-variables/providers.mdx`
- `deployment/environment-variables/examples-and-secrets.mdx`
- `deployment/environment-variables/troubleshooting.mdx`

Content migration rule:

- Move existing sections mostly verbatim into target domain pages.
- Keep examples and warnings attached to the variables they describe.
- Avoid changing semantics of defaults or required-ness unless clearly incorrect.

### 2) Sidebar / Navigation Behavior

Update `synapse-docs/sidebars.ts` deployment sidebar to include:

- A collapsed category dropdown labeled similar to `Environment Variables`.
- Child items mapped to the new domain pages above.

Result:

- Readers can expand only the domain they need.
- The old single-page navigation bottleneck is removed.

### 3) OpenAPI REST Reference Integration

Add OpenAPI tooling to `synapse-docs` and configure Docusaurus docs plugin integration:

- Add OpenAPI renderer/plugin dependencies.
- Add plugin config in `docusaurus.config.ts`.
- Store spec at a stable path (e.g. `synapse-docs/openapi/synapse-rest-api.yaml`).
- Generate/render OpenAPI docs into docs routes and expose a dedicated sidebar/nav item (e.g. `REST API (OpenAPI)`).

Content model:

- `api/overview.mdx` remains conceptual entry point.
- Endpoint-level reference is generated from the OpenAPI spec.
- Existing `api/rest-api.mdx` is replaced with a bridge page or retired from sidebar to avoid duplicate authorities.

## Data Flow / Ownership

1. API contract source: OpenAPI spec file.
2. Docusaurus OpenAPI plugin renders docs pages from that spec.
3. Sidebar/nav points users to generated API references.
4. Conceptual and operational guidance remains in handwritten MDX pages.

## Error Handling / Risks

### Risk: Broken links after split
Mitigation:
- Keep index page at old conceptual entry path.
- Add explicit links from old anchors to new domain pages.
- Validate sidebar IDs and doc paths.

### Risk: OpenAPI plugin/version incompatibility
Mitigation:
- Use plugin versions compatible with current Docusaurus major version.
- Keep config minimal and conventional.
- Build docs locally after configuration change.

### Risk: Spec drift vs backend implementation
Mitigation:
- Start by encoding currently documented endpoints.
- Treat spec as authoritative going forward.
- Leave room for later CI validation against backend handlers.

## Testing / Validation

For env-vars split:

- Verify docs build passes.
- Verify sidebar dropdown renders and all pages resolve.
- Verify key old links still have clear forward paths.

For OpenAPI integration:

- Verify plugin loads and generated API docs render.
- Verify nav item appears and routes are stable.
- Verify representative endpoints, schemas, and examples display correctly.

## Commit Plan (User-Requested)

1. **Commit A** — Env vars split by domain + deployment sidebar dropdown update.
2. **Commit B** — OpenAPI plugin/config/spec + API nav integration.
3. Single push containing both commits.

## Out of Scope

- Reworking backend endpoint behavior.
- Full automatic contract extraction from code in this pass.
- Large stylistic rewrites of unrelated docs pages.

