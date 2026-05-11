# Platform & Ecosystem Ideas

**Status**: Mixed (some completed, some roadmap-planned, some long-term)  
**Primary Roadmap Window**: v2.6.0 – v3.x

---

## Scope

This file consolidates platform-level features, plugin ecosystem direction, and long-term product extensions.

---

## Plugin Ecosystem

### Ecosystem Baseline

**Status**: Core repository structure largely established

- Official plugins repository
- Community plugins repository
- Template repository for bootstrapping new plugins

### Java-First Plugin Strategy

**Status**: Active strategy  

- Native Java plugins as primary model
- external runtime model for non-Java plugins in later phases
- phased approach to SDK/tooling, sandboxing, marketplace, and runtime expansion

### Next Plugin Milestones

**Roadmap Target**: v2.6.0 – v2.7.0

- Java plugin API and lifecycle
- SDK/tooling and validation
- sandbox/security controls
- marketplace and governance hardening
- registry reliability and external runtime foundation

---

## Platform Features

### Multi-User System

**Status**: Implemented foundation; ongoing hardening  
**Spec**: [`/docs/multi-user.md`](/docs/multi-user.md)

- role-based access and tenant-aware resource boundaries
- admin/operator visibility model

### Store, Themes, and Commands

**Status**: Mixed

- Store model and extension lifecycle
- custom commands and workflow shortcuts
- dashboard theming and personalization

---

## Long-Term Backlog (v3+)

### AI/Product Extensions

- multi-modal model support
- fine-tuning orchestration
- deeper multi-agent protocol evolution

### Client Surfaces

- mobile clients
- desktop client
- advanced UX workflows

### Developer Experience

- GraphQL surface (optional alongside REST)
- official SDKs across multiple languages
- richer plugin/operator tooling

