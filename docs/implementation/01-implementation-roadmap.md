# Implementation Roadmap

> Based on `docs/stage-0/01-stage0-baseline.md` stage numbering and names. Stage names are preserved.

## Stage Gate Summary

| Stage | Name | Goal | Main Output | Next-Stage Gate |
|---:|---|---|---|---|
| 0 | 需求与设计 | Freeze requirements and design. | Stage 0 documents. | Documents reviewed; conflicts and assumptions recorded. |
| 1 | 后端基础工程 | Create runnable backend foundation only. | Spring Boot empty project, package structure, common response, exception handling, validation, MySQL/MyBatis-Plus baseline, initial schema/data. | Backend starts; health/basic checks pass; schema initializes; no business modules beyond foundation. |
| 2 | 知识库 CRUD | Implement category and knowledge management. | Admin category/knowledge APIs, pagination/search, `INNER JOIN`, cache invalidation hook placeholder or implementation as appropriate. | Admin CRUD works; employee cannot mutate; required `INNER JOIN` verified. |
| 3 | 会话与消息 | Implement sessions and message history. | Session/message APIs, ownership checks, `LEFT JOIN` session list. | Own-session lifecycle works; empty sessions appear through `LEFT JOIN`; cross-user access denied. |
| 4 | 大模型问答 | Implement knowledge-grounded Agent answer flow without Redis optimization. | `LlmClient`, Prompt builder, knowledge search, answer persistence, references, trace, failure/no-knowledge behavior. | First question can produce grounded answer; no-knowledge does not call LLM; failure does not save fake answer. |
| 5 | Redis | Add answer cache and degradation. | Cache key normalization, read/write, TTL, cache hit handling, invalidation, Redis failure fallback. | Repeated question hits cache; cache hit still saves messages; Redis failure does not block main flow. |
| 6 | Agent 工具、工单与异步日志 | Add tools, ticket workflow, and async logs. | `AgentTool`, `searchKnowledge`, `createTicket`, ticket APIs, `@Async` logs, thread pool. | Ticket state machine and logs pass API checks; async failure does not roll back main business. |
| 7 | Vue 与联调 | Build frontend and integrate APIs. | Vue pages, Axios layer, admin/user flows. | Browser can complete main user/admin MVP flows. |
| 8 | Docker 与项目包装 | Package and present the project. | Dockerfile, Docker Compose, Nginx, README, screenshots, demo data, deployment notes. | Docker Compose deployment works; GitHub presentation complete; secrets excluded. |

## Stage 0: 需求与设计

Goal:

- Confirm the repository baseline before implementation.

Inputs:

- Current task instruction.
- `AGENTS.md`.
- Five stage 0 documents under `docs/stage-0/`.

Outputs:

- `docs/implementation/00-stage0-review.md`
- `docs/implementation/01-implementation-roadmap.md`
- `docs/implementation/02-task-breakdown.md`
- `docs/implementation/03-acceptance-checklist.md`
- `docs/implementation/04-risk-register.md`
- `docs/implementation/05-decision-log.md`

Dependencies:

- Existing Git repository and stage 0 documents.

Acceptance:

- Required files exist and are readable.
- Conflicts, missing documents, and assumptions are explicitly recorded.
- No business code or project scaffolding is created.

Gate:

- User accepts or resolves the recorded Stage 1 assumptions.

## Stage 1: 后端基础工程

Goal:

- Create a runnable backend foundation that future modules can build on.

Inputs:

- Stage 0 review.
- Frozen or confirmed Java version.
- Database design document.
- API common response and error-code rules.

Outputs:

- Spring Boot backend skeleton.
- Maven configuration.
- `com.workmate.ai` package structure.
- Common response wrapper.
- Global exception handling.
- Validation setup.
- MyBatis-Plus and MySQL connection baseline.
- Initial SQL/migration and seed data.
- Health endpoint.

Dependencies:

- Resolve Java 17 vs Java 21 conflict.
- Confirm no JWT/Spring Security in V1 unless baseline changes.

Acceptance:

- Backend starts locally.
- Health endpoint returns application/database status as designed.
- Schema contains exactly the 8 MVP tables.
- Seed users and demo knowledge exist.
- No knowledge CRUD, session, Agent, Redis, ticket, frontend, or Docker business implementation is included prematurely.

Gate:

- Compile/startup verified.
- Database schema verified.
- Stage 1 documentation updated.

## Stage 2: 知识库 CRUD

Goal:

- Implement knowledge category and knowledge management with role enforcement.

Inputs:

- Stage 1 backend foundation.
- Database tables: `knowledge_category`, `knowledge`, `sys_user`.
- API sections 5 and 6.

Outputs:

- Category DTO/VO/controller/service/mapper.
- Knowledge DTO/VO/controller/service/mapper.
- Admin-only mutations.
- Employee/admin read APIs.
- Required `INNER JOIN` search/list SQL.
- Cache invalidation integration point for knowledge changes.

Dependencies:

- Stage 1 common response, exception, validation, mapper, schema, and user context.

Acceptance:

- Admin can create/update/status/delete category and knowledge.
- Employee cannot mutate category/knowledge.
- Enabled knowledge search excludes disabled/deleted categories and knowledge.
- `INNER JOIN` is used in mapper SQL for knowledge with effective category.
- Pagination works.

