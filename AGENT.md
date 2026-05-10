# AGENT.md - Workflow Rules & Guidelines for AI Agents

This document defines the mandatory workflow rules and development conventions that all AI agents must follow when working on the SYNAPSE project.

---

## Git Workflow Rules

### 1. Commit, Tag, and Push for Every Development Version

**Rule:** For every `v*.*.x-dev` development version, you MUST:

1. **Commit changes** with a clear, descriptive commit message
2. **Create an annotated tag** for the dev version
3. **Push immediately** after committing and tagging

**Example:**
```bash
# Make changes for v2.0.1-dev
git add .
git commit -m "feat(docs): setup Docusaurus infrastructure

- Initialize Docusaurus project with custom theme
- Configure documentation structure
- Add version selector

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

# Tag the dev version
git tag -a v2.0.1-dev -m "v2.0.1-dev: Docusaurus Setup & Structure

Initialized documentation site with Docusaurus."

# Push IMMEDIATELY (don't batch)
git push origin main
git push origin v2.0.1-dev
```

**❌ WRONG:** Batching multiple commits/tags and pushing at the end  
**✅ CORRECT:** Commit → Tag → Push → Repeat for next task

---

### 2. GitHub Pre-Releases for Development Versions

**Rule:** For every `v*.*.x-dev` tag, create a GitHub pre-release AFTER documentation is updated.

**Workflow:**
1. Complete code changes
2. **Update documentation** (including CHANGELOG.md)
3. Commit → Tag → Push
4. **Create pre-release**

**Use GitHub CLI:**
```bash
gh release create v2.0.1-dev \
  --prerelease \
  --title "v2.0.1-dev: Docusaurus Setup & Structure" \
  --notes "Initialized documentation site with Docusaurus.

**Added:**
- Docusaurus project structure
- Custom theme configuration
- Documentation navigation"
```

**Requirements:**
- Mark as pre-release (not production release)
- Use descriptive title matching the dev version
- Include brief release notes highlighting what was added/changed
- Create immediately after pushing the tag
- **Documentation must be updated BEFORE creating pre-release**

---

### 3. GitHub Releases for Minor Versions

**Rule:** For every `v*.x.0` milestone release, create a full GitHub release (not pre-release) AFTER comprehensive documentation review.

**Workflow:**
1. Complete all dev versions (v2.0.1-dev through v2.0.x-dev)
2. **Review and update all documentation**
3. **Update CHANGELOG.md** with milestone summary
4. **Create release notes file** (RELEASE_NOTES_V2.X.0.md)
5. Commit documentation → Tag → Push
6. **Create release**

**Example:**
```bash
gh release create v2.1.0 \
  --title "v2.1.0: Documentation Platform" \
  --notes-file RELEASE_NOTES_V2.1.0.md
```

**Requirements:**
- NOT marked as pre-release (this is a production-ready milestone)
- Use comprehensive release notes file
- Include all features from v2.0.1-dev through v2.0.x-dev
- Tag milestone achievements
- **All documentation must be reviewed and updated BEFORE creating release**

---

### 4. Commit Message Format

**Rule:** Use Conventional Commits format.

**Structure:**
```
<type>(<scope>): <subject>

<body>

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Maintenance tasks
- `perf`: Performance improvements
- `ci`: CI/CD changes

**Examples:**
```
feat(agents): add distributed task execution framework
fix(auth): resolve JWT token expiration edge case
docs(api): add WebSocket API documentation
refactor(core): extract users module to top-level package
test(providers): add integration tests for OpenAI provider
chore(deps): update Spring Boot to 4.0.1
```

**Requirements:**
- Always include `Co-authored-by: Copilot` trailer
- Keep subject line ≤ 50 characters
- Use imperative mood ("add" not "added")
- Include body for complex changes
- Reference issues when applicable

---

## Version Numbering Rules

### Version Format: `vMAJOR.MINOR.PATCH-dev`

**Structure:**
- **MAJOR** (`v3.0.0`): Major release with breaking changes
- **MINOR** (`v2.1.0`): Feature milestone within major version
- **PATCH** (`v2.0.1-dev`, `v2.0.2-dev`, etc.): Development iterations

### Roadmap Versioning Philosophy

**The V3 Roadmap leads TO v3.0.0:**
- All work uses `v2.x.x` versions
- Development versions: `v2.0.1-dev`, `v2.0.2-dev`, ..., `v2.11.4-dev`
- Milestone releases: `v2.1.0`, `v2.2.0`, ..., `v2.12.0`
- **Final release:** `v3.0.0`

**Example progression:**
```
v2.0.1-dev  → Docusaurus setup
v2.0.2-dev  → Migrate existing docs
v2.0.3-dev  → Installation guides
v2.0.4-dev  → API documentation
v2.0.5-dev  → Developer docs
v2.0.6-dev  → Deployment & hosting
v2.1.0      → Documentation Platform Release (milestone)

