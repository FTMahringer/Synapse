# Agent & Memory Ideas

**Status**: Mixed (partly implemented, partly roadmap-planned)  
**Primary Roadmap Window**: v2.3.0 – v2.5.0

---

## Scope

This file consolidates agent behavior, collaboration, memory lifecycle, and learning-loop direction.

---

## Agent Foundations

### Agent Identity System

**Status**: Designed  
**Spec**: [`/docs/agent-identity-system.md`](/docs/agent-identity-system.md)

- File-based agent definition (`identity.md`, `soul.md`, `connections.md`, `config.yml`)
- Validation at load/reload
- Lifecycle logging and transparent diffability

### Agent Teams + AI-Firm

**Status**: Implemented foundation; advanced behavior ongoing  
**Spec**: [`/docs/agent-teams-system.md`](/docs/agent-teams-system.md), [`/docs/ai-firm-system.md`](/docs/ai-firm-system.md)

- Team routing contracts and team membership model
- AI-Firm project dispatch flow
- Collaboration sessions/messages/delegations/shared context (v2.3.4-dev)

### Reasoning & Planning

**Status**: Implemented foundation in v2.3.5-dev  

- Goal-based planning persistence
- Versioned plan artifacts with reasoning-chain snapshots
- Plan refinement/adaptation
- Next-step retrieval for token-efficient execution

### Agent Capability Hardening (next)

**Roadmap Target**: v2.3.7-dev

- End-to-end memory/collaboration/planning scenarios
- Delegation-loop/runaway-plan guardrails
- Performance/cost profiling for advanced workflows

---

## Memory System

### Three-Tier Lifecycle

**Status**: Implemented foundation (v2.3.3-dev)  
**Spec**: [`/docs/concepts/memory-system.mdx`](/docs/concepts/memory-system.mdx)

- `SHORT_TERM`, `KNOWLEDGE`, `ARCHIVE`
- Controlled promotions and tier-aware retrieval
- Monthly knowledge compaction + bi-monthly archive cleanup scaffold

### Memory Vault & Self-Learning

**Status**: Designed / incremental rollout  
**Specs**: [`/docs/memory-vault.md`](/docs/memory-vault.md), [`/docs/self-learning-loop.md`](/docs/self-learning-loop.md)

- Per-agent persistent memory model
- Reflection-driven knowledge/pattern extraction
- Skill suggestion with explicit user approval
- Optional soul-update proposal flow (never auto-apply)

---

## Built-in Skills (Token Efficiency)

**Status**: Idea  
**Detailed strategy**: [`./BUILTIN_SKILLS_STRATEGY.md`](./BUILTIN_SKILLS_STRATEGY.md)

- Core enforced built-ins (e.g., `codeburn`)
- TUI-selectable built-ins (e.g., `caveman`)
- Activation-first model (no install/uninstall for built-ins)

