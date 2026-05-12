# Plugin Publishing and Maven Repository Strategy

## Context

The SYNAPSE plugin ecosystem requires a Maven repository to host `synapse-plugin-api` and all plugin JARs. Currently, nothing is published to Maven Central, and the Docker/CI builds fail because `synapse-plugin-api:1.0.0` cannot be resolved from public repositories.

## Decision: Self-Hosted Nexus (Not Maven Central)

For the foreseeable future, **no artifacts will be published to Maven Central**. Instead:

- A **self-hosted Sonatype Nexus** instance will serve as the primary Maven repository
- All official plugins, approved community plugins, official bundles, and approved community bundles will be hosted there
- The core `synapse-plugin-api` JAR will also be hosted there

## Admin-Configurable Repository URL

Admins can configure their own Nexus instance as the "home store" for their private plugins:

- **CLI**: `synapse config set repo.url https://nexus.mycompany.com/repository/synapse-plugins/`
- **Dashboard**: Settings → Plugin Store → Repository URL
- The platform uses this repo to resolve and download plugins

## Plugin Publishing Options (To Be Decided)

Two possible paths for third-party plugin publishing:

### Option A: Maven Central → Scraped to Nexus
- Authors publish to Maven Central as usual
- SYNAPSE Nexus periodically scrapes/imports approved artifacts
- Pros: familiar workflow for Java developers, existing tooling
- Cons: delay between publish and availability, need to monitor Central

### Option B: Direct to SYNAPSE Nexus
- Authors publish directly to the SYNAPSE Nexus instance
- Heavy security and validation gates before acceptance
- Pros: immediate availability, full control over artifacts
- Cons: authors need Nexus credentials, different workflow from standard Maven

## Security and Validation Gates

Regardless of publishing path, every artifact must pass:

1. **Bytecode scan** (ASM) — no forbidden references
2. **Manifest validation** — required fields present, semver valid
3. **Dependency resolution** — no missing hard deps, no cycles
4. **Slot clash check** — no conflicting channel_id / provider_id
5. **Trust tier assignment** — official vs community vs unverified
6. **Signature verification** (future) — signed JARs only for community

## Open Questions

- [ ] Which publishing path (A or B) is preferred?
- [ ] Should unverified community plugins be allowed at all, or only after approval?
- [ ] How to handle versioning conflicts when scraping from Maven Central?
- [ ] What is the review process for community plugin approval?
- [ ] Should there be a staging repository for pending-review plugins?

## Related

- `docs/superpowers/specs/2026-05-12-v2.6.0-plugin-ecosystem-design.md`
- `docs/superpowers/specs/2026-05-12-v2.7.0-plugin-ecosystem-advanced-design.md`
- `store/STORE_SPEC.md`
