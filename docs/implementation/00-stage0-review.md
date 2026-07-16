# Stage 0 Review

> Scope: documentation review, development baseline confirmation, and implementation planning only. No business code, framework initialization, database creation, dependency installation, Docker setup, commit, or push was performed.

## Files Actually Read

- `AGENTS.md`
- `docs/stage-0/01-stage0-baseline.md`
- `docs/stage-0/02-requirements-specification.md`
- `docs/stage-0/03-business-process.md`
- `docs/stage-0/04-database-design.md`
- `docs/stage-0/05-api-design.md`

Root-level `AGENTS.md` now exists and is applied as the repository constitution. `README.md` does not currently exist.

## Project Positioning

WorkMate AI is an enterprise internal knowledge-base Q&A Agent MVP. It helps employees ask questions against curated internal knowledge, uses the retrieved knowledge to call an LLM, stores the conversation, caches repeated answers in Redis, and guides users to create manual consultation tickets when the knowledge base cannot answer reliably.

Target users:

- `EMPLOYEE`: internal staff who ask questions, manage their own sessions, and create/view their own tickets.
- `ADMIN`: operators who maintain categories and knowledge, process all tickets, and review model call logs.

Core value:

- Reduce employee time spent searching documents or asking colleagues.
- Reduce hallucination risk by forcing answers to be grounded in stored knowledge.
- Demonstrate a Java + AI Agent project with a complete business loop: user question, permission checks, persistence, knowledge retrieval, prompt assembly, LLM call, references, cache, async logs, and fallback ticket workflow.

First-version final deliverable:

- A deployable MVP with Spring Boot backend, Vue frontend, MySQL, Redis, Docker Compose, Nginx, demo data, README/deployment notes, and GitHub presentation material.

## MVP Scope

### Must Implement In V1

- Current-user lookup through simulated `X-User-Id`.
- Role lookup from `sys_user`; do not trust the frontend role.
- Knowledge category management.
- Knowledge management with pagination, keyword search, status changes, and logical deletion.
- Chat session creation, listing with `LEFT JOIN`, title update, logical deletion, and message listing.
- Agent Q&A flow using Redis lookup, MySQL `LIKE` knowledge search, Prompt construction, LLM HTTP call, answer persistence, knowledge references, trace steps, and model-call log recording.
- No-knowledge flow that does not call the LLM and returns `canCreateTicket=true`.
- Ticket creation, own-ticket query, admin all-ticket query, and valid status updates.
- Redis answer cache with 30-minute TTL, question normalization/hash, invalidation after knowledge/category changes, and degradation when Redis is unavailable.
- Async model call logs with `CACHE_HIT`, `SUCCESS`, `FAILED`, and `NO_KNOWLEDGE`.
- Unified response wrapper, global exception handling, Bean Validation, DTO/VO separation, and no Entity exposure.
- Docker Compose deployment with MySQL, Redis, Spring Boot, Vue, and Nginx.

### May Implement In V1 If Time Allows

- Model-log statistics endpoint and simple frontend statistics display. The API document marks statistics as lower priority than log writing and paginated query.
- Richer frontend presentation of Agent trace steps, as long as it does not expose chain of thought, prompts, secrets, or raw stack traces.

### Explicitly Excluded From V1

- Formal login, registration, JWT, Spring Security, RBAC, OAuth, verification codes.
- File upload, PDF parsing, Markdown batch import, Obsidian auto-sync.
- Embedding, vector database, Elasticsearch, hybrid retrieval.
- LangChain4j, complex Spring AI workflow, multi-Agent design.
- WebSocket, SSE.
- Kafka, RabbitMQ.
- Microservices, Spring Cloud, Kubernetes.
- Redis cluster, distributed locks, complex monitoring, GitHub Actions.

### Later-Version Candidates

- Real authentication and authorization.
- File or knowledge-base import pipeline.
- Embedding/vector search and improved RAG quality.
- Streaming responses.
- More complex Agent tools and workflows.
- CI/CD and production observability.

## Roles And Permissions

### EMPLOYEE

Can:

- Get current simulated user info.
- Query enabled categories and enabled knowledge.
- Create, list, update title, and logically delete own sessions.
- Ask the Agent in own sessions.
- View own messages.
- Create tickets after user confirmation.
- View own tickets and own ticket details.

Cannot:

- Manage categories.
- Create, update, enable/disable, or delete knowledge.
- View all tickets.
- Update ticket status.
- View model-call logs.
- Access other users' sessions, messages, or tickets.

### ADMIN

Can:

- Get current simulated user info.
- Query categories and knowledge.
- Manage categories and knowledge.
- Manage own sessions and ask the Agent.
- Create and view own tickets.
- View all tickets.
- Update ticket status according to the allowed state machine.
- View model-call logs and, if implemented, statistics.

Cannot:

- Bypass the same session/message ownership checks for personal chat data unless an API explicitly supports admin access.
- Perform invalid ticket status rollback.
- Execute `createTicket` through the model without explicit user confirmation.

### Authentication And Authorization

