# Acceptance Checklist

Use this checklist as the stage gate before moving to the next phase. Items are intentionally explicit so a stage cannot pass only by code inspection.

## Stage 0: 需求与设计

- [ ] Required stage 0 files exist.
- [ ] `AGENTS.md` location issue is resolved or recorded.
- [ ] `README.md` absence is resolved or recorded.
- [ ] Java version conflict is resolved or recorded as an assumption.
- [ ] MVP scope and non-goals are documented.
- [ ] No business code or framework initialization was added during review.

## Stage 1: 后端基础工程

Functional:

- [ ] Backend skeleton starts.
- [ ] Package structure follows `com.workmate.ai`.
- [ ] `GET /api/users/current` works with `X-User-Id`.
- [ ] `GET /api/health` does not call the LLM.

Database:

- [ ] Exactly 8 MVP tables exist.
- [ ] Seed users `employee_demo` and `admin_demo` exist.
- [ ] Initial demo categories and knowledge exist.
- [ ] No physical foreign keys are added.

API:

- [ ] Common response wrapper is used.
- [ ] Documented error codes are represented.
- [ ] Validation errors return `40001`.

Permissions:

- [ ] Missing user header returns `40002`.
- [ ] Missing/disabled user returns `40003`.
- [ ] No JWT/Spring Security is added unless baseline is changed.

Testing/build:

- [ ] Backend tests pass.
- [ ] Backend compiles.
- [ ] Application startup is verified.

Security/docs:

- [ ] No secrets are committed.
- [ ] `.env.example` plan exists if environment variables are introduced.

## Stage 2: 知识库 CRUD

Functional:

- [ ] Admin can create, update, enable/disable, and delete categories.
- [ ] Admin can create, update, enable/disable, delete, page, search, and view knowledge.
- [ ] Employee can query enabled categories and knowledge.

Database:

- [ ] Category uniqueness respects `(name, is_deleted)`.
- [ ] Knowledge logical delete hides records.
- [ ] Knowledge mutations set `created_by`/`updated_by` from current user.

API:

- [ ] Category endpoints match API section 5.
- [ ] Knowledge endpoints match API section 6.
- [ ] Pagination response matches `PageResult<T>` shape.

Permissions:

- [ ] Employee mutations are rejected with `40301`.
- [ ] Disabled/deleted categories and knowledge are excluded from effective search.

Testing:

- [ ] Validation failure tests pass.
- [ ] Permission tests pass.
- [ ] Required `INNER JOIN` query is implemented and verified.

## Stage 3: 会话与消息

Functional:

- [ ] User can create an empty session.
- [ ] User can list own sessions.
- [ ] User can update own session title.
- [ ] User can logically delete own session.
- [ ] User can query own messages.

Database:

- [ ] Session list uses `LEFT JOIN` to include empty sessions.
- [ ] Message ordering is `created_at ASC, id ASC`.
- [ ] Deleting a session does not physically delete messages.

API:

- [ ] Session endpoints match API section 7.
- [ ] Message pagination works.

Permissions:

- [ ] Cross-user session access is denied.
- [ ] Cross-user message access is denied.

Testing:

- [ ] Empty-session list test passes.
- [ ] Last-message query test passes.
- [ ] Cross-user denial tests pass.

## Stage 4: 大模型问答

Functional:

- [ ] User question validates user and session ownership.
- [ ] User message is saved before knowledge/LLM processing.
- [ ] Knowledge search returns up to 5 effective records.
- [ ] Prompt is built from retrieved knowledge.
- [ ] LLM is called only when knowledge exists.
- [ ] Assistant answer and references are saved on success.
- [ ] Trace steps are returned.

Data:

- [ ] `knowledge_reference` is saved only after assistant message is saved.
- [ ] Model failure does not save fake assistant answer.

API:

- [ ] `POST /api/agent/chat` matches normal/no-knowledge/failure response contracts.
- [ ] `answerMessageId` is null on model failure.

Security:

- [ ] Trace does not expose chain of thought.
- [ ] Trace does not expose full system prompt.
- [ ] Trace does not expose API keys, raw stack traces, or sensitive headers.