v2.1.1-dev  → Create infrastructure layer
v2.1.2-dev  → Create common layer
...
v2.2.0      → Package Restructure Release (milestone)
```

---

## Documentation Requirements

### Documentation Philosophy

**Rule:** Documentation must always be reasonable and well-structured.

**What "reasonable and well-structured" means:**
- **Accurate:** Reflects actual implementation, no outdated information
- **Clear:** Written for humans, not machines
- **Comprehensive:** Covers all user-facing features and APIs
- **Organized:** Logical structure, easy navigation, consistent formatting
- **Up-to-date:** Updated with EVERY code change, not as an afterthought
- **Accessible:** Examples, screenshots, diagrams where helpful
- **Searchable:** Good headings, keywords, cross-references

**❌ Bad documentation:**
- Missing steps in installation guide
- Outdated screenshots showing old UI
- API endpoints without examples
- Features mentioned in roadmap but not documented
- Copy-pasted from old version without updating

**✅ Good documentation:**
- Step-by-step guides that actually work
- Current screenshots matching implementation
- API examples with request/response
- Complete feature documentation matching roadmap deliverables
- Version-specific documentation (v2.x vs v3.x)

---

### For Every Development Version (`v*.*.x-dev`)

**Rule:** Update documentation AND changelog to reflect changes made in the development version.

**Required actions:**
1. Update relevant documentation files in `/synapse-docs/docs/`
2. **Add changelog entry to `CHANGELOG.md` in root repository**
3. Add changelog entry to `/synapse-docs/docs/changelog.md` (documentation site)
4. Update API documentation if endpoints changed
5. Add examples for new features
6. Update screenshots if UI changed
7. Commit documentation changes WITH the code changes

**Example:**
```bash
# v2.1.1-dev: Create Infrastructure Layer
# Code changes:
packages/core/src/main/java/dev/synapse/core/infrastructure/...

# Documentation changes:
synapse-docs/docs/core-concepts/architecture.md  # Update package structure
synapse-docs/docs/development/codebase.md        # Update developer guide
synapse-docs/docs/changelog.md                   # Add v2.1.1-dev entry

# Repository changelog:
CHANGELOG.md                                     # Add v2.1.1-dev section
```

**Commit message should mention both:**
```
refactor(core): create infrastructure layer

Moved cross-cutting concerns to core/infrastructure:
- security/, logging/, event/, exception/, filter/

Updated documentation:
- Architecture diagrams showing new structure
- Developer guide with package organization
- Changelog entries (CHANGELOG.md and docs site)

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

**CHANGELOG.md format for dev versions:**
```markdown
## [v2.1.1-dev] - 2026-XX-XX

### Refactor
- Created core infrastructure layer for cross-cutting concerns
- Moved security, logging, event, exception, filter packages to core/infrastructure

### Documentation
- Updated architecture diagrams
- Updated developer guide with package structure
```

---

### For Every Milestone Release (`v*.x.0`)

**Rule:** Comprehensive documentation review, changelog update, and release notes creation.

**Required actions BEFORE creating the release:**
1. **Review all documentation** for accuracy
2. **Update CHANGELOG.md** with complete milestone summary
3. **Create/update release notes** file (e.g., `RELEASE_NOTES_V2.1.0.md`)
4. **Update README.md** if needed (badges, quick start, features)
5. **Update installation guides** if deployment changed
6. **Update API documentation** with new endpoints
7. **Add migration guide** if breaking changes
8. **Create announcement blog post** (optional but recommended)
9. **Update version selector** in Docusaurus (add v2.1.0 to versions)

