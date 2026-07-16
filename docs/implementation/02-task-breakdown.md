# Task Breakdown

> Task granularity is sized for single Codex execution rounds. Each task should end with verification and a focused suggested commit. Do not combine multiple business modules into one task.

## Stage 1: 后端基础工程

### S1-T01

- Stage: 1
- Name: Resolve foundation assumptions and create backend skeleton
- Goal: Confirm Java version/no-JWT assumption, then create a runnable empty Spring Boot backend if approved for Stage 1.
- Prerequisites: User accepts Stage 0 review assumptions, especially Java 17 vs Java 21.
- Inputs: `docs/implementation/00-stage0-review.md`, `docs/stage-0/01-stage0-baseline.md`, `AGENTS.md`.
- Outputs: Backend skeleton only; no business modules.
- Suggested modification scope: `backend/`, root `.gitignore` if needed, minimal backend README notes if created.
- Not included: Knowledge CRUD, sessions, Agent, Redis, tickets, frontend, Docker.
- Acceptance: App starts; package root is `com.workmate.ai`; no JWT/Spring Security unless baseline changed.
- Test requirements: `mvn test`; start app; call a minimal health/basic endpoint if present.
- Suggested commit message: `feat(backend): initialize Spring Boot foundation`

### S1-T02

- Stage: 1
- Name: Add common API response, errors, and validation baseline
- Goal: Implement `CommonResult`, business error codes, global exception handling, and validation wiring.
- Prerequisites: S1-T01.
- Inputs: API sections 2 and 3; `AGENTS.md` layer rules.
- Outputs: Common response wrapper, error enum/exception, validation behavior.
- Suggested modification scope: `backend/src/main/java/com/workmate/ai/common`, `exception`, `config`, tests.
- Not included: Module-specific controllers or database business logic.
- Acceptance: Validation errors return `40001`; missing user handling can be stubbed for later user context if not yet implemented.
- Test requirements: Unit or MVC tests for success/error envelope and validation failure.
- Suggested commit message: `feat(backend): add common response and exception handling`

### S1-T03

- Stage: 1
- Name: Add database schema and seed data
- Goal: Create the exact eight MVP tables and seed demo users/categories/knowledge.
- Prerequisites: S1-T01.
- Inputs: Database design sections 2, 4, 7, and 8.
- Outputs: SQL migration/init script and local configuration example.
- Suggested modification scope: `backend/src/main/resources`, database init scripts.
- Not included: Business CRUD APIs.
- Acceptance: Schema has exactly `sys_user`, `knowledge_category`, `knowledge`, `chat_session`, `chat_message`, `knowledge_reference`, `ticket`, `model_call_log`.
- Test requirements: Run schema initialization against local MySQL or test database; verify seed rows.
- Suggested commit message: `feat(db): add initial schema and demo data`

### S1-T04

- Stage: 1
- Name: Add user context and health baseline
- Goal: Implement current-user loading from `X-User-Id` and a health endpoint without LLM calls.
- Prerequisites: S1-T02, S1-T03.
- Inputs: API sections 1, 4, 11; requirements section 3.
- Outputs: `GET /api/users/current`, `GET /api/health`, user lookup/role validation helper.
- Suggested modification scope: `controller`, `service`, `mapper`, `dto`/`vo` for user/health.
- Not included: Formal login, JWT, Spring Security, RBAC.
- Acceptance: Missing header returns `40002`; disabled/missing user returns `40003`; health does not call LLM.
- Test requirements: API tests for current user and health.
- Suggested commit message: `feat(backend): add user context and health endpoints`

## Stage 2: 知识库 CRUD

### S2-T01

- Stage: 2
- Name: Implement category management
- Goal: Add category CRUD/status APIs with admin-only mutation.
- Prerequisites: Stage 1 complete.
- Inputs: API section 5; database table `knowledge_category`.
- Outputs: Category DTOs, VOs, controller, service, mapper, tests.
- Suggested modification scope: `controller`, `dto`, `vo`, `service`, `mapper`, XML if needed.
- Not included: Knowledge CRUD or Agent search.
- Acceptance: Admin can create/update/status/list/detail; employee can list enabled categories but cannot mutate.
- Test requirements: Permission, validation, duplicate name, status filtering.
- Suggested commit message: `feat(knowledge): implement category management`

