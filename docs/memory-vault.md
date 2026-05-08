# Memory Vault

## Purpose

The Memory Vault is a per-agent knowledge store that persists across sessions. Every agent in {SYSTEM_NAME} has its own isolated vault — a structured set of markdown files that the agent can read during tasks and that the system updates after sessions end.

The vault is designed to be **Obsidian-compatible**: all files use valid YAML frontmatter, ISO 8601 dates, and standard wikilinks (`[[file-name]]`). You can point Obsidian at the vault root and browse it as a standard knowledge base.

Vaults live on the filesystem under the configured storage path and are additionally tracked in the database for metadata and search indexing.

---

## Folder Structure

```
vaults/
└── {agent_id}/
    ├── index.md                    # Vault table of contents, auto-maintained
    ├── identity.md                 # Agent identity: name, role, purpose. NEVER compressed.
    ├── soul.md                     # Agent personality, values, tone. NEVER compressed.
    ├── connections.md              # Known users, relationships, preferences. NEVER compressed.
    │
    ├── working/                    # Current-session memory (hot)
    │   ├── session_{timestamp}.md  # One file per session, created on session start
    │   └── scratchpad.md           # Temporary notes within a session, cleared on end
    │
    ├── episodic/                   # Past sessions, compressed (cold)
    │   ├── 2025-W01.md             # One file per ISO calendar week
    │   ├── 2025-W02.md
    │   └── ...
    │
    └── semantic/                   # Extracted knowledge (structured)
        ├── learned.md              # Insights from reflections: facts, preferences, constraints
        ├── patterns.md             # Detected recurring patterns (written after 3+ occurrences)
        └── skills_notes.md         # Notes on installed skills, how they were used, outcomes
```

### File Descriptions

| File | Written by | Read by | Compressed? |
|---|---|---|---|
| `index.md` | System (auto) | Agent, system | No |
| `identity.md` | User/admin | Agent | **Never** |
| `soul.md` | User on approval | Agent | **Never** |
| `connections.md` | Agent, user | Agent | **Never** |
| `working/session_*.md` | Agent (during session) | Agent (same session) | Yes — triggers compression |
| `working/scratchpad.md` | Agent | Agent | Cleared, not compressed |
| `episodic/{week}.md` | Compression process | Agent (context retrieval) | Already compressed |
| `semantic/learned.md` | Reflection loop | Agent (every task) | No |
| `semantic/patterns.md` | Reflection loop | Agent (every task) | No |
| `semantic/skills_notes.md` | Skill events | Agent | No |

---

## Memory Types

### 1. Working Memory

**Scope:** Current session only.

Working memory is stored in `working/session_{timestamp}.md`. A new file is created when a session starts and the agent writes to it continuously during the session — task notes, intermediate results, user instructions given mid-session, and tool outputs.

When the working directory accumulates tokens beyond the configured `compression_threshold` (default: **40,000 tokens**), the compression process is triggered automatically.

Frontmatter example:

```yaml
---
type: working
session_id: sess_01HXY3Z...
agent_id: agent_finance_01
started_at: 2025-06-12T09:15:00Z
token_estimate: 12400
---
```

The `scratchpad.md` file is a lightweight scratch space for within-session reasoning that does not need to persist. It is cleared (not compressed) at session end.

### 2. Episodic Memory

**Scope:** Past sessions, permanently stored in compressed form.

After compression, session files move to `episodic/{week}.md` where `{week}` is the ISO week string (e.g., `2025-W24`). Each week file may contain multiple compressed session summaries, appended in chronological order with a horizontal rule separator.

Episodic memory is read-only from the agent's perspective during a task. {SYSTEM_NAME} includes recent episodic entries in the agent's context when starting a new session (configurable: `vault.episodic_context_weeks`, default 2 weeks).

Frontmatter of a week file:

```yaml
---
type: episodic
week: 2025-W24
agent_id: agent_finance_01
sessions_compressed: 7
last_updated: 2025-06-15T22:00:00Z
---
```

### 3. Semantic Memory

**Scope:** Persistent, structured knowledge extracted across all sessions.

Semantic memory contains the agent's lasting understanding of the world it operates in. It is written by the self-learning reflection loop and is read at the start of every task to provide persistent context without re-reading all episodic history.

- **`semantic/learned.md`** — Bullet-point list of discrete facts, user preferences, domain constraints, and lessons from past tasks. Each entry has a date stamp.
- **`semantic/patterns.md`** — Recurring behavioral patterns. A pattern is only written here after being observed at least 3 times (see self-learning-loop.md).
- **`semantic/skills_notes.md`** — Notes on installed skills: which were used, outcomes, edge cases discovered.

