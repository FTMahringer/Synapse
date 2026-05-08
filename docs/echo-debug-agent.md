# ECHO Debug Agent

ECHO is a manual-only debug agent. It is local, minimal, and never activated automatically.

## Activation

ECHO starts only when the user runs `/echo` in the CLI or Dashboard. No fallback, error handler, heartbeat, or other agent may activate it.

## Model Selection

ECHO uses registered self-hosted providers only. The default is Ollama with CPU-friendly micro-models such as `phi3-mini` or `llama3.2:1b`.

## Capabilities

- Simple local questions
- Vault markdown reads
- Local task saves
- Local note saves
- Project context reads
- Local memory queries

## Limitations

- No internet
- No external APIs
- No store access
- No git provider access
- No plugin install
- No agent or team management
- No system configuration changes

## Main Agent Path

The Main Agent may explain ECHO or route the user to `/echo`, but it must not invoke ECHO automatically.

## Manual Path

The user types `/echo`. The runtime starts ECHO with the configured local model and ends the session on `/exit`.

## Logging

ECHO start, stop, denied capability, and local model failure events use `AGENT`.