### S2-T02

- Stage: 2
- Name: Implement knowledge management
- Goal: Add knowledge CRUD/status/delete/page/detail APIs.
- Prerequisites: S2-T01.
- Inputs: API section 6; database table `knowledge`.
- Outputs: Knowledge DTOs, VOs, controller, service, mapper.
- Suggested modification scope: Knowledge module files and tests.
- Not included: Agent answering and Redis cache behavior beyond an invalidation hook if needed.
- Acceptance: Admin mutations work; employee read-only access works; logical delete hides records.
- Test requirements: Category existence validation, permission checks, pagination, validation failures.
- Suggested commit message: `feat(knowledge): implement knowledge CRUD`

### S2-T03

- Stage: 2
- Name: Implement knowledge search with INNER JOIN
- Goal: Implement effective knowledge search using required `INNER JOIN` with enabled categories.
- Prerequisites: S2-T02.
- Inputs: Database section 6.1; API `GET /api/knowledge/search`.
- Outputs: Mapper XML/query, search VO, tests.
- Suggested modification scope: Knowledge mapper XML and search service.
- Not included: LLM Prompt or Agent endpoint.
- Acceptance: Search returns max 5 enabled, non-deleted knowledge rows whose categories are enabled and non-deleted.
- Test requirements: Verify disabled category knowledge is excluded; inspect SQL or mapper to confirm `INNER JOIN`.
- Suggested commit message: `feat(knowledge): add effective knowledge search`

## Stage 3: 会话与消息

### S3-T01

- Stage: 3
- Name: Implement chat session lifecycle
- Goal: Create/list/detail/title/delete own sessions.
- Prerequisites: Stage 1 complete.
- Inputs: API section 7; database table `chat_session`.
- Outputs: Session DTOs, VOs, controller, service, mapper.
- Suggested modification scope: Chat session module files and tests.
- Not included: Agent asking or message creation through Agent.
- Acceptance: Users can only access own sessions; logical delete hides sessions.
- Test requirements: Own access, cross-user denial, validation.
- Suggested commit message: `feat(chat): implement session lifecycle`

### S3-T02

- Stage: 3
- Name: Implement session list LEFT JOIN and message query
- Goal: Return sessions with last message while preserving empty sessions, and return ordered messages.
- Prerequisites: S3-T01.
- Inputs: Database section 6.2; API section 7.
- Outputs: `LEFT JOIN` mapper query, message list endpoint.
- Suggested modification scope: Chat mapper XML, message service/controller, tests.
- Not included: Agent answer creation.
- Acceptance: Empty session appears in list; messages ordered by `created_at ASC, id ASC`.
- Test requirements: Verify empty-session list result, last-message result, cross-user denial.
- Suggested commit message: `feat(chat): add message history and session list join`

## Stage 4: 大模型问答

### S4-T01

- Stage: 4
- Name: Add LLM client abstraction and Prompt builder
- Goal: Create a provider-neutral `LlmClient` interface and Prompt assembly based on retrieved knowledge.
- Prerequisites: Stage 2 search, Stage 3 sessions/messages.
- Inputs: Baseline sections 7 and 12; process sections 5 and 13.
- Outputs: `LlmClient`, implementation stub/config, Prompt builder, tests.
- Suggested modification scope: `client`, `client/impl`, Agent support services.
- Not included: Redis cache, tickets, async logs.
- Acceptance: Prompt uses retrieved knowledge and does not expose secrets; LLM config is externalized.
- Test requirements: Unit tests for prompt content and no-knowledge bypass decision.
- Suggested commit message: `feat(agent): add llm client and prompt builder`

### S4-T02

- Stage: 4
- Name: Implement Agent chat happy path
- Goal: Persist user question, search knowledge, call LLM, persist answer/references, and return trace.
- Prerequisites: S4-T01.
- Inputs: API section 8 normal answer; database transaction sections.
- Outputs: `POST /api/agent/chat` happy path.
- Suggested modification scope: Agent controller/service, chat message service, reference mapper.
- Not included: Redis cache hit, ticket creation, async model logs.
- Acceptance: User/assistant messages and references are saved; trace has safe system-step summaries.
- Test requirements: API/integration test with mock LLM and seeded knowledge.
- Suggested commit message: `feat(agent): implement knowledge-grounded chat`