Example entry in `learned.md`:

```markdown
- [2025-06-10] User prefers concise bullet-point summaries over prose for financial reports.
- [2025-06-11] The project "Atlas" refers to the internal data pipeline, not the client project.
- [2025-06-14] Running SQL migrations requires the `--dry-run` flag first per team policy.
```

---

## Files That Are Never Compressed

Three files are **excluded from all compression operations**:

| File | Reason |
|---|---|
| `identity.md` | Defines who the agent is. Must never be summarized or altered by automated processes. |
| `soul.md` | Defines personality and values. Requires explicit user approval to change (see self-learning-loop.md). |
| `connections.md` | Relationship graph. Compressing would lose precision about individual users and their preferences. |

The compression process has a hardcoded exclusion list for these three paths. No configuration can override this.

---

## Compression

### Trigger

Compression is triggered when the total estimated token count of all files in `working/` exceeds `compression_threshold`. This threshold is configurable per-agent in the agent's `config.yml`:

```yaml
vault:
  compression_threshold: 40000   # tokens, default
  compression_provider: ollama/llama3.2
```

Token counts are estimated by the backend after each write to the working directory. The estimate uses a simple character-based approximation (characters / 4) sufficient for threshold comparison.

### Compression Provider

The compression LLM is configurable and can be any **self-hosted provider registered in {SYSTEM_NAME}**. The setting is:

**System Settings → Vault → Compression Provider**

Default: `ollama/llama3.2`

Any other registered self-hosted provider (Ollama with a different model, a local vLLM instance, etc.) can be selected. Cloud providers (Anthropic, OpenAI, etc.) are intentionally excluded from this list to avoid sending vault contents to external services unless the operator explicitly configures otherwise. The available choices in the UI are populated from the list of self-hosted providers in the ACP Registry.

### Compression Process (6 Steps)

1. **Read working session**
   The backend reads the current `working/session_{timestamp}.md` file in full. The file is passed to the compression LLM with a system prompt instructing it to produce a concise summary.

2. **Create compact summary**
   The LLM generates a summary of **maximum 2,000 tokens**. The summary preserves: key decisions made, tasks completed, facts learned, user instructions given, and open items. Conversational filler is discarded.

3. **Update `semantic/learned.md`**
   The LLM (or a second pass) extracts discrete facts from the session that belong in long-term semantic memory. These are appended to `semantic/learned.md` with the current date. Duplicates are not added if a semantically identical fact already exists.

4. **Append to `episodic/{week}.md`**
   The compact summary is appended to the current ISO week's episodic file. If the file does not exist, it is created with the appropriate frontmatter. The summary is preceded by a session header:

   ```markdown
   ## Session: 2025-06-12T09:15:00Z

   {compressed summary text}

   ---
   ```

5. **Delete original working file**
   The original `working/session_{timestamp}.md` is deleted from the filesystem. The database record is updated to mark it as `state: compressed` and stores the episodic file it was moved into.

6. **Update `index.md`**
   The vault's `index.md` is regenerated to reflect the current state of all vault files. It lists the most recent episodic entries, a summary of what is in `semantic/learned.md` (entry count, last updated), and a link to the current active working session if one is open.

### Compression Logging

All compression events are logged under the `MEMORY` category:

```
[MEMORY] compression-start   agent_id=... session_file=... token_estimate=...
[MEMORY] compression-llm-call provider=ollama/llama3.2 input_tokens=... 
[MEMORY] semantic-update     agent_id=... facts_added=...
[MEMORY] episodic-append     agent_id=... week=2025-W24
[MEMORY] working-deleted     agent_id=... session_file=...
[MEMORY] index-updated       agent_id=...
[MEMORY] compression-end     agent_id=... duration_ms=...
```

---

## Vector Search (Optional)

{SYSTEM_NAME} supports semantic search across vault content using **Qdrant** as the vector store.

**Enable in:** System Settings → Vault → Vector Search → Enable Qdrant

When enabled:
- After each write to the vault (working, semantic, episodic), the backend embeds the changed content using the configured embedding model and upserts into a per-agent Qdrant collection (`vault_{agent_id}`).
- Agents can issue semantic queries against their own vault at task start to retrieve the most relevant past context, rather than relying solely on recency-based episodic loading.
- The embedding model is configurable separately from the compression provider (System Settings → Vault → Embedding Model).

When disabled (default):
- Vault retrieval is purely file-based and recency-based.
- No Qdrant connection is required.

---

## Obsidian Compatibility

All vault files are valid Obsidian markdown:

