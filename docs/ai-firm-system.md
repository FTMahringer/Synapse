# AI-Firm System

The AI-Firm is an optional project-management layer with one CEO. A {SYSTEM_NAME} instance may have zero or one active firm.

## Structure

The firm lives under `agents/ai-firm/` and is configured by `firm.yml`. The CEO files live under `agents/ai-firm/ceo/`.

## Max-One Rule

The database table `ai_firm` is singleton-constrained. Runtime code must reject attempts to create a second firm and route the user to edit the existing one instead.

## Main Agent Path

The user runs `/firm new`. The Main Agent explains the CEO, teams, Paperclip mode, and board integration options. It asks which teams are included, requests confirmation, writes `firm.yml`, creates CEO files, registers the firm, and logs `firm.created`.

## Manual Path

Create `agents/ai-firm/firm.yml` and CEO files manually. Run `synapse reload`. Validation checks the singleton rule, CEO ID, registered teams, routing, and board integration.

## Logging

Firm intake, project planning, task delegation, blocker escalation, completion, and reports use `AI_FIRM`.
