# Agent Vault Specification

**Component:** Per-Agent Knowledge Vault  
**Format:** Obsidian-compatible Markdown  
**Version:** 0.2.0
**Status:** Authoritative design document

---

## 1. Purpose

Each agent in the platform maintains a personal, structured knowledge store called its **vault**. The vault is the agent's long-term memory: it accumulates identity, experiences, learned patterns, and project context across sessions. It is the single source of truth for anything the agent "knows" about itself and its past.

Key design goals:

- **Human-readable at rest.** All vault content is plain Markdown that a human can open, read, and edit in any text editor or Obsidian.
- **Obsidian-compatible.** Frontmatter follows YAML convention; wikilinks (`[[Note Name]]`) are valid for cross-referencing within the same agent vault.
- **Append-friendly.** New information is appended or merged into existing files, not overwritten wholesale.
- **Selectively compressed.** High-churn memory sections are periodically summarised by an LLM; identity and relationship files are never touched by the compressor.
- **Backend-synced.** Every significant vault write is reflected in the `system_logs` table under the `MEMORY` category so that events are queryable without opening files.
- **Path-safe.** Every logical vault path is resolved inside the configured vault root before any read or write occurs.

---

## 2. Folder Structure

The vault root lives inside the backend's configured data directory. The layout below is canonical. No files or folders outside this structure are created by the platform; additional files placed here by a human operator are ignored by the backend.

```
vault/
└── agents/
    └── [agent-id]/
        ├── identity.md           ← NEVER compressed
        ├── soul.md               ← NEVER compressed
        ├── connections.md        ← NEVER compressed
        ├── memory/
        │   ├── working/          ← Current session — always uncompressed
        │   │   └── [session-id].md
        │   ├── episodic/         ← Past sessions — compressed after archival
        │   │   ├── 2026-W19.md
        │   │   └── index.md
        │   └── semantic/
        │       ├── patterns.md
        │       ├── learned.md
        │       └── skills-notes.md
        └── projects/
            └── [project-id]/
                └── context.md
```

`[agent-id]` is the text primary key from the `agents` table (e.g. `main-agent`).  
`[session-id]` is the UUID from the `sessions` table.  
`[project-id]` is the UUID from the `projects` table.

The backend rejects any resolved path that escapes the configured vault root after normalization. This applies to all user-provided agent IDs, session IDs, project IDs, filenames, archive paths, and search result paths.

### 2.1 File Descriptions

| File | Purpose | Compression |
|---|---|---|
| `identity.md` | Who the agent is: name, description, core directives, persona | Never |
| `soul.md` | Values, communication style, principles, ethical boundaries | Never |
| `connections.md` | Relationships with other agents, users, teams | Never |
| `memory/working/[session-id].md` | Live context for the active session | Never (while active) |
| `memory/episodic/[year]-W[week].md` | Weekly compressed summary of past sessions | Compressor output |
| `memory/episodic/index.md` | Chronological index of all episodic entries | Appended only |
| `memory/semantic/patterns.md` | Recurring behavioral and interaction patterns | Soft-compressed |
| `memory/semantic/learned.md` | Facts, preferences, and knowledge acquired over time | Soft-compressed |
| `memory/semantic/skills-notes.md` | Notes on skills: usage, quirks, discovered behaviours | Soft-compressed |
| `projects/[project-id]/context.md` | Project-specific briefings, decisions, open questions | Soft-compressed |

**Soft-compressed** means the compressor may merge and summarise content within the file but must preserve the file itself and retain all section headings.

---

## 3. File Format Rules

### 3.1 Frontmatter

Every vault file begins with a YAML frontmatter block. The fields listed below are required for the file type indicated; additional fields are allowed.

**All files:**
```yaml
---
agent_id: main-agent
file_type: identity | soul | connections | working | episodic | semantic | project-context
created_at: 2026-05-08T00:00:00Z    # ISO 8601, UTC
updated_at: 2026-05-08T12:34:56Z    # ISO 8601, UTC, updated on every write
version: 1                           # Integer, incremented on every write
---
```

