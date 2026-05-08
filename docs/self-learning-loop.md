# Self-Learning Loop

## Purpose

The self-learning loop allows agents to improve their performance over time by reflecting on completed work. After each task or conversation ends, the agent reads what happened, extracts insights, and updates its knowledge vault. Over time, this produces an agent that understands recurring patterns in its domain, retains user preferences without being told repeatedly, and optionally turns reusable workflows into shareable skills.

Learning is agent-specific, non-destructive, and governed by strict rate limits and user consent rules.

---

## When the Loop Runs

The learning loop is triggered by two events:

1. **Task completion** — a discrete task finishes (success or failure). Triggered by the `TaskCompletedEvent`.
2. **Conversation end** — the user closes or idles out of a session (> 10 minutes idle). Triggered by `SessionClosedEvent`.

If both events fire for the same session within a short window (configurable: `learning.dedup_window_seconds`, default 60), only one learn-cycle runs.

The loop runs **asynchronously** after the triggering event. It does not block the user from starting a new session.

### Rate Limiting

To prevent excessive processing and LLM costs:

- **Maximum 1 learn-cycle per agent per hour.** If the agent completed 5 tasks in one hour, only the first triggers a full cycle. Subsequent triggers are queued and processed after the cooldown expires.
- **Maximum 3 pattern updates per agent per day.** Pattern detection is more expensive (reads multiple past sessions). After 3 pattern-update runs in a day, pattern detection is skipped until midnight UTC.

Rate limit state is stored in Redis:

```
synapse:learning:cooldown:{agent_id}          → TTL 3600s
synapse:learning:pattern_count:{agent_id}     → TTL until midnight UTC
```

---

## Configuration

Learning is configured per-agent in the agent's `config.yml`:

```yaml
learning:
  enabled: true                    # Master switch. Default: true.
  reflect_after_tasks: true        # Run reflect step after task completion. Default: true.
  reflect_after_conversations: true  # Run reflect step after session close. Default: true.
  update_soul: false               # Whether the agent may propose soul.md changes. Default: false.
  pattern_threshold: 3             # How many times a pattern must appear before being recorded. Default: 3.
  skill_suggestion_enabled: true   # Whether the agent may ask the user to create a skill. Default: true.
```

When `learning.enabled: false`, the agent continues to function normally. It receives tasks and produces results as usual. The only difference is that no reflect/update cycle runs after sessions end. Vault content from earlier (when learning was enabled) is preserved and still read.

---

## The 5-Step Learning Flow

### Step 1: REFLECT

The agent reads the session or task that just ended. This is the working memory file for the session (`working/session_{timestamp}.md`) plus any tool outputs and structured results that were produced.