- V1 identity source is `X-User-Id`.
- Backend must load `sys_user` and verify user existence, enabled status, and role.
- Missing user header returns `40002`.
- Missing or disabled user returns `40003`.
- Permission failure returns `40301`.
- Formal login/JWT/Spring Security are excluded by the frozen stage 0 MVP, but this conflicts with `AGENTS.md`; see "Document Conflicts".

## Core Business Loop

### Normal Agent Flow

1. User submits a question with `sessionId`.
2. Backend validates `X-User-Id`.
3. Backend validates session exists and belongs to the current user.
4. Backend normalizes the question.
5. Backend saves `chat_message(USER)` and updates `chat_session.last_message_at`.
6. Backend queries Redis key `agent:answer:{questionHash}`.
7. On miss, backend searches enabled, non-deleted knowledge with enabled, non-deleted category by MySQL `LIKE` and `INNER JOIN`, limited to 5 records.
8. Backend builds a Prompt from retrieved knowledge.
9. Backend calls the LLM through `LlmClient`.
10. Backend saves `chat_message(ASSISTANT)`, saves `knowledge_reference`, and updates session time.
11. Backend writes Redis cache outside the database transaction.
12. Backend records model log asynchronously.
13. Backend returns answer, references, `fromCache`, `canCreateTicket`, and trace steps.

### Cache-Hit Flow

1. User question and session are validated.
2. User message is saved.
3. Redis answer is found.
4. Agent message and references are still saved for this conversation.
5. Session time is updated.
6. `CACHE_HIT` log is saved asynchronously.
7. Response returns `fromCache=true` and does not call the LLM.

### No-Knowledge Flow

1. User question and session are validated.
2. User message is saved.
3. Redis miss occurs.
4. MySQL knowledge search returns empty.
5. LLM is not called.
6. Knowledge-insufficient assistant message is saved.
7. `NO_KNOWLEDGE` log is saved asynchronously.
8. Response returns `canCreateTicket=true`.

### Model-Failure Flow

1. User question has already been saved.
2. Knowledge has been retrieved.
3. LLM call fails.
4. No fake assistant answer is saved.
5. `FAILED` log is saved asynchronously.
6. Response returns `50004`, `answerMessageId=null`, and `canCreateTicket=true`.

### Ticket Flow

1. Agent response returns `canCreateTicket=true`.
2. User explicitly confirms ticket creation.
3. Backend validates session ownership and question-message ownership.
4. Backend validates the source message belongs to the session and has role `USER`.
5. Backend creates `PENDING` ticket.
6. Admin may move `PENDING -> PROCESSING`, `PENDING -> RESOLVED`, `PROCESSING -> RESOLVED`, and `RESOLVED -> CLOSED`.
7. `RESOLVED` requires `resolution`, sets `handled_by`, and sets `resolved_at`.

## Functional Modules

Backend modules:

- Common response and error handling.
- User identity and permission checks.
- Knowledge category.
- Knowledge.
- Chat session and message.
- Agent Q&A.
- Redis cache.
- LLM client.
- Agent tools: `searchKnowledge`, `createTicket`.
- Ticket.
- Model-call log.
- Health check.

Frontend modules:

- Agent chat page.
- Knowledge management page.
- Category management page.
- Ticket management page.
- Model-call log page or simple statistics display.
- Axios API layer.

AI Agent modules:

- Question normalization and hash.
- Knowledge search tool.
- Prompt builder.
- `LlmClient` abstraction.
- Trace step generation.
- Ticket creation tool with explicit user confirmation.

Database modules:

- The fixed 8 tables listed below.
- SQL initialization and demo data.
- Mapper XML for required `INNER JOIN` and `LEFT JOIN`.

File or knowledge-base modules:

- V1 has manual/demo knowledge records only.
- No file upload, parsing, Markdown import, or Obsidian sync.

Admin/operations modules:

- Category and knowledge administration.
- Ticket administration.
- Model log review.
- Health check.

Deployment modules:

- Dockerfile.
- Docker Compose.
- Nginx reverse proxy.
- `.env.example` and `.gitignore` handling for secrets.
- README/deployment notes in a later stage.

## Data Entities

Fixed tables:

- `sys_user`: demo users, unique `username`, role/status index.
- `knowledge_category`: category metadata, unique logical name, status/delete indexes.
- `knowledge`: category-linked content, keywords, status, audit user fields, search/update indexes.
- `chat_session`: per-user session, logical delete, last-message timestamp indexes.
- `chat_message`: user/assistant messages, cache/ticket flags, logical delete, session/user/role indexes.
- `knowledge_reference`: assistant-message to knowledge relation, unique `(message_id, knowledge_id)`.
- `ticket`: manual consultation ticket, unique `ticket_no`, status/user/session indexes.
- `model_call_log`: async model/cache log, status/cache/session/user indexes.

Database design notes:

