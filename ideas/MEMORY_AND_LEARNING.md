# Memory & Learning

**Status**: Designed (detailed specs in `/docs/`)  
**Target**: v2.3.0 – v2.4.0

---

## Memory Vault

**Spec**: [`/docs/memory-vault.md`](/docs/memory-vault.md)  
**Priority**: High

Per-agent knowledge store that persists across sessions. Every agent has an isolated vault — a structured set of markdown files on disk, additionally tracked in the database for metadata and search indexing.

### Vault Structure

```
vaults/{agent_id}/
├── index.md                    # Auto-maintained table of contents
├── identity.md                 # NEVER compressed
├── soul.md                     # NEVER compressed
├── connections.md              # NEVER compressed
├── working/
│   ├── session_{timestamp}.md  # One per session
│   └── scratchpad.md           # Cleared at session end
├── episodic/
│   └── {year}-W{week}.md       # Compressed past sessions, one file per ISO week
└── semantic/
    ├── learned.md              # Extracted facts and user preferences
    ├── patterns.md             # Recurring patterns (threshold: 3+ occurrences)
    └── skills_notes.md         # Notes on installed skills and their outcomes
```

### Memory Types

| Type | Scope | Written by |
|---|---|---|
| **Working** | Current session | Agent (during task) |
| **Episodic** | All past sessions, compressed | Compression process |
| **Semantic** | Persistent structured knowledge | Reflection loop |

### Compression

Triggered when `working/` exceeds `compression_threshold` (default: 40,000 tokens). Uses a configurable self-hosted LLM (default: `ollama/llama3.2`). Cloud providers intentionally excluded to protect vault contents.

6-step process: read working session → create compact summary → update `semantic/learned.md` → append to `episodic/{week}.md` → delete original working file → update `index.md`.

### Optional: Vector Search

Qdrant integration for semantic search across vault content. When enabled, every vault write is embedded and upserted into a per-agent Qdrant collection. Disabled by default.

### Obsidian Compatibility

All vault files use valid YAML frontmatter, ISO 8601 dates, and standard wikilinks (`[[file-name]]`). Point Obsidian at `vaults/{agent_id}/` to browse as a standard knowledge base.

---

## Self-Learning Loop

**Spec**: [`/docs/self-learning-loop.md`](/docs/self-learning-loop.md)  
**Priority**: High

Agents improve over time by reflecting on completed work. The loop runs asynchronously after task completion or session close — it never blocks the user.

### Rate Limits

- Max 1 learn-cycle per agent per hour
- Max 3 pattern updates per agent per day
- State tracked in Redis with TTL

### 5-Step Learning Flow

1. **REFLECT** — Agent reads the completed session, produces a structured reflection: what went well, what went badly, what is new.
2. **UPDATE MEMORY** — Insights from "what is new" are appended to `semantic/learned.md` with date stamps. Duplicate facts are skipped via similarity check.
3. **UPDATE PATTERNS** — Reads last N reflections looking for recurring friction points. A pattern is written to `semantic/patterns.md` only after `pattern_threshold` (default: 3) separate observations.
4. **SKILL CREATION** (optional, user consent required) — When a pattern exceeds `skill_suggestion_threshold` (default: 5), agent drafts a skill file and sends a notification to the user. User must explicitly approve before the skill is written to disk. Publishing to skills.sh is always a separate manual action.
5. **SOUL UPDATE** (disabled by default) — When `update_soul: true`, agent may propose changes to `soul.md` after detecting consistent behavioral divergence. `soul.md` is **never auto-updated** — explicit user approval always required.

### Config (`config.yml`)

```yaml
learning:
  enabled: true
  reflect_after_tasks: true
  reflect_after_conversations: true
  update_soul: false
  pattern_threshold: 3
  skill_suggestion_enabled: true
```
