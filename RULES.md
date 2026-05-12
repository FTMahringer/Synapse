# RULES.md - Project Rules and Team Conventions

This is the canonical rules document for SYNAPSE project workflow and quality gates.

## Scope and precedence

1. Use `AGENT.md` for agent execution behavior.
2. Use this file for project/process rules (versioning, release flow, docs coupling, testing, quality gates).
3. If both mention the same topic, this file is the source of truth for project policy.

---

## 1) Git and release workflow

### Development versions (`v*.*.x-dev`)

For each dev version, always:
1. Commit changes with a clear Conventional Commit message.
2. Update `CHANGELOG.md` with the version entry BEFORE tagging.
3. Create annotated tag for the dev version.
4. Push immediately (`main` + tag), do not batch.
5. Create GitHub **pre-release** after docs/changelog updates.

### Milestone releases (`v*.x.0`)

Before release:
1. Complete all planned dev versions.
2. Review/update docs and `CHANGELOG.md`.
3. Create release notes file (for example `RELEASE_NOTES_V2.X.0.md`).
4. Commit, tag, push, then create full GitHub release (not pre-release).

### Security hotfix policy (critical)

After every milestone release (`v*.x.0`):
1. Immediately review GitHub security alerts (Dependabot + CodeQL + secret scanning).
2. Fix all discovered security issues in one consolidated hotfix.
3. Tag as `v*.x.0-hotfix`.
4. Push and publish release for that hotfix.
5. Update `CHANGELOG.md` with security fixes.

Hard rules:
- One hotfix tag per milestone release cycle.
- Do not skip security checks.
- Test between fixes before publishing the hotfix.

---

## 2) Commit message rules

Use Conventional Commits:

`<type>(<scope>): <subject>`

Allowed types include:
- `feat`, `fix`, `fix(security)`, `docs`, `refactor`, `test`, `chore`, `perf`, `ci`

Requirements:
- Imperative subject, concise wording.
- Include body for non-trivial changes.
- Include trailer:

`Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`

---

## 3) Versioning rules

### Main repo format

- Development: `vMAJOR.MINOR.PATCH-dev`
- Milestone: `vMAJOR.MINOR.0`
- Hotfix: `vMAJOR.MINOR.0-hotfix`

Roadmap progression principle:
- V3 roadmap work is delivered through `v2.x.x` cycles and culminates in `v3.0.0`.

### `synapse-docs` mapping

- Docs pre-release format: `vD.N-dev`
- Docs milestone format: `vD.N`
- `D` matches main major version.
- `N` increments per milestone family that changes docs.
- Every docs-affecting dev version must produce a docs pre-release in `synapse-docs`.

---

## 4) Documentation coupling rules

Documentation is not optional and not deferred.

For every docs-affecting dev version:
1. Update relevant `synapse-docs` pages.
2. Update root `CHANGELOG.md`.
3. Update docs changelog page if applicable.
4. Update API docs/examples for changed endpoints.
5. Commit docs with code changes in the same cycle.
6. Create docs pre-release after docs push.

For every milestone:
1. Full docs accuracy review.
2. Milestone `CHANGELOG.md` entry.
3. Release notes file.
4. README/install/API updates as needed.
5. Migration guide when required by breaking changes.

---

## 5) Testing and quality gates

Before commit/merge:
1. Code compiles (Maven `mvn compile` or `docker compose build backend`).
2. Relevant tests pass.
3. Linters pass if configured.
4. For infra/backend changes, Docker Compose build verification is MANDATORY.
5. After Docker Compose build passes, a full smoke check (`docker compose up -d` + health endpoint) is recommended before milestone releases.

For build errors discovered in CI after push:
1. Immediately fix the compilation error.
2. Tag as `vX.Y.Z-hotfix`.
3. Push hotfix and create GitHub pre-release.
4. Continue with the next dev version.

Quality expectations:
- No committed secrets.
- No debug print leftovers in production paths.
- Follow existing naming and architecture conventions.
- Proper explicit error handling; do not ignore failures.

---

## 6) Deployment and repository conventions

- SYNAPSE is Docker-first, self-hosted.
- Docker Compose path must remain first-class.
- Bare-metal is supported.
- Kubernetes is optional (advanced), not baseline-required.
- `synapse-docs` is a separate git repository with its own release cadence.

Configuration handling:
- Maintain `.env.example`.
- Never commit `.env` or private secrets.

---

## 7) Communication and execution standards

- Be explicit about major changes and impacted files.
- Report blockers and errors directly; do not hide failures.
- When uncertain on conflicting directions, ask.
- Keep implementation aligned with established patterns unless a deliberate change is required.

---

## 8) User-driven preferences reinforced in this repo

These are recurring project preferences that should be treated as operating rules unless explicitly overridden:

1. Keep docs concise and avoid overwhelming, monolithic pages.
2. Use installer-first onboarding in quick-start content.
3. Keep deep install variants in Deployment docs (Docker / bare-metal / Kubernetes / troubleshooting).
4. Favor flatter sidebars where wrapper groups add noise.
5. Keep Plugin API reference separate from core API and place it under Plugins navigation.
6. Prefer clear, read-focused API docs UX; avoid cluttered "everything expanded" layouts.