**Working memory files additionally:**
```yaml
session_id: 550e8400-e29b-41d4-a716-446655440000
session_started_at: 2026-05-08T09:00:00Z
status: active | closed
```

**Episodic files additionally:**
```yaml
week: 2026-W19
compressed_at: 2026-05-12T02:00:00Z
source_sessions: [session-uuid-1, session-uuid-2]
compressor_model: llama3.2
token_count_before: 38400
token_count_after: 6200
```

### 3.2 Date and Time

- All timestamps are ISO 8601 with explicit UTC offset (`Z` or `+00:00`).
- No locale-specific date formats.
- Week identifiers use ISO 8601 week notation: `YYYY-Www` (e.g. `2026-W19`).

### 3.3 Wikilinks

Cross-references within the same agent vault may use Obsidian wikilink syntax:

```
See also: [[connections]] for the relationship with the project manager.
```

Links that cross agent vaults are not supported and will not resolve in Obsidian.

### 3.4 No Hardcoded Platform Name

Vault files must not embed the name of the platform. Use neutral language such as "this system", "the platform", or "the agent runtime". This ensures vault files remain portable and environment-agnostic.

### 3.5 Section Headings

- Use ATX headings (`#`, `##`, `###`).
- Top-level heading (`#`) states the document title.
- Semantic files must retain their section headings across compression cycles; the compressor is not permitted to drop headings.

---

## 4. Protected Files — Never Compressed

The following three files are **absolutely protected** from the compression pipeline, rate limiting, and any automated rewrite process:

| File | Reason |
|---|---|
| `identity.md` | Contains the agent's core identity directives. Compression could alter the agent's self-model in ways that break consistency across sessions. |
| `soul.md` | Contains values and ethical boundaries. Lossy summarisation of ethical constraints is unacceptable. |
| `connections.md` | Contains relationship data that must remain precise. Summarised relationships lose nuance and can cause incorrect social reasoning. |

Any code path that touches the compression pipeline must explicitly check that the target file is not one of these three before proceeding. The check is enforced by the `VaultCompressionService` in the backend, which maintains a `NEVER_COMPRESS` constant set containing the file names.

---

## 5. Compression Mechanism

### 5.1 Trigger Conditions

Compression is triggered automatically when either of the following conditions is met:

1. **Token threshold:** The estimated token count of `memory/working/[session-id].md` reaches or exceeds `compression_threshold_tokens` (default: **40,000 tokens**). Token count is estimated using the provider's tokenizer or, if unavailable, at 4 characters per token.
2. **Session close:** When a session transitions to `closed` status, its working memory file is scheduled for compression regardless of size.

Manual trigger is also available via the admin API (see Section 7).

### 5.2 Compression Provider

The compression LLM is configurable and independent of the agent's primary model. Configuration is stored in the agent's `config` JSONB in the `agents` table under the key `memory.compressor`:

```json
{
  "memory": {
    "vault_enabled": true,
    "compression_threshold_tokens": 40000,
    "compressor": {
      "provider": "ollama",
      "model": "llama3.2",
      "base_url": "http://localhost:11434",
      "temperature": 0.3,
      "max_tokens": 4096
    }
  }
}
```

If `compressor` is not specified, the platform falls back to the first available self-hosted model registered in `installed_models` where `is_self_hosted = true`. If no self-hosted model is available, compression is deferred and a `WARN` event is written to `system_logs`.

The compression provider must be a **registered model** in the `installed_models` table. External API providers (e.g. Anthropic, OpenAI) may be used only if the operator explicitly configures them; the default intentionally favours local providers to keep memory processing private and cost-free.

### 5.3 Six-Step Compression Process

When compression is triggered for a file (e.g. a working memory file at session close):

**Step 1 — Read and Tokenise**  
The backend reads the target file from disk. It extracts the frontmatter, parses section headings, and calculates the current token count. If the file is in the `NEVER_COMPRESS` set, the process is aborted and logged.

