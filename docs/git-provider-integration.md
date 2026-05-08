# Git Provider Integration

Git provider integration is optional. The platform runs without it.

## Providers

| Provider | Mode |
|---|---|
| GitLab | Cloud or self-hosted |
| GitHub | Cloud |
| Forgejo | Self-hosted |
| Gitea | Self-hosted |

## Main Agent Path

The user runs `/git connect <provider>`. The Main Agent explains permissions, OAuth or token requirements, per-user vs system-wide scope, and asks for confirmation before storing encrypted credentials.

## Manual Path

Operators add rows to `git_providers` through the admin API or database migration, then link repositories in `git_repos`. Tokens must be encrypted before storage.

## Scope

Users can have personal provider accounts. OWNER and ADMIN can configure shared providers for system-wide project management.

## Logging

Provider connect, disconnect, repo link, repo unlink, sync, webhook, and error events use `GIT`.
