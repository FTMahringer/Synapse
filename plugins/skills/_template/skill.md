---
name: example-skill
description: Use this skill when an agent needs the concrete capability described by this package.
version: 1.0.0
---

# Example Skill

## Purpose

This file is the canonical skill instruction document. Replace the example text with a precise description of when the skill should be used, what inputs it expects, what output it returns, and what safety limits apply.

## Inputs

- `request`: The user's task or the calling agent's instruction.
- `context`: Relevant conversation, vault, or project context supplied by the runtime.
- `options`: Optional skill-specific configuration from the manifest.

## Procedure

1. Check whether the request matches this skill's purpose.
2. Validate required inputs.
3. Perform only the actions declared in the manifest permissions.
4. Return a concise result with any warnings, limits, or follow-up actions.

## Output Contract

Return a Markdown response with:

- `Result`: the primary answer or generated artifact.
- `Evidence`: relevant source context when available.
- `Limits`: anything the caller must not assume.

## Safety Rules

- Do not request permissions that are not declared in `manifest.yml`.
- Do not read or write files unless the manifest allows it.
- Do not call external services unless network access is explicitly enabled.
- Do not publish this skill automatically; publishing requires user consent through `/skills publish`.
