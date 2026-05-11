# Design Spec: Installation Docs Clarity + Sidebar Flattening

## Context

Current docs navigation and installation guidance create friction:

- Homepage **Get Started** points to quick-start rather than introduction.
- Getting Started and Deployment installation content overlap.
- Quick-start overemphasizes manual compose + `.env`, while installer scripts exist.
- Sidebar structure has wrappers/categories where direct items are preferred.

## Approved Scope

1. Change homepage **Get Started** button target to `Introduction to SYNAPSE`.
2. Keep Getting Started installation guidance short and onboarding-focused.
3. Move/keep full installation depth in Deployment guides, grouped by variant.
4. Remove global-group style wrappers from sidebars.
5. In Plugins sidebar, remove `Overview` category and show overview/architecture as direct items.

## Approach (Approved)

- Use **Getting Started** for minimal path-to-first-run.
- Use **Deployment** for full operational detail and variant-specific guidance.
- Apply sidebar simplification consistently to reduce click depth and visual noise.

## Design

### 1) Homepage CTA correction

Update homepage CTA:

- from: `/docs/getting-started/quick-start`
- to: `/docs/getting-started/introduction`

This restores expected onboarding sequence (intro first, then quick install).

### 2) Getting Started: Quick Install format

`getting-started/quick-start.mdx` should be concise:

- Prerequisites (minimal)
- Run installer:
  - Linux/macOS: `./install.sh`
  - Windows: `.\installer\install.ps1`
- Verify:
  - dashboard URL
  - health endpoint
- default login warning
- CTA to full deployment docs

No long platform-variant detail here.

### 3) Deployment docs grouping

Deployment sidebar keeps grouped install variants:

- Docker variant
- Bare-metal variant
- Kubernetes variant
- Troubleshooting and operations references

Full installation complexity lives here, not in quick-start.

### 4) Sidebar flattening rules

Apply across sidebars:

- remove unnecessary top-level wrapper groups ("global group" style)
- keep direct doc items where grouping does not add structure
- preserve meaningful grouped categories (e.g., Official/Community plugins, environment variable domains)

### 5) Plugins sidebar update

Replace:

- `Overview` category with two items inside

With:

- direct sidebar items:
  - `plugins/overview`
  - `plugins/architecture`

Keep remaining plugin categories unchanged unless structurally needed.

## Data Flow / Ownership

- Getting Started owns onboarding path.
- Deployment owns install-depth and platform operations.
- Sidebars are the source of navigation behavior; docs pages should avoid duplicative structural intent.

## Error Handling / Risks

### Risk: broken links during sidebar/content move
Mitigation:

- keep stable doc IDs/paths where possible
- run docs build and check link integrity

### Risk: user confusion from reduced quick-start detail
Mitigation:

- add explicit “Full installation guides” CTA in quick-start
- include concise “when to use Deployment docs” note

## Testing / Validation

1. Homepage CTA leads to introduction page.
2. Quick-start renders installer-first path correctly.
3. Deployment sidebar still exposes full install variants.
4. Plugins sidebar shows overview + architecture as direct items.
5. Docs build succeeds without new link errors.

## Out of Scope

- Rewriting every deployment guide deeply.
- Changing runtime installer behavior itself (docs/navigation only in this scope).