**Step 2 — Select Target**  
The system determines the output file. For session-close compression, the target is the current week's episodic file (`memory/episodic/[year]-Www.md`). If the file does not exist, it is created from the episodic template with appropriate frontmatter.

**Step 3 — Build Compression Prompt**  
A system prompt is constructed instructing the compressor to:
- Summarise the content faithfully, preserving all decisions, facts, named entities, and action items.
- Retain all section headings from the source document.
- Discard conversational filler, repetition, and transient observations.
- Output only the summarised Markdown body, without frontmatter (the backend handles frontmatter separately).
- Not invent, infer, or hallucinate any information not present in the source.

**Step 4 — Invoke Compressor**  
The compression prompt and source file body are sent to the configured compressor model. The request uses a low temperature (0.3 by default) to maximise determinism. The response is streamed and buffered.

**Step 5 — Merge into Target**  
The compressor output is appended to the target episodic file under a dated section heading (e.g. `## 2026-05-08`). The target file's `updated_at` and `version` frontmatter fields are incremented. The episodic `index.md` is updated to record the new entry.

**Step 6 — Archive and Clean Up**  
The source working memory file's frontmatter `status` is set to `closed`. The file is moved to `memory/episodic/archive/[session-id].md.gz` (gzip-compressed) if the `archive_raw_sessions` option is enabled; otherwise it is deleted. A `MEMORY` log event is written to `system_logs` with `event = 'session.compressed'` and a payload including source path, target path, token counts before and after, compressor model, and duration.

### 5.4 Rate Limiting

To prevent compression storms (e.g. many agents closing sessions simultaneously):

- Compression jobs are placed on an internal queue with a **maximum concurrency of 2** parallel compression invocations per platform instance.
- A single agent may not have more than **1 active compression job** at a time. If a second trigger fires while compression is in progress, it is queued and deduplicated (the queued job picks up the latest file state when it eventually runs).
- There is a **minimum interval of 60 seconds** between consecutive compression jobs for the same agent.
- If the compressor model is unavailable (e.g. Ollama is down), the job is retried with exponential backoff: 30 s, 2 min, 10 min, 1 h. After 4 failed attempts, a `WARN` event is logged and the job is dropped from the queue.

### 5.5 Failure Handling

Compression is never allowed to destroy source memory when the target write fails. The backend follows these rules:

- If compressor invocation fails, the working file remains unchanged and a retry is scheduled.
- If target episodic write fails, the working file remains in place with `status: closed` and the job is marked retryable.
- If archive creation fails after a successful target write, the working file remains in place and `session.compression_archive_failed` is logged at `WARN`.
- If frontmatter parsing fails, compression is skipped and `session.compression_failed` is logged with the parse error.
- Operators may manually requeue compression after correcting the file.

---

## 6. Vector Search (Optional)

The vault supports optional semantic search via **Qdrant**, a self-hosted vector database.

### 6.1 Configuration

Qdrant integration is enabled by setting the `vector_search` key in `system_metadata.settings`:

```json
{
  "vector_search": {
    "enabled": true,
    "qdrant_url": "http://localhost:6333",
    "collection_prefix": "vault_",
    "embedding_provider": "ollama",
    "embedding_model": "nomic-embed-text",
    "embedding_base_url": "http://localhost:11434",
    "chunk_size_tokens": 512,
    "chunk_overlap_tokens": 64
  }
}
```

### 6.2 Indexing Behaviour

When vector search is enabled, every vault file write triggers an async embedding job:

1. The file body (excluding frontmatter) is split into overlapping chunks of `chunk_size_tokens` tokens with `chunk_overlap_tokens` overlap.
2. Each chunk is embedded using the configured embedding model.
3. Chunks are upserted into Qdrant under a collection named `vault_[agent-id]`.
4. Each Qdrant point stores the following payload alongside the vector: `agent_id`, `file_path`, `file_type`, `chunk_index`, `updated_at`.

### 6.3 Query API