- **Frontmatter:** Every file starts with a YAML frontmatter block delimited by `---`. All dates are ISO 8601 (`2025-06-12T09:15:00Z` or `2025-06-12`).
- **Wikilinks:** Cross-references between vault files use Obsidian-style wikilinks: `[[semantic/learned]]`, `[[identity]]`. The backend resolves these links when building agent context.
- **Headings:** Files use standard ATX headings (`#`, `##`, `###`). The compression LLM is instructed to output only standard markdown.
- **No proprietary syntax:** No Obsidian-specific plugins, queries, or Dataview syntax are generated by the system. The vault remains readable in any markdown editor.

To browse a vault in Obsidian: open the `vaults/{agent_id}/` folder as an Obsidian vault. All files will be readable and navigable.

---

## Backend API — Read/Write Behavior

### Filesystem

Vault files are stored on the filesystem at the path configured in `application.yml`:

```yaml
synapse:
  vault:
    storage_path: /data/vaults
```

Each agent vault lives at `{storage_path}/{agent_id}/`. The backend performs all reads and writes directly to the filesystem using standard Java NIO. File writes are atomic (write to temp file, then rename).

### Database Sync

A `vault_files` table tracks metadata for every vault file:

```sql
CREATE TABLE vault_files (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id        UUID NOT NULL REFERENCES agents(id),
    file_path       TEXT NOT NULL,          -- relative to vault root
    file_type       TEXT NOT NULL,          -- working | episodic | semantic | identity | soul | connections
    state           TEXT NOT NULL DEFAULT 'active',  -- active | compressed | deleted
    token_estimate  INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    compressed_into TEXT                    -- episodic file path, if compressed
);
```

The database is the source of truth for metadata (token estimates, state, compression history). The filesystem is the source of truth for content. On startup, {SYSTEM_NAME} reconciles the two: files present on disk but not in the DB are registered; DB records for files missing on disk are marked `state: deleted`.

### Agent Read Access

During a task, the agent has read access to:
- Its own `identity.md`, `soul.md`, `connections.md`
- Its own `semantic/learned.md`, `semantic/patterns.md`, `semantic/skills_notes.md`
- Its own active `working/session_{timestamp}.md`
- Recent episodic files (controlled by `vault.episodic_context_weeks`)

The agent context is assembled by the `VaultContextBuilder` service, which reads these files and injects their content into the system prompt at task start.

### Agent Write Access

During a task, the agent can append to:
- Its own `working/session_{timestamp}.md` (via tool call or structured output)
- Its own `working/scratchpad.md`

The agent cannot directly write to `semantic/` or `episodic/` during a task. Those writes happen only through the compression/reflection loop after the session ends.

---

## Security

- **Isolation:** Each vault lives in a directory owned by the agent. The backend enforces agent-scoped path validation on all reads and writes. A request to read `vaults/agent_b/...` from agent A's session is rejected with a 403.
- **User access:** A user can read their own agents' vaults through the {SYSTEM_NAME} UI. They cannot read another user's agents' vaults without explicit permission being granted by that user or an admin.
- **Admin access:** Admins can access any vault for debugging purposes. All admin vault reads are logged.
- **No cross-agent reads:** Agents cannot read other agents' vaults. Collaboration between agents, if needed, is handled through explicit message-passing, not shared vault access.
- **Encryption at rest:** Vault files can be encrypted at rest if the storage backend supports it (e.g., encrypted filesystem, or encrypted S3 bucket). {SYSTEM_NAME} does not perform application-level encryption of vault content in the current version.

---

## Logging Reference

All vault operations log to the `MEMORY` category. Implementors must ensure every vault read and write produces a log entry.

| Event | Fields |
|---|---|
| `vault-read` | `agent_id`, `file_path`, `bytes_read` |
| `vault-write` | `agent_id`, `file_path`, `bytes_written` |
| `vault-delete` | `agent_id`, `file_path` |
| `compression-start` | `agent_id`, `session_file`, `token_estimate` |
| `compression-llm-call` | `agent_id`, `provider`, `input_tokens`, `output_tokens` |
| `semantic-update` | `agent_id`, `facts_added` |
| `episodic-append` | `agent_id`, `week`, `session_file` |
| `working-deleted` | `agent_id`, `session_file` |
| `index-updated` | `agent_id` |
| `compression-end` | `agent_id`, `duration_ms`, `success` |
| `vector-upsert` | `agent_id`, `file_path`, `chunks` | *(only if Qdrant enabled)* |

Log format follows the {SYSTEM_NAME} standard structured log format:

```
2025-06-12T09:31:44Z [MEMORY] compression-start agent_id=agent_finance_01 session_file=working/session_1749720400.md token_estimate=41230
```
