# Skills Integration

Skills are reusable agent capabilities using the Claude Code Skills format. Skills may be installed from the store, loaded manually, or published to skills.sh with explicit user consent.

## Main Agent Path

The user runs `/skills`, `/install`, or `/skills publish`. The Main Agent lists installed skills, shows permissions and cost impact, asks for confirmation, installs or publishes only after approval, and logs the action.

## Manual Path

Create a skill folder with `manifest.yml` and `skill.md`, then run `synapse reload` or load it through the Dashboard. Manual load validates manifest fields, permission declarations, entrypoint, and target agents.

## Publishing Rule

Publishing is never automatic. Agents may suggest creating a skill, but `/skills publish` must be run by the user and must confirm registry, metadata, and permissions.

## Logging

Skill install, invoke, validation failure, creation proposal, and publish events use `PLUGIN`, `LEARNING`, and `STORE` as appropriate.