**CHANGELOG.md format for milestone releases:**
```markdown
## [v2.1.0] - 2026-XX-XX

**Milestone:** Documentation Platform

Complete Docusaurus-based documentation site with version management, API reference, and deployment guides.

### Added
- Docusaurus documentation site with custom theme
- Version selector for v2.x and v3.x documentation
- Comprehensive API reference with examples
- Installation and deployment guides
- Developer contribution guide
- Algolia DocSearch integration

### Changed
- Migrated all /docs/*.md files to Docusaurus structure
- Reorganized documentation with improved navigation

### Development Versions
- v2.0.1-dev: Docusaurus setup
- v2.0.2-dev: Migrate existing docs
- v2.0.3-dev: Installation guides
- v2.0.4-dev: API documentation
- v2.0.5-dev: Developer docs
- v2.0.6-dev: Deployment & hosting
```

**Milestone documentation checklist:**
- [ ] All features documented
- [ ] Breaking changes highlighted
- [ ] Migration guide created (if needed)
- [ ] API reference updated
- [ ] Installation guide accurate
- [ ] README badges updated
- [ ] Release notes comprehensive
- [ ] Examples work with new version
- [ ] Screenshots up to date
- [ ] Search index rebuilt

---

## Testing Requirements

### Before Every Commit

**Rule:** Validate changes before committing.

**Required checks:**
1. **Code compiles** without errors
2. **Tests pass** (unit + integration if applicable)
3. **No linting errors** (if linters exist)
4. **Docker Compose starts** (for infrastructure changes)

**Commands:**
```bash
# Java/Maven
mvn clean compile  # Must succeed
mvn test           # Must pass

# Docker Compose (if backend changed)
cd installer/compose
docker compose up -d
# Verify all services healthy
docker compose ps
# Check migrations applied
docker compose logs backend | grep "Flyway"
```

---

## Development Workflow

### Step-by-Step Process for Each Development Version

**Example: Implementing v2.0.1-dev**

1. **Read the roadmap** - Understand what v2.0.1-dev requires
2. **Plan the work** - Break down into tasks
3. **Implement changes** - Write code/config
4. **Update documentation** - Add docs for the changes
5. **Test locally** - Verify everything works
6. **Commit changes** - Include code + docs in one commit
7. **Tag the version** - `git tag -a v2.0.1-dev -m "..."`
8. **Push immediately** - `git push origin main && git push origin v2.0.1-dev`
9. **Create GitHub pre-release** - `gh release create v2.0.1-dev --prerelease ...`
10. **Move to next version** - Start v2.0.2-dev

**❌ Never skip steps**  
**❌ Never batch multiple dev versions**  
**✅ Complete each version fully before moving to next**

---

## Deployment Philosophy (Important!)

### SYNAPSE is Docker-First, Self-Hosted Software

**Core principles:**
- **Primary deployment:** Docker Compose
- **Single-node focus:** Easy for homelab and small teams
- **Bare-metal friendly:** systemd services, not just containers
- **Kubernetes optional:** Advanced deployment, NOT required
- **Self-hosting emphasis:** Like OpenClaw, Langfuse, Supabase, GitLab self-managed

**What this means for development:**
1. Docker Compose MUST always work
2. Documentation should prioritize simple deployments
3. Kubernetes is a bonus, not a requirement
4. Features should work on single-node setups, multi-node (kubernetes) later
5. Resource usage should be reasonable for homelab hardware

**Example priorities:**
- ✅ Simple Docker Compose deployment
- ✅ Environment variable configuration
- ✅ SQLite option for small deployments
- ❌ Kubernetes-only features
- ❌ Massive resource requirements
- ❌ Cloud-only dependencies

---

## File Organization Rules

### Documentation Site (`/synapse-docs/`)

**Rule:** Separate git repository, gitignored from main repo.

**Location:** `/synapse-docs/` (root-level folder)  
**Git:** Separate `.git` folder inside `/synapse-docs/`  
**Ignored:** Added to main repo's `.gitignore`

**Why separate?**
- Different release cycle than code
- Can be deployed independently
- Cleaner git history
- Multiple contributors to docs vs code

---

### Test Files

**Rule:** Tests go alongside production code.