### S4-T03

- Stage: 4
- Name: Implement no-knowledge and model-failure paths
- Goal: Enforce no-free-answer and no-fake-answer failure behavior.
- Prerequisites: S4-T02.
- Inputs: API section 8 no-knowledge/model failure; process sections 5.3 and 5.4.
- Outputs: No-knowledge response and model-failure response behavior.
- Suggested modification scope: Agent service and tests.
- Not included: Actual ticket creation.
- Acceptance: No knowledge does not call LLM; model failure does not save assistant answer; both allow ticket creation where documented.
- Test requirements: Mock no-search-result and LLM failure cases.
- Suggested commit message: `feat(agent): handle no-knowledge and model failure`

## Stage 5: Redis

### S5-T01

- Stage: 5
- Name: Add Redis answer cache service
- Goal: Implement key normalization, hashing, TTL, read/write, and serialization.
- Prerequisites: Stage 4 Agent response structure.
- Inputs: Redis baseline sections.
- Outputs: Cache service and tests.
- Suggested modification scope: `service`/`config`/`utils` cache files.
- Not included: Knowledge invalidation integration.
- Acceptance: Cache stores answer and references only; does not store session/message/user/trace data.
- Test requirements: Unit tests for normalization/hash and serialization.
- Suggested commit message: `feat(cache): add agent answer cache service`

### S5-T02

- Stage: 5
- Name: Integrate cache with Agent flow
- Goal: Add cache lookup/write and cache-hit response behavior.
- Prerequisites: S5-T01, S4-T02.
- Inputs: API cache-hit requirements.
- Outputs: Cache-aware Agent chat.
- Suggested modification scope: Agent service and tests.
- Not included: Ticket module.
- Acceptance: Second identical question returns `fromCache=true`, saves messages/references, and skips LLM.
- Test requirements: API test with Redis or test double; verify LLM called only once.
- Suggested commit message: `feat(agent): integrate Redis answer cache`

### S5-T03

- Stage: 5
- Name: Add cache invalidation and Redis degradation
- Goal: Clear answer cache after knowledge/category changes and continue when Redis fails.
- Prerequisites: S5-T02, Stage 2 modules.
- Inputs: Requirements section 2.5; process section 7.
- Outputs: Invalidation on knowledge/category changes and fallback behavior.
- Suggested modification scope: Knowledge/category services, cache service, tests.
- Not included: Redis cluster or distributed locks.
- Acceptance: Knowledge mutation clears `agent:answer:*`; Redis errors do not block main Agent flow.
- Test requirements: Mutation invalidation test and simulated Redis failure test.
- Suggested commit message: `feat(cache): invalidate answers and degrade on Redis failure`

## Stage 6: Agent 工具、工单与异步日志

### S6-T01

- Stage: 6
- Name: Add AgentTool registry
- Goal: Implement `AgentTool`, `searchKnowledge`, and registry management.
- Prerequisites: Stage 2 search.
- Inputs: Baseline section 15; process section 12.
- Outputs: Tool interface and search tool.
- Suggested modification scope: `tool`, `tool/impl`, tests.
- Not included: Automatic model-driven tool execution beyond documented MVP needs.
- Acceptance: `searchKnowledge` can be invoked through `Map<String, AgentTool>`.
- Test requirements: Unit tests for registry and tool output.
- Suggested commit message: `feat(agent): add tool registry`

### S6-T02

- Stage: 6
- Name: Implement ticket workflow
- Goal: Add ticket creation/query/admin update APIs and state machine.
- Prerequisites: Stage 3 messages and Stage 4 no-knowledge behavior.
- Inputs: API section 9; database `ticket`; process section 10.
- Outputs: Ticket DTOs, VOs, controller, service, mapper, `createTicket` tool.
- Suggested modification scope: Ticket module and tool implementation.
- Not included: Formal approval workflow or notifications.
- Acceptance: Ownership validation, role validation, allowed transitions, and `RESOLVED` resolution rules are enforced.
- Test requirements: Create own ticket, deny invalid source message, admin transition, invalid rollback.
- Suggested commit message: `feat(ticket): implement ticket workflow`

