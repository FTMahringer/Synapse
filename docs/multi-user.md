# Multi-User System

The platform supports multiple users with role-based access and per-user resources.

## Roles

| Role | Access |
|---|---|
| OWNER | Full system control and initial admin authority |
| ADMIN | System management, plugin install, shared agent management |
| USER | Own agents, own channels, own projects, shared resources |
| VIEWER | Read-only access to permitted dashboards and logs |

## Authentication

Authentication uses JWT or opaque session tokens tracked in `sessions_auth`. OAuth providers such as GitHub and GitLab may be configured. Two-factor authentication is optional but recommended for OWNER and ADMIN.

## Main Agent Path

Admins can ask the Main Agent to invite users, change roles, or explain access. The Main Agent shows impact, requests confirmation, applies the change, and logs it.

## Manual Path

Operators may update users through admin API endpoints or database migrations. Manual edits must preserve the one-OWNER expectation and token security rules.

## Logging

Login, logout, token creation, token expiry, role changes, permission denial, and OAuth events use `AUTH`.