**Java/Maven structure:**
```
packages/core/src/
├── main/java/dev/synapse/...          # Production code
└── test/java/dev/synapse/...          # Test code
    ├── unit/                          # Unit tests
    ├── integration/                   # Integration tests
    └── BaseIntegrationTest.java       # Test base classes
```

---

### Configuration Files

**Rule:** Environment-based configuration.

**Required:**
- `.env.example` - Template with all variables documented
- `docker-compose.yml` - Uses `${ENV_VAR}` references
- `application.yml` - Uses `${ENV_VAR}` references

**Never commit:**
- `.env` - User-specific values
- `.env.local` - Local overrides
- Secrets, API keys, passwords

---

## Code Quality Rules

### Before Merging to Main

**Rule:** Code must meet quality standards.

**Requirements:**
1. **No commented-out code** (unless temporarily needed with TODO)
2. **Reasonable and good comments** (only with long code-blocks, maybe interfaces as well)
2. **No console.log / System.out.println** in production code
3. **Proper error handling** (try-catch, error responses)
4. **Consistent naming** (follow existing conventions)
5. **Documentation comments** for public APIs
6. **No hardcoded values** (use configuration)

### Package Structure Conventions

**Rule:** Follow domain-driven design.

**Pattern for feature modules:**
```
feature/
├── api/         # Controllers (REST endpoints)
├── service/     # Business logic
├── domain/      # Feature-specific entities (if needed)
├── dto/         # Request/Response objects
└── README.md    # Module documentation
```

**Example - agents module:**
```
agents/
├── api/
│   ├── AgentManagementController.java
│   └── AgentTeamController.java
├── service/
│   ├── AgentService.java
│   └── AgentTeamService.java
└── dto/
    ├── AgentDTO.java
    └── CreateAgentRequest.java
```

---

## Communication & Transparency

### Always Explain What You're Doing

**Rule:** Be transparent about actions.

**Before major changes:**
- Explain your approach
- Ask for confirmation if uncertain
- List files that will be modified
- Estimate impact

**During work:**
- Report progress
- Notify about issues encountered
- Ask questions when stuck

**After completion:**
- Summarize what was done
- List files created/modified/deleted
- Mention any deviations from plan

---

## Error Handling

### When Things Go Wrong

**Rule:** Don't hide errors, fix them.

**If a command fails:**
1. **Don't ignore it** - Address the failure
2. **Read error messages** - Understand what went wrong
3. **Try alternative approaches** - Be creative
4. **Ask for help** - If stuck, explain the situation
5. **Document the issue** - Add to troubleshooting docs

**Example:**
```bash
# ❌ WRONG: Ignore test failures
mvn test  # 5 tests failed
git commit -m "feat: add feature"  # Committing anyway

# ✅ CORRECT: Fix the failures
mvn test  # 5 tests failed
# Read error output
# Fix the failing tests
mvn test  # All tests pass
git commit -m "feat: add feature"
```

**Always then add `-hotfix` to the tag as well**
If a new commit and push is made, then make it also a new vx.x.(x)-hotfix tag and pre-release.

---

## Summary Checklist

Before completing ANY development version, verify:

- [ ] Code changes implemented and tested
- [ ] Documentation updated (if applicable)
- [ ] Tests passing (unit + integration)
- [ ] Commit message follows convention
- [ ] Committed with descriptive message
- [ ] Tagged with `v*.*.x-dev`
- [ ] Pushed to origin immediately
- [ ] GitHub pre-release created
- [ ] Ready for next development version

Before completing ANY milestone release, verify:

- [ ] All development versions (v*.*.x-dev) completed
- [ ] Full documentation review completed
- [ ] Release notes file created
- [ ] README updated (if needed)
- [ ] Migration guide added (if breaking changes)
- [ ] All tests passing
- [ ] Docker Compose smoke test passes
- [ ] Tagged with `v*.x.0`
- [ ] Pushed to origin immediately
- [ ] GitHub release created (NOT pre-release)

---

## Questions?

If anything is unclear:
1. Ask the user for clarification
2. Reference this document
3. Look at previous commits/tags/releases as examples
4. Follow the established patterns

**Remember:** Consistency and transparency are key to successful collaboration!