The agent (using the same LLM provider configured for the agent's normal tasks) produces a structured reflection in three sections:

```markdown
## Reflection: 2025-06-12T09:31:00Z

### What went well
- The SQL query optimization approach worked on first attempt.
- User confirmed the output format was correct immediately.

### What went badly
- First tool call to the external API returned a 429. Retry logic was not present.
- Estimated completion time was too optimistic (said 5 min, took 20 min).

### What is new
- User clarified that "report" in this context always means the monthly finance PDF.
- The staging database has a 30-second query timeout, not 60 seconds.
```

This reflection is written to `working/scratchpad.md` temporarily during the cycle. It is not persisted after the cycle completes.

**Logged events:**

```
[LEARNING] reflect-start  agent_id=... session_file=... 
[LEARNING] reflect-end    agent_id=... duration_ms=...
```

---

### Step 2: UPDATE MEMORY

The agent writes the "What is new" section's insights into `semantic/learned.md`. Each insight becomes a bullet with a date stamp:

```markdown
- [2025-06-12] "Report" in the finance context means the monthly PDF, not any ad-hoc summary.
- [2025-06-12] The staging DB query timeout is 30 seconds (not 60).
```

Before writing, the backend checks whether a semantically equivalent fact is already in `learned.md` (simple string similarity check; not an LLM call). If a fact is effectively identical to an existing one, it is skipped to avoid duplication.

After writing to `semantic/learned.md`, the compression threshold check runs. If `working/` now exceeds `compression_threshold` tokens, the compression process (documented in memory-vault.md) is triggered as a sub-step here.

**Logged events:**

```
[LEARNING] memory-update  agent_id=... facts_added=2 facts_skipped=0
```

---

### Step 3: UPDATE PATTERNS

Pattern detection runs only if the daily rate limit has not been reached (`learning.pattern_count < 3`).

The pattern detection routine reads the last N reflections (N = `learning.pattern_window`, default 10) and the existing `semantic/patterns.md`. It looks for recurring entries across the "What went badly" sections specifically, because those represent repeated friction points — the most valuable candidates for pattern recognition.

**Detection rule:** A pattern is written to `semantic/patterns.md` only after it has been observed in at least `pattern_threshold` separate reflections (default: **3**). The counter is maintained in the vault's metadata table, not in the file itself.

When a new pattern is confirmed:

```markdown
## Pattern: External API Rate Limiting

**First seen:** 2025-06-01  
**Occurrences:** 3  
**Last seen:** 2025-06-12  

When calling external APIs without retry logic, 429 errors occur on the first call during peak hours.

**Resolution applied so far:** Manual retry after delay.  
**Suggested action:** Add exponential backoff to all external API tool calls.
```

**Logged events:**

```
[LEARNING] pattern-check   agent_id=... reflections_read=10
[LEARNING] pattern-found   agent_id=... pattern_id=... occurrences=3
```

---

### Step 4: SKILL CREATION (Optional)

Skill creation is optional and requires explicit user consent. It is never automatic.

**Trigger condition:** A pattern in `semantic/patterns.md` has been seen frequently enough (configurable: `learning.skill_suggestion_threshold`, default: 5 occurrences) AND `skill_suggestion_enabled: true` in the agent config.

**Flow:**

1. The agent detects a high-frequency pattern that has no existing skill associated with it.
2. The agent creates a **draft skill file** in memory (not written to disk yet):

   ```markdown
   ---
   id: auto-api-retry
   name: Automatic API Retry with Backoff
   version: 0.1.0
   author: agent_finance_01 (draft)
   description: Adds exponential backoff retry to external API calls that return 429.
   tags: [api, reliability, retry]
   ---

   ## Trigger
   When a tool call to an external API returns HTTP 429 or a rate-limit error.

   ## Instructions
   1. Wait for the initial backoff period: 2 seconds.
   2. Retry the same call. If it fails again, double the wait (4s, 8s, 16s).
   3. After 4 retries, report failure to the user with the error message.
   4. Log each retry attempt.

   ## Examples
   - Tool call to payment API returns 429 → wait 2s → retry → success.

   ## Limitations
   - Does not apply to authentication errors (401, 403).
   - Max 4 retries before giving up.
   ```

3. The system sends the user a **notification** (in-app, not email) with the message:

   > "I've noticed a recurring pattern: {pattern name}. I've drafted a skill to handle it automatically. Would you like me to create it?  
   > [View draft] [Yes, create it] [No, don't create]"

4. **If the user selects "Yes, create it":** The draft skill file is written to `plugins/skills/{skill-id}.md`. The skill is loaded into {SYSTEM_NAME} on next plugin reload or immediately if hot-reload is enabled.

5. **If the user selects "No, don't create":** The draft is discarded. The pattern remains in `patterns.md` but is flagged `skill_declined: true` so the suggestion is not repeated.

6. **If the user ignores the notification:** No action is taken. The suggestion is re-issued after the next `learning.skill_suggestion_cooldown_days` days (default: 7).

**Publishing is always separate.** Creating the skill file locally does not publish it anywhere. Publishing requires the `/skills publish` command (see skills-integration.md).

**Logged events:**

```
[LEARNING] skill-draft-created  agent_id=... skill_id=... pattern_id=...
[LEARNING] skill-created        agent_id=... skill_id=... approved_by=user:{user_id}
[LEARNING] skill-declined       agent_id=... skill_id=... user_id=...
```

---

### Step 5: SOUL UPDATE

`soul.md` defines the agent's personality, values, and tone. It is the most sensitive file in the vault.

**Default behavior:** `update_soul: false` in config. The agent never proposes changes to `soul.md` unless this is explicitly enabled.

**When `update_soul: true`:**

If the agent's reflection reveals a strong consistent pattern in how it communicates or approaches tasks that differs from what `soul.md` currently describes, the agent may propose an update.

The proposal is always user-facing:

> "Based on recent sessions, I've noticed that I consistently take a more step-by-step explanatory approach than my current persona describes. Would you like me to update my soul.md to reflect this?  
> [View proposed change] [Yes, update] [No, keep as is]"

The "View proposed change" shows a diff between the current `soul.md` and the proposed version.

**`soul.md` is NEVER auto-updated.** Even with `update_soul: true`, the file is only written after explicit user approval. The approval stores a record in the `soul_update_log` table:

```sql
CREATE TABLE soul_update_log (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id     UUID NOT NULL REFERENCES agents(id),
    proposed_at  TIMESTAMPTZ NOT NULL,
    approved_at  TIMESTAMPTZ,
    approved_by  UUID REFERENCES users(id),
    diff         TEXT NOT NULL,
    applied      BOOLEAN NOT NULL DEFAULT false
);
```

**Logged events:**

```
[LEARNING] soul-update-requested  agent_id=... proposed_diff_chars=...
[LEARNING] soul-update-approved   agent_id=... approved_by=user:{user_id}
[LEARNING] soul-update-rejected   agent_id=... user_id=...
[LEARNING] soul-update-applied    agent_id=...
```

---

## Skill Publishing Flow

Creating a skill locally (Step 4) and publishing it to the skills.sh API are two entirely separate actions. Publishing is **always manual** and **always requires user action**.

### `/skills publish` Command

The user (or the agent, if instructed by the user) runs:

```
/skills publish {skill-id}
```

**Step-by-step flow:**

1. {SYSTEM_NAME} reads `plugins/skills/{skill-id}.md` and validates the frontmatter (id, name, version, author, description are all present and non-empty).
2. The skill content is displayed to the user in a preview panel:
   - Frontmatter fields rendered as a table
   - Skill body rendered as markdown
3. A confirmation prompt is shown:
   > "You are about to publish **{skill name}** (v{version}) to skills.sh. This will make it publicly available. Continue?"
   > [Publish] [Cancel]
4. On confirmation, {SYSTEM_NAME} calls the skills.sh API:
   - `POST https://skills.sh/api/v1/skills`
   - Auth: the skills.sh API token configured in System Settings → Integrations → skills.sh
   - Body: the skill markdown content plus metadata
5. On success, the user receives:
   > "Skill published. Available at: https://skills.sh/skills/{skill-id}"
   The local skill file is updated with `published: true` and `published_at: {timestamp}` in its frontmatter.
6. On failure, the error from skills.sh is displayed verbatim and no changes are made to the local file.

**There is no automatic publish path.** No background job, no scheduler, no auto-publish on skill creation. The only way a skill reaches skills.sh is the sequence above with explicit user confirmation at step 3.

---

## When Learning is Disabled

When `learning.enabled: false`:

- The agent processes all tasks normally.
- No `TaskCompletedEvent` listener runs the learning loop.
- No `SessionClosedEvent` listener runs the learning loop.
- Vault content written during earlier enabled periods is preserved and still injected into context at task start.
- Pattern counts and rate limit keys in Redis are not written (they are only set by the learning loop).
- The agent will not propose skill creation or soul updates.

Re-enabling learning (`learning.enabled: true`) resumes the loop from the next task completion. No back-filling of past sessions occurs — the loop only processes sessions that complete after it is enabled.

---

## Logging Reference

All learning loop events log to the `LEARNING` category. Every step of the loop must produce at least one log entry.

| Event | Fields |
|---|---|
| `reflect-start` | `agent_id`, `session_file`, `trigger` (task/conversation) |
| `reflect-end` | `agent_id`, `duration_ms` |
| `memory-update` | `agent_id`, `facts_added`, `facts_skipped` |
| `pattern-check` | `agent_id`, `reflections_read`, `daily_count` |
| `pattern-found` | `agent_id`, `pattern_id`, `occurrences` |
| `pattern-skipped` | `agent_id`, `reason` (rate_limit / threshold_not_met) |
| `skill-draft-created` | `agent_id`, `skill_id`, `pattern_id` |
| `skill-created` | `agent_id`, `skill_id`, `approved_by` |
| `skill-declined` | `agent_id`, `skill_id`, `user_id` |
| `soul-update-requested` | `agent_id`, `proposed_diff_chars` |
| `soul-update-approved` | `agent_id`, `approved_by` |
| `soul-update-rejected` | `agent_id`, `user_id` |
| `soul-update-applied` | `agent_id` |
| `cycle-rate-limited` | `agent_id`, `reason` (hourly_cooldown / daily_pattern_limit) |
| `cycle-skipped` | `agent_id`, `reason` (learning_disabled) |

Log format:

```
2025-06-12T09:35:00Z [LEARNING] reflect-start agent_id=agent_finance_01 session_file=working/session_1749720400.md trigger=task
2025-06-12T09:35:04Z [LEARNING] reflect-end agent_id=agent_finance_01 duration_ms=3840
2025-06-12T09:35:05Z [LEARNING] memory-update agent_id=agent_finance_01 facts_added=2 facts_skipped=0
2025-06-12T09:35:05Z [LEARNING] pattern-check agent_id=agent_finance_01 reflections_read=10 daily_count=1
```
