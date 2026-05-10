# Backend Restructure Plan — v1.10.0

Audit of `packages/core/src/main/java/dev/synapse/core` as of v1.9.0.

---

## Problems

| Problem | File(s) |
|---|---|
| Controllers in service/ | `service/TaskController.java`, `service/UserController.java` |
| Agent services in flat service/ | 10 files: Agent*, AiFirm*, MainAgent*, TeamDispatch* |
| Conversation services in service/ | `ConversationService`, `MessageService` |
| Provider services in service/ | `ModelProviderService`, `ProviderTestService`, `ProviderUsageLogService` |
| Plugin service duplicate | `service/PluginService` (when `plugin/PluginLifecycleService` exists) |
| Metadata service in wrong module | `service/SystemMetadataService` (controller is in `config/`) |

---

## Move Plan

### New packages created

| New package | Purpose |
|---|---|
| `dev.synapse.core.tasks` | Task CRUD — controller + service |
| `dev.synapse.core.users` | User CRUD — controller + service |
| `dev.synapse.core.agents.service` | All agent orchestration services |
| `dev.synapse.core.conversation` (extend) | ConversationService + MessageService join ConversationController |

### Files moved

| File | From | To | New package |
|---|---|---|---|
| `TaskController.java` | `service/` | `tasks/` | `dev.synapse.core.tasks` |
| `TaskService.java` | `service/` | `tasks/` | `dev.synapse.core.tasks` |
| `UserController.java` | `service/` | `users/` | `dev.synapse.core.users` |
| `UserService.java` | `service/` | `users/` | `dev.synapse.core.users` |
| `AgentHeartbeatService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `AgentManagementService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `AgentMemoryService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `AgentRuntimeService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `AgentService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `AgentTeamService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `AiFirmDispatchService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `MainAgentPromptService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `MainAgentRouterService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `TeamDispatchService.java` | `service/` | `agents/service/` | `dev.synapse.core.agents.service` |
| `ConversationService.java` | `service/` | `conversation/` | `dev.synapse.core.conversation` |
| `MessageService.java` | `service/` | `conversation/` | `dev.synapse.core.conversation` |
| `ModelProviderService.java` | `service/` | `provider/` | `dev.synapse.core.provider` |
| `ProviderTestService.java` | `service/` | `provider/` | `dev.synapse.core.provider` |
| `ProviderUsageLogService.java` | `service/` | `provider/` | `dev.synapse.core.provider` |
| `SystemMetadataService.java` | `service/` | `config/` | `dev.synapse.core.config` |
| `PluginService.java` | `service/` | `plugin/` | `dev.synapse.core.plugin` |

### Files NOT moved (risk > benefit)

| Package | Reason |
|---|---|
| `domain/` (21 files) | Imported by every other package — package rename cascades everywhere |
| `dto/` (33 files) | Same — cross-cutting, stable, no structural problem |
| `repository/` (16 files) | Same |
| `exception/` | Already cohesive, no problem |
| `security/` | Cohesive auth module — rename to `auth/` deferred to v1.11.0 |
| `config/SystemMetadataController` | Already correct location |
| `plugin/` controllers | Already co-located with plugin services |

---

## After Restructure: service/ deleted

`service/` package removed entirely. All 21 files redistributed.

---

## Import Update Summary

Every file that imports from `dev.synapse.core.service.*` must update to the new package.
Spring Boot `@SpringBootApplication` on root package auto-scans all sub-packages — no component scan config changes needed.
