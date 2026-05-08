# ACP Registry

ACP Registry integration discovers provider definitions, endpoints, model lists, and credential modes without requiring a local plugin file for standard providers.

## Credential Types

| Type | Meaning |
|---|---|
| `api_key` | Provider-issued key stored encrypted |
| `subscription_id` | Subscription billing identifier stored encrypted |
| `self_hosted` | Local endpoint such as Ollama or vLLM |

## Main Agent Path

The user runs `/models new` and chooses ACP Registry. The Main Agent fetches provider metadata, asks for credential type, shows endpoint and model capabilities, requests confirmation, stores encrypted credentials, and logs provider registration.

## Manual Path

Create or edit provider configuration in the database or config file using registry metadata, then reload model providers. Manual entries must include endpoint, credential type, model list, and capabilities.

## Logging

Provider discovery, credential validation, model sync, and setup failure events use `MODEL` and `STORE`.
