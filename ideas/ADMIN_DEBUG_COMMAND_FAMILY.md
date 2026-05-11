# Admin Debug Command Family

## Goal

Define a controlled debug/ops command family for secure troubleshooting and recovery, with strong auditability and admin approval safeguards.

## Candidate Scope

### 1) Debug command namespace

- Add an explicit admin-only command family (for example: `/debug ...`).
- Keep capabilities modular and permission-gated.
- Require elevated role and audit log for every invocation.

### 2) Optional tooling integrations

- Redis inspection tools (for example Redis Commander integration).
- Postgres inspection tools (for example pgAdmin integration).
- Runtime health and queue/state introspection commands.

### 3) Admin password recovery flow

- CLI/TUI command to request admin password reset.
- Require explicit confirmation in Admin UI before reset executes.
- Include one-time token / short TTL approval handshake.

### 4) Approval & audit model

- Every debug action should have:
  - requester identity
  - target scope
  - reason/comment
  - approval status
  - immutable audit trail

### 5) Safety constraints

- No silent destructive actions.
- Time-boxed elevated access.
- Clear operator warnings and confirmation prompts for risky commands.

## Rollout Idea

1. Define command taxonomy + permissions.
2. Add read-only introspection commands first.
3. Add approval-gated mutating operations.
4. Add external debug tool integrations (Redis/Postgres).
