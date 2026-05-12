# IDEAS.md — Future Concepts & Inspiration

Ideas collected during planning sessions. Not on any active roadmap yet.
Each entry has a rough category and the context that sparked it.

---

## Plugin Store Website (Modrinth-inspired)

**Category:** Ecosystem / Community
**Sparked by:** v2.6.0 plugin system planning session

A standalone web platform (similar to [Modrinth](https://modrinth.com)) for browsing, searching,
and installing SYNAPSE plugins — separate from the in-app store UI.

Key ideas:
- Public-facing site, no SYNAPSE instance required to browse
- Plugin pages with descriptions, screenshots, version history, changelogs
- Author profiles, download counts, ratings
- Search + filter by category (channel, model provider, skill, MCP), trust tier, compatibility
- One-click install via SYNAPSE CLI (`synapse plugin install <id>`)
- API backing the in-app store (same data source as the dashboard marketplace)
- Community plugins from `/synapse-plugins-community`, official from `/synapse-plugins`
- Could eventually replace or front the Nexus/Sonatype artifact registry

**Dependencies:** Requires v2.6.0 plugin system + v2.7.0 registry service to be solid first.

---