### S6-T03

- Stage: 6
- Name: Implement async model-call logs
- Goal: Save model/cache log records asynchronously with a custom thread pool.
- Prerequisites: Stage 4 Agent, Stage 5 cache.
- Inputs: API section 10; process section 11.
- Outputs: Log entity/mapper/service, admin query/statistics if included, async config.
- Suggested modification scope: Model log module, async config, Agent integration.
- Not included: Complex monitoring system.
- Acceptance: Four statuses are recorded; async failure is logged and does not fail main response.
- Test requirements: Agent calls produce expected log statuses; admin query is permission-protected.
- Suggested commit message: `feat(logs): add asynchronous model call logging`

## Stage 7: Vue 与联调

### S7-T01

- Stage: 7
- Name: Initialize Vue frontend shell and API client
- Goal: Create frontend foundation and Axios API layer after backend APIs are stable.
- Prerequisites: Backend through Stage 6.
- Inputs: Frontend stack and API base path.
- Outputs: Vue 3/Vite app shell, routing, Axios client, user header handling for demo.
- Suggested modification scope: `frontend/`.
- Not included: Full pages.
- Acceptance: Frontend starts and can call current-user/health endpoints.
- Test requirements: Build check and browser smoke test.
- Suggested commit message: `feat(frontend): initialize Vue app shell`

### S7-T02

- Stage: 7
- Name: Build employee chat and ticket flows
- Goal: Implement employee-facing chat/session/message/ticket creation flow.
- Prerequisites: S7-T01 and backend APIs.
- Inputs: API sections 7, 8, 9.
- Outputs: Chat page, session list, message list, ticket creation UI.
- Suggested modification scope: Frontend views/components/api files.
- Not included: Admin knowledge/log pages.
- Acceptance: Employee can ask, see answer/references/trace, and create ticket when allowed.
- Test requirements: Browser smoke tests for normal, cache-hit, no-knowledge paths.
- Suggested commit message: `feat(frontend): add employee chat workflow`

### S7-T03

- Stage: 7
- Name: Build admin management pages
- Goal: Implement category, knowledge, ticket, and model-log admin views.
- Prerequisites: S7-T01 and backend APIs.
- Inputs: API sections 5, 6, 9, 10.
- Outputs: Admin CRUD/list pages.
- Suggested modification scope: Frontend admin views/components/api files.
- Not included: Complex dashboards beyond optional simple statistics.
- Acceptance: Admin can manage categories/knowledge/tickets/logs; employee cannot use admin mutations.
- Test requirements: Browser smoke tests and build check.
- Suggested commit message: `feat(frontend): add admin management views`

## Stage 8: Docker 与项目包装

### S8-T01

- Stage: 8
- Name: Add Docker packaging
- Goal: Containerize backend, frontend/Nginx, MySQL, and Redis.
- Prerequisites: Stages 1-7 complete.
- Inputs: Deployment baseline.
- Outputs: Dockerfile(s), `docker-compose.yml`, Nginx config.
- Suggested modification scope: `backend/`, `frontend/`, deployment files.
- Not included: Kubernetes or GitHub Actions.
- Acceptance: Docker Compose starts all MVP services locally.
- Test requirements: `docker compose up` smoke test; health endpoint check.
- Suggested commit message: `deploy: add Docker Compose packaging`

### S8-T02

- Stage: 8
- Name: Add project documentation and demo presentation
- Goal: Create final README, screenshots, setup/deployment guide, and demo data notes.
- Prerequisites: S8-T01.
- Inputs: Stage 0 acceptance and final app behavior.
- Outputs: `README.md`, screenshots, `.env.example`, deployment notes.
- Suggested modification scope: Root docs and demo assets.
- Not included: CI/CD or production monitoring.
- Acceptance: A new developer can run the project from README; no secrets are committed.
- Test requirements: Follow README setup on a clean local environment or document any unverified steps.
- Suggested commit message: `docs: add README and deployment guide`
