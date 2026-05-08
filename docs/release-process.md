# Release Process

This document defines how SYNAPSE moves through the implementation roadmap.

## Source of Truth

- `SYNAPSE_IMPLEMENTATION_ROADMAP.md` defines implementation order, milestone versions, patch windows, and fixed roadmap labels.
- `VERSION` stores the current roadmap milestone version.
- `CHANGELOG.md` records completed milestone changes.
- GitHub labels are synced from the roadmap label block by `.github/workflows/sync-roadmap-labels.yml`.

## Version Rules

- `v0.0.0` is the scaffold baseline.
- A major roadmap step completes as a minor version, such as `v0.1.0`, `v0.2.0`, or `v0.3.0`.
- Patch-sized work inside a step uses patch versions, such as `v0.2.1`, `v0.2.2`, and `v0.2.3`.
- Patch versions may appear in commit messages, PR titles, or issue titles.
- `VERSION` should only be advanced when a milestone is complete.
- Annotated milestone tags must include the matching changelog section in the tag message.
- Every pushed `v0.x.0` milestone tag must have a GitHub release.
- A milestone release should include all patch changelog notes accumulated during that step.
- GitHub releases are created or updated by `.github/workflows/create-milestone-release.yml`.

## Commit Rules

- Keep commits focused on one logical roadmap slice.
- Use conventional commit prefixes when possible: `feat`, `fix`, `docs`, `chore`, `ci`, `refactor`, `test`.
- Mention the roadmap version in the commit body when the commit belongs to a patch window.
- Use the matching phase label and one status label on GitHub issues and pull requests.

Example:

```text
docs: complete backend vault spec

Roadmap: v0.2.3
Labels: roadmap:phase-backend, roadmap:status-done
```

## Milestone Completion

Before a milestone is complete:

1. Confirm every exit criterion in the roadmap phase is satisfied.
2. Update `VERSION` to the milestone version.
3. Add or update the matching `CHANGELOG.md` section.
4. Mark related issues and PRs with `roadmap:status-done`.
5. Run the available validation commands for the touched area.
6. Commit the milestone update.

Optional tag command:

```bash
git tag -a v0.2.0 -F docs/release-notes/v0.2.0.md
git push origin v0.2.0
```

If a milestone tag was created without the changelog body, recreate it before public release:

```bash
git tag -d v0.2.0
git tag -a v0.2.0 -F docs/release-notes/v0.2.0.md
git push origin v0.2.0 --force
```

For an older milestone whose tag already exists, use the workflow's manual dispatch mode and pass the tag name. The workflow reads the matching file from `docs/release-notes/` on the default branch and creates or updates the GitHub release.

## GitHub Label Sync

The label sync workflow runs when either of these files changes:

- `SYNAPSE_IMPLEMENTATION_ROADMAP.md`
- `.github/workflows/sync-roadmap-labels.yml`

The workflow reads only the JSON block between these comments:

```text
<!-- ROADMAP_LABELS:BEGIN -->
<!-- ROADMAP_LABELS:END -->
```

Do not edit roadmap labels directly in GitHub unless the same change is also made in the roadmap file.

## Status Labels

- `roadmap:status-planned` means the work is known but not active.
- `roadmap:status-active` means implementation is currently moving.
- `roadmap:status-blocked` means an issue, dependency, or decision is preventing progress.
- `roadmap:status-done` means the work is complete and validated.

Only one roadmap status label should be active on an issue or pull request at a time.

## Phase Labels

Use one phase label for the dominant area of work:

- `roadmap:phase-foundation`
- `roadmap:phase-backend`
- `roadmap:phase-agents`
- `roadmap:phase-plugins`
- `roadmap:phase-store`
- `roadmap:phase-docs`
- `roadmap:phase-installer`
- `roadmap:phase-cli`
- `roadmap:phase-runtime`
- `roadmap:phase-hardening`