- Primary keys use `BIGINT AUTO_INCREMENT`.
- No physical MySQL foreign keys in V1; Service layer checks relationship integrity.
- Logical deletion applies to `knowledge_category`, `knowledge`, `chat_session`, `chat_message`, and `ticket`.
- `knowledge_reference` and `model_call_log` are not logically deleted.
- Audit timestamps are present on most mutable tables; `chat_message`, `knowledge_reference`, and `model_call_log` only define `created_at`.
- Data lifecycle: demo users and seed knowledge are initialized; knowledge/category/session/message/ticket are logically deleted; model logs are append-only in V1.

## API Modules

API groups:

- Users: `GET /api/users/current`.
- Knowledge categories: create/list/detail/update/status.
- Knowledge: create/update/detail/page/search/status/delete.
- Chat sessions: create/list/detail/title/delete/messages.
- Agent: `POST /api/agent/chat`.
- Tickets: create, my list, detail, admin list, admin status update.
- Model logs: admin page query and optional statistics.
- Health: `GET /api/health`.

Coverage assessment:

- API groups cover all documented MVP business use cases.
- Common response, pagination structure, status codes, DTO names, VO names, and permission matrix are documented.
- Some endpoints provide examples but not complete request/response schemas or all validation rules. This is not a blocker for Stage 1 foundation work, but each implementation task should freeze exact DTO/VO fields before coding that module.
- API document explicitly requires DTO/VO separation and does not require Entity exposure.

## Technical Stack

Frozen in stage 0 baseline:

- Backend: Java 17, Spring Boot 3, Spring Web, MyBatis-Plus, MySQL 8, Spring Data Redis, Bean Validation, Spring Async, Maven.
- Frontend: Vue 3, Vite, Axios, Element Plus. Pinia is not forced by the baseline.
- AI: HTTP LLM API through an independent `LlmClient` interface.
- Deployment: Docker, Docker Compose, Nginx, cloud server, GitHub.

`AGENTS.md` differs on Java/JWT/Pinia; see "Document Conflicts".

## Deployment Target

- Local development should support Spring Boot, MySQL, Redis, and Vue.
- Final deployment target is Docker Compose with backend, frontend/Nginx, MySQL, and Redis.
- Cloud server deployment and GitHub README/screenshots/deployment notes are expected in Stage 8.
- Health check must not call the LLM to avoid cost and latency.

## Document Conflicts

| ID | Conflict | Impact | Suggested Handling |
|---|---|---|---|
| C-001 | Earlier review found `AGENTS.md` only under `docs/`; it has since been moved to the repository root. | Location issue is resolved. | Keep root `AGENTS.md` as the canonical agent instruction file. |
| C-002 | Current instruction requires `README.md`; repository has no `README.md`. Stage 0 says README is part of final GitHub display in Stage 8. | Current review cannot read README; Stage 8 still needs README creation. | Treat missing README as expected before Stage 8, but record it as a repository baseline gap. |
| C-003 | `AGENTS.md` says backend uses Java 21 and JWT; stage 0 baseline freezes Java 17 and explicitly bans JWT/Spring Security/RBAC in V1. | Java version affects project initialization; JWT would expand MVP scope and contradict no-login requirement. | Confirm before Stage 1 initialization. Minimum-risk assumption for planning: follow frozen stage 0 MVP for Java 17 and no JWT unless the user updates the baseline. |
| C-004 | `AGENTS.md` includes Pinia in frontend stack; stage 0 baseline says first version does not force Pinia. | Could add unnecessary frontend state-management scope. | Do not require Pinia in V1 tasks unless a frontend task demonstrates a concrete need. |
| C-005 | `01-stage0-baseline.md` section 1 lists old Chinese filenames and a different document priority order from the current repository filenames and current user instruction. | Agents could apply the wrong priority chain or cite missing old filenames. | Use current instruction priority for this review; future docs should update the baseline filename/priority section. |

## Document Gaps

- `README.md` missing.
- Exact LLM provider/model, base URL, timeout, retry policy, and mock strategy are not frozen.
- Exact Java version conflict remains unresolved.
- Exact Maven artifact coordinates and package naming are suggested but not fully frozen beyond `com.workmate.ai`.
- Full DTO/VO field lists are suggested, not completely specified for every endpoint.
- Pagination default/max sizes are not frozen.
- Ticket number generation algorithm is not specified.
- Cache invalidation implementation strategy for `agent:answer:*` is not specified.
- Exact frontend routing/layout is not specified.
- Exact Docker port mapping and environment variable names are not specified.

## Executability Conclusion

The stage 0 documents are broadly sufficient to plan Stage 1 and later implementation. They define the MVP, business loops, tables, APIs, permissions, status machines, non-goals, and deployment target.

Stage 1 should not start with business feature implementation. The first implementation task should be a foundation-only preflight and backend skeleton task, but the Java version conflict should be resolved before initializing the actual Spring Boot project.

## Stage 1 Readiness

Conclusion: conditionally ready for Stage 1.

Conditions:

- Record the minimum assumption that V1 uses Java 17 and excludes JWT/Spring Security unless the user explicitly changes the frozen baseline.
- Resolve or explicitly accept the `README.md` gap.
- Keep Stage 1 limited to backend foundation and schema initialization; do not implement knowledge, session, Agent, Redis, or ticket business features early.