The backend exposes an internal service method `VaultSearchService.search(agentId, query, topK)` that:
- Embeds `query` using the same embedding model.
- Queries Qdrant for the top-K nearest chunks from the agent's collection.
- Returns ranked results with file path, chunk text, and similarity score.
- Falls back to PostgreSQL `ILIKE` full-text search if Qdrant is unavailable.

Vector search is never mandatory. All platform features function correctly without it; it only improves context retrieval quality.

---

## 7. Backend API — Reading and Writing the Vault

### 7.1 File System Layer

All vault reads and writes go through the `VaultFileService` class. No other service or controller may access vault files directly (i.e. no raw `java.io.File` or `java.nio.Path` usage outside `VaultFileService`).

`VaultFileService` responsibilities:
- Resolve the absolute path for any logical vault reference (agent ID + file type + optional sub-key).
- Parse and serialise frontmatter.
- Increment `version` and update `updated_at` on every write.
- Acquire a per-file write lock before writing to prevent concurrent modification.
- Emit a `MEMORY` log entry to `system_logs` for every create, update, or delete operation.

### 7.2 Database Sync

The vault does not store content in PostgreSQL; the file system is the source of truth for vault content. The database is used for:

- **Indexing:** The `sessions` table tracks active session IDs, which map to working memory files.
- **Cost tracking:** Compression invocations are recorded in `agent_costs` (provider = compressor provider, tokens counted for the compression call).
- **Event logging:** All vault events go to `system_logs` (see Section 8).
- **Search fallback:** When Qdrant is not available, `system_logs` payload data provides a partial fallback for event-based queries.

On startup, `VaultFileService` performs a **reconciliation scan**: it walks the vault directory tree and verifies that every file it finds corresponds to a known agent in the `agents` table. Orphaned directories (agent ID not found in DB) are logged at `WARN` level but not deleted automatically.

### 7.3 Key Operations

| Operation | Method | Behaviour |
|---|---|---|
| Open session | `openWorkingMemory(agentId, sessionId)` | Creates `memory/working/[sessionId].md` with initial frontmatter and blank body. |
| Append to session | `appendToWorkingMemory(agentId, sessionId, content)` | Appends markdown block to working file; updates `updated_at`. |
| Close session | `closeSession(agentId, sessionId)` | Marks file as `closed`; enqueues compression job. |
| Read identity | `readIdentity(agentId)` | Returns parsed frontmatter + body of `identity.md`. |
| Update connections | `updateConnections(agentId, content)` | Replaces body of `connections.md`; never compresses. |
| Read project context | `readProjectContext(agentId, projectId)` | Returns `projects/[projectId]/context.md` or empty if not found. |
| Write project context | `writeProjectContext(agentId, projectId, content)` | Upserts `projects/[projectId]/context.md`. |
| Trigger compression | `triggerCompression(agentId, sourceFilePath)` | Manually enqueues a compression job, bypassing token threshold check. |
| Search vault | `search(agentId, query, topK)` | Delegates to `VaultSearchService`; returns ranked chunks. |

### 7.4 Agent Context Assembly

When the runtime prepares the context window for an agent call, it assembles vault content in this priority order:

1. `identity.md` body (always included, no truncation)
2. `soul.md` body (always included, no truncation)
3. Current working memory file body (included if token budget allows)
4. Relevant episodic summaries (included based on semantic search or recency)
5. Relevant semantic memory sections (included based on semantic search)
6. Project context for the active project (included if a project is active)
7. `connections.md` body (included if remaining token budget allows)

Steps 3–7 are subject to a configurable token budget (`max_vault_context_tokens`, default 8,000) to avoid consuming the entire context window with memory.

---

## 8. Logging — Vault Events

All vault events are written to the `system_logs` table with `category = 'MEMORY'`. The `source` JSONB field always includes `{"component": "vault", "agent_id": "[agent-id]"}`.

### 8.1 Event Types

