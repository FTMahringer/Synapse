# User ↔ Admin Request & Notification Channel

## Problem

Users need a safe, auditable way to request privileged actions (policy overrides, capability enablement, elevated tool access) without bypassing governance.

## Direction

Create a central request/notification channel with:

- request submission API for users
- approval/rejection workflow for admins
- status tracking and event notifications to both sides
- immutable audit trail

## Primary Use Cases

- policy override request for a specific agent
- temporary capability unlock request
- exception request for blocked delegation/planning action
- security-sensitive operation confirmation

## Core States

`SUBMITTED -> UNDER_REVIEW -> APPROVED | REJECTED | EXPIRED`

Optional:

`CANCELLED` (user withdrew request before decision)

## Architecture Notes

- Single request service with typed request categories
- Notification fanout through existing event/log infrastructure
- Role-based access boundaries:
  - users: create + view own requests
  - admins: view all + decide + annotate rationale

## Data Model Ideas

- `admin_requests` (type, scope, payload, requester, status, expires_at)
- `admin_request_decisions` (decision, decided_by, reason, decided_at)
- `admin_request_notifications` (recipient, channel, delivery_status)

## Rollout

1. generic request entity + lifecycle
2. notifications + inbox endpoints
3. integrate with policy override workflow
4. add templates for common request types