Gate:

- API checks pass.
- Database records match expected changes.
- Permission errors return documented codes.

## Stage 3: 会话与消息

Goal:

- Implement user-owned chat session and message history.

Inputs:

- Stage 1 foundation.
- Tables: `chat_session`, `chat_message`.
- API section 7.

Outputs:

- Session create/list/detail/title/delete.
- Message list.
- Ownership checks.
- Required `LEFT JOIN` session list with last message.

Dependencies:

- User context and common API rules.

Acceptance:

- Users can manage only their own sessions.
- Empty sessions are returned by list query.
- Message order is `created_at ASC, id ASC`.
- Logical deletion hides sessions from normal queries.

Gate:

- `LEFT JOIN` behavior verified.
- Cross-user access denied.

## Stage 4: 大模型问答

Goal:

- Implement the first working knowledge-grounded Agent flow without relying on Redis cache optimization.

Inputs:

- Stages 2 and 3.
- API section 8.
- Business process sections 5, 6, 8, and 13.

Outputs:

- Agent chat endpoint.
- Question normalization.
- Knowledge search through Stage 2 service/mapper.
- Prompt builder.
- `LlmClient` abstraction and implementation.
- User and assistant message persistence.
- Knowledge references.
- Trace steps.
- No-knowledge and model-failure behavior.

Dependencies:

- Knowledge search exists.
- Session/message persistence exists.
- LLM provider configuration is confirmed or mockable.

Acceptance:

- Normal answer persists user/assistant messages and references.
- No-knowledge path saves insufficient-knowledge assistant message and returns `canCreateTicket=true`.
- Model failure returns `50004` and does not save fake assistant answer.
- Trace excludes chain of thought, full system prompt, API keys, stack traces, and sensitive headers.

Gate:

- Main Agent happy path and failure paths verified through API.

## Stage 5: Redis

Goal:

- Add answer caching without changing the business contract.

Inputs:

- Stage 4 Agent flow.
- Redis baseline.

Outputs:

- Cache service.
- Key normalization and hash.
- 30-minute TTL.
- Cache hit response.
- Cache write after successful answer.
- Cache invalidation after knowledge/category mutations.
- Redis failure fallback.

Dependencies:

- Agent answer structure and knowledge mutation paths exist.

Acceptance:

- First identical question returns `fromCache=false`.
- Second identical question returns `fromCache=true`.
- Cache hit still saves current conversation messages and references.
- Cache hit does not call the LLM.
- Redis failure logs/skips cache and continues.

Gate:

- Cache behavior verified with Redis available and unavailable.

## Stage 6: Agent 工具、工单与异步日志

Goal:

- Complete fallback/support loop and async observability.

Inputs:

- Stage 4 Agent flow.
- Stage 5 cache status.
- Ticket and model-log API sections.

Outputs:

- `AgentTool` interface.
- `searchKnowledge` tool.
- `createTicket` tool gated by user confirmation.
- Ticket APIs and state machine.
- Async model logs.
- Custom async thread pool.

Dependencies:

- Agent flow can produce `canCreateTicket=true`.
- User/session/message ownership checks exist.

Acceptance:

- Ticket creation validates session/message ownership and message role.
- Ticket state transitions reject invalid rollback.
- `RESOLVED` requires resolution and sets handler/resolution time.
- Logs are recorded for `CACHE_HIT`, `SUCCESS`, `FAILED`, and `NO_KNOWLEDGE`.
- Async log failure does not roll back or fail main response.

Gate:

- Ticket workflow and model logs pass API checks.

## Stage 7: Vue 与联调

Goal:

- Provide usable frontend flows for employees and admins.

Inputs:

- Backend APIs through Stage 6.
- API document and permission matrix.

Outputs:

- Vue 3 + Vite app.
- Axios API layer.
- Chat page.
- Knowledge management page.
- Category management page.
- Ticket management page.
- Model log page or statistics display.

Dependencies:

- Backend endpoints stable.

Acceptance:

- Employee can ask questions, view history, and create tickets when allowed.
- Admin can manage categories/knowledge, tickets, and logs.
- Permission failures are visible and understandable.
- Frontend does not expose secrets or rely on hardcoded admin-only shortcuts.

Gate:

- Browser-based main flow checks pass.

## Stage 8: Docker 与项目包装

Goal:

- Make the MVP deployable and presentable.

Inputs:

- Backend and frontend complete.
- Deployment baseline.

Outputs:

- Backend Dockerfile.
- Frontend/Nginx Dockerfile or equivalent.
- Docker Compose for MySQL, Redis, backend, frontend/Nginx.
- `.env.example`.
- `.gitignore` includes `.env`.
- README with setup, screenshots, demo account notes, API/deployment notes.
- Demo data.

Dependencies:

- App can run locally before packaging.

Acceptance:

- Docker Compose starts all services.
- Health endpoint reports expected application/MySQL/Redis status.
- Demo workflow can be performed after deployment.
- No secrets are committed.
- GitHub presentation material is complete.

Gate:

- Final MVP acceptance checklist passes.