| `event` value | `level` | Description |
|---|---|---|
| `vault.file.created` | `INFO` | A new vault file was created. |
| `vault.file.updated` | `DEBUG` | An existing vault file was written to. |
| `vault.file.deleted` | `WARN` | A vault file was deleted (unusual; should be rare). |
| `session.opened` | `INFO` | A new working memory file was initialised for a session. |
| `session.closed` | `INFO` | A session working memory file was marked closed and compression enqueued. |
| `session.compressed` | `INFO` | Compression completed successfully. Payload includes before/after token counts. |
| `session.compression_deferred` | `WARN` | Compression could not run (no model available). |
| `session.compression_failed` | `ERROR` | Compression failed after all retries. |
| `session.compression_queued` | `DEBUG` | Compression job placed in queue. |
| `vault.search.query` | `DEBUG` | A semantic search was executed against the vault. |
| `vault.reconcile.orphan` | `WARN` | An orphaned agent vault directory was found with no matching DB record. |
| `vault.reconcile.complete` | `INFO` | Startup reconciliation scan completed. Payload includes counts. |

### 8.2 Payload Schema

Payloads are free-form JSONB but the following keys are standardised:

```json
{
  "file_path": "agents/main-agent/memory/working/session-uuid.md",
  "file_type": "working",
  "session_id": "uuid",
  "compressor_model": "ollama/llama3.2",
  "tokens_before": 38400,
  "tokens_after": 6200,
  "compression_ratio": 0.16,
  "duration_ms": 4321,
  "error": "optional error message string"
}
```

Not all fields are present in every event; include only the fields relevant to the specific event type.

---

## 9. Constraints and Invariants

The following invariants must be maintained at all times by any code that touches the vault:

1. `identity.md`, `soul.md`, and `connections.md` are never passed to the compression pipeline.
2. Every vault write increments the `version` field in the file's frontmatter.
3. The `updated_at` frontmatter field reflects wall-clock UTC time at the moment of write; it is never backdated.
4. A compression job never runs concurrently with another compression job for the same agent.
5. Working memory files are never deleted while their session's `status` in the `sessions` table is `active`.
6. No vault file contains the platform's name as a hardcoded string.
7. All paths handled by `VaultFileService` are validated against the vault root to prevent path traversal (reject any resolved path that does not begin with the configured vault root directory).
8. Vault writes are atomic at the file level: the backend writes to a temporary file in the same directory and renames it into place (`Files.move` with `ATOMIC_MOVE` option) to prevent partial reads.

---

## 10. Configuration Reference

All vault configuration lives in the agent's `config` JSONB (`agents.config`) or in `system_metadata.settings` for platform-wide defaults.

| Key | Location | Default | Description |
|---|---|---|---|
| `memory.vault_enabled` | agent config | `true` | Enable or disable the vault for this agent. |
| `memory.compression_threshold_tokens` | agent config | `40000` | Token count at which working memory is automatically compressed. |
| `memory.compressor.provider` | agent config | `ollama` | Provider for the compression model. |
| `memory.compressor.model` | agent config | `llama3.2` | Model name for compression. |
| `memory.compressor.base_url` | agent config | `http://localhost:11434` | Base URL for self-hosted provider. |
| `memory.compressor.temperature` | agent config | `0.3` | Temperature for compression calls. |
| `memory.compressor.max_tokens` | agent config | `4096` | Max output tokens for compression calls. |
| `memory.archive_raw_sessions` | agent config | `false` | Gzip and retain raw working memory files after compression. |
| `memory.max_vault_context_tokens` | agent config | `8000` | Maximum tokens of vault content injected into the context window. |
| `vector_search.enabled` | system settings | `false` | Enable Qdrant-backed semantic search. |
| `vector_search.qdrant_url` | system settings | — | Qdrant HTTP URL. Required if enabled. |
| `vector_search.embedding_provider` | system settings | `ollama` | Provider for embedding model. |
| `vector_search.embedding_model` | system settings | `nomic-embed-text` | Embedding model name. |
| `vector_search.chunk_size_tokens` | system settings | `512` | Chunk size for embedding. |
| `vector_search.chunk_overlap_tokens` | system settings | `64` | Overlap between consecutive chunks. |
