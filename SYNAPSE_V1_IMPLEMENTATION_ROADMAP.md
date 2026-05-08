# SYNAPSE v1 Implementation Roadmap

> Roadmap for turning the documentation scaffold into a runnable AI platform.
> Active development version: `v1.0.0-dev`.
> First release version: `v1.0.0`.

---

## 1. Release Rule

- `v1.0.0-dev` marks the active implementation track.
- Do not tag `v1.0.0` until the platform can run locally with backend, frontend, database, logs, and at least one model provider path.
- Release notes for `v1.0.0` must summarize all implementation work completed during the dev cycle.
- The existing `v0.x.0` roadmap remains the scaffold/specification history.

---

## 2. Target Product

`v1.0.0` is the first runnable MVP of SYNAPSE:

- Spring Boot backend starts on port `8080`.
- Vue dashboard starts on port `3000`.
- PostgreSQL schema is managed by migrations.
- Redis is available for future realtime/log fanout.
- Dashboard can show health, agents, logs, plugins, store metadata, and settings.
- Backend exposes REST APIs and OpenAPI docs.
- Backend persists structured logs.
- Backend can read agent definitions from the repository.
- Backend has a model provider abstraction with an Ollama-first implementation path.
- ECHO stays manual-only.

---

## 3. Module Layout

| Module | Path | Purpose |
|---|---|---|
| Backend | `packages/core` | Spring Boot application |
| Frontend | `packages/dashboard/frontend` | Vue 3 + Vite dashboard |
| CLI | `packages/cli` | Go CLI after backend APIs stabilize |
| Database | `backend/db` | Schema, seed, migrations |
| Installer | `installer/compose` | Local runtime services |

---

## 4. Implementation Milestones

### v1-dev.1 - Backend Build Skeleton

Goal: create a real Spring Boot project that compiles and starts.

Scope:

- Add `packages/core/pom.xml`.
- Add Spring Boot main class.
- Add application config.
- Add `/api/health`.
- Add basic test structure.

Exit criteria:

- Backend project structure is valid.
- Health endpoint is implemented.
- Static source checks pass locally.

### v1-dev.2 - Database and Migrations

Goal: move the existing SQL foundation into runtime-managed migrations.

Scope:

- Add Flyway migrations from `backend/db/schema.sql` and `backend/db/seed.sql`.
- Configure PostgreSQL datasource.
- Add profile-based local configuration.
- Add migration validation guidance.

Exit criteria:

- Backend can apply schema from migrations.
- Seed data is idempotent.
- DB config is externalized.

### v1-dev.3 - Backend Domains and APIs

Goal: expose the first usable REST API.

Scope:

- Agents API.
- Teams API.
- Plugins API.
- Store API.
- Logs API.
- Settings API.
- OpenAPI metadata.

Exit criteria:

- Dashboard has API contracts to consume.
- Mutating endpoints log system events.
- Read endpoints return stable DTOs.

### v1-dev.4 - Agent Definition Loader

Goal: load repository agent files into runtime DTOs.

Scope:

- Parse `agents/main`, `agents/echo`, templates, and AI-Firm definitions.
- Validate required files.
- Expose loaded agents via API.
- Keep ECHO manual-only.

Exit criteria:

- Backend can list file-defined agents.
- Validation failures are visible in logs.

### v1-dev.5 - Logging Runtime

Goal: make logs a real platform feature.

Scope:

- Log entity and repository.
- Log write service.
- REST query endpoint.
- SSE endpoint for live logs.
- Category constants matching docs.

Exit criteria:

- System operations create logs.
- Dashboard can query logs.
- SSE stream is available.

### v1-dev.6 - Model Provider Shell

Goal: define the runtime boundary for model calls.

Scope:

- Model provider interface.
- Ollama client skeleton.
- Provider status endpoint.
- No automatic ECHO fallback.

Exit criteria:

- Provider status can be queried.
- Future chat runtime has a model abstraction to call.

### v1-dev.7 - Frontend Build Skeleton

Goal: create a Vue dashboard that runs.

Scope:

- Add Vite/Vue project files.
- Add TypeScript config.
- Add API client.
- Add router and Pinia store.
- Add base dashboard shell.

Exit criteria:

- `npm` can validate project metadata.
- Dashboard can call backend health API.

### v1-dev.8 - Dashboard Screens

Goal: build the first usable dashboard views.

Scope:

- Overview.
- Agents.
- Logs.
- Plugins.
- Store.
- Settings.

Exit criteria:

- Operator can inspect core runtime state.
- Views use backend APIs, not static mock data, where APIs exist.

### v1-dev.9 - Compose Runtime

Goal: run backend, frontend, PostgreSQL, Redis, Qdrant, and optional Ollama together.

Scope:

- Add backend service to Compose.
- Add frontend service to Compose.
- Wire environment variables.
- Add health checks.

Exit criteria:

- `docker compose` config validates.
- Local runtime has all planned services.

### v1-dev.10 - v1.0.0 Release Hardening

Goal: prepare the first real release.

Scope:

- Add backend and frontend CI.
- Verify boot path.
- Verify migrations.
- Verify dashboard build.
- Update release notes.
- Tag and release `v1.0.0`.

Exit criteria:

- Backend starts.
- Frontend starts.
- Database initializes.
- Dashboard reads backend APIs.
- Release notes are complete.

---

## 5. Current Development Order

1. Backend build skeleton.
2. Database migrations.
3. Backend APIs.
4. Frontend skeleton.
5. Dashboard screens.
6. Compose runtime.
7. Runtime hardening.

This order prioritizes a working backend contract before dashboard polish.

---

## 6. Current Progress

Completed in `v1.0.0-dev`:

- Spring Boot backend skeleton under `packages/core`.
- Backend health API at `/api/health`.
- Backend file-defined agents API at `/api/agents`.
- Flyway migration files copied from the existing schema and seed SQL.
- Vue/Vite dashboard skeleton under `packages/dashboard/frontend`.
- Dashboard health and agents API client.
- Compose backend and dashboard services for quick/dev and production configs.

Remaining before `v1.0.0`:

- Install Maven or add a Maven wrapper so backend builds can run locally.
- Install frontend dependencies and verify the dashboard build.
- Add persistent log APIs and SSE streaming.
- Add full domain APIs for plugins, store, settings, teams, and AI-Firm.
- Add model provider runtime with Ollama status and request path.
- Add CI checks for backend, frontend, Docker, and migrations.