Testing:

- [ ] Mock LLM success test passes.
- [ ] No-knowledge test proves LLM is not called.
- [ ] LLM failure test proves no fake answer is saved.

## Stage 5: Redis

Functional:

- [ ] Cache key uses normalized question hash.
- [ ] Cache TTL is 30 minutes.
- [ ] Cache value contains answer and references only.
- [ ] First request returns `fromCache=false`.
- [ ] Second identical request returns `fromCache=true`.
- [ ] Cache hit saves user and assistant messages.
- [ ] Cache hit does not call the LLM.
- [ ] Knowledge/category changes clear `agent:answer:*`.
- [ ] Redis failure degrades to database search and LLM call.

Testing:

- [ ] Normal cache miss/hit tests pass.
- [ ] Invalidation tests pass.
- [ ] Redis failure fallback test passes.

## Stage 6: Agent 工具、工单与异步日志

Functional:

- [ ] `AgentTool` interface exists.
- [ ] `searchKnowledge` tool works.
- [ ] `createTicket` requires explicit user confirmation.
- [ ] Employee can create tickets from valid own source messages.
- [ ] Employee can view own tickets.
- [ ] Admin can view all tickets.
- [ ] Admin can update ticket status.
- [ ] Model logs are saved asynchronously.

Data/status:

- [ ] Ticket numbers are unique.
- [ ] Allowed transitions are enforced: `PENDING -> PROCESSING`, `PENDING -> RESOLVED`, `PROCESSING -> RESOLVED`, `RESOLVED -> CLOSED`.
- [ ] Invalid rollback is rejected.
- [ ] `RESOLVED` requires `resolution`, sets `handled_by`, and sets `resolved_at`.
- [ ] Logs cover `CACHE_HIT`, `SUCCESS`, `FAILED`, and `NO_KNOWLEDGE`.

Permissions:

- [ ] Employee cannot view all tickets.
- [ ] Employee cannot update tickets.
- [ ] Ticket creation validates session ownership, message ownership, message session, and message role `USER`.
- [ ] Model logs are admin-only.

Testing:

- [ ] Ticket creation tests pass.
- [ ] Ticket state-machine tests pass.
- [ ] Async log tests pass.
- [ ] Async log failure does not fail main response.

## Stage 7: Vue 与联调

Functional:

- [ ] Chat page supports session list, messages, ask flow, references, and trace.
- [ ] No-knowledge response allows user-confirmed ticket creation.
- [ ] Knowledge management page supports admin CRUD.
- [ ] Category management page supports admin CRUD/status.
- [ ] Ticket management page supports employee own tickets and admin handling.
- [ ] Model log page or simple statistics display exists.

API/integration:

- [ ] Axios base path is `/api`.
- [ ] Demo `X-User-Id` switching is controlled and visible to testers.
- [ ] Frontend handles documented error codes.

Build/testing:

- [ ] Frontend build passes.
- [ ] Browser smoke tests cover employee happy path.
- [ ] Browser smoke tests cover admin management path.

Security:

- [ ] No API keys or production credentials are embedded in frontend code.

## Stage 8: Docker 与项目包装

Docker/build:

- [ ] Backend Dockerfile works.
- [ ] Frontend/Nginx Dockerfile or equivalent works.
- [ ] `docker-compose.yml` starts MySQL, Redis, backend, and frontend/Nginx.
- [ ] Health endpoint reports expected application/MySQL/Redis status.

Configuration/security:

- [ ] `.env.example` exists.
- [ ] `.env` is ignored by Git.
- [ ] No secrets, API keys, passwords, SSH keys, or private certificates are committed.

Documentation:

- [ ] Root `README.md` exists.
- [ ] README explains local development.
- [ ] README explains Docker Compose deployment.
- [ ] README lists demo users.
- [ ] README includes screenshots or demo notes.
- [ ] API/deployment caveats are documented.

Deployment:

- [ ] Cloud server deployment steps are documented.
- [ ] GitHub repository presentation is complete.
- [ ] No GitHub Actions are added in V1 unless baseline changes.
