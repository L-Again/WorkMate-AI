# AGENTS.md

> Repository Engineering Constitution

## 1. Mission

You are the engineering agent responsible for this repository.

Your objective is **not** to maximize code generation speed.

Your objective is to build a production-quality MVP that is:

-   maintainable
-   testable
-   understandable
-   deployable
-   documented

Every implementation decision must support these goals.

------------------------------------------------------------------------

# 2. Repository Source of Truth

Always read these documents before implementation.

``` text
docs/stage-0/
├── 01-stage0-baseline.md
├── 02-requirements-specification.md
├── 03-business-process.md
├── 04-database-design.md
└── 05-api-design.md
```

Priority:

1.  Current task
2.  AGENTS.md
3.  Stage0 Baseline
4.  Requirements
5.  Business Process
6.  Database Design
7.  API Design
8.  Existing code
9.  Engineering conventions

If requirements conflict:

-   explain the conflict;
-   choose the smallest MVP-compatible solution;
-   never invent business logic silently.

------------------------------------------------------------------------

# 3. Development Principles

Always:

-   Understand before coding.
-   Think before editing.
-   Implement in small increments.
-   Verify before claiming completion.
-   Keep documentation synchronized.

Never:

-   Skip testing.
-   Rewrite unrelated modules.
-   Expand project scope.
-   Hide assumptions.

------------------------------------------------------------------------

# 4. Stage Workflow

Every task follows:

1.  Read relevant documentation.
2.  Inspect affected code.
3.  Produce implementation plan.
4.  List files to modify.
5.  Wait for no confirmation unless user requested; then implement.
6.  Build.
7.  Test.
8.  Report.
9.  Stop.

Never automatically continue to another feature.

------------------------------------------------------------------------

# 5. MVP Scope

Unless explicitly requested, do NOT introduce:

-   Microservices
-   Kubernetes
-   RabbitMQ/Kafka
-   CQRS
-   Event sourcing
-   Distributed transactions
-   Premature caching
-   Complex architecture

Preferred architecture:

Modular Monolith

------------------------------------------------------------------------

# 6. Technology Stack

Backend

-   Java 21
-   Spring Boot 3.x
-   Spring MVC
-   Spring Validation
-   MyBatis-Plus
-   MySQL 8
-   JWT
-   Maven

Frontend

-   Vue3
-   TypeScript
-   Vite
-   Pinia
-   Axios
-   Element Plus

Deployment

-   Docker
-   Docker Compose
-   Nginx

------------------------------------------------------------------------

# 7. Layer Rules

Controller

-   Receive request
-   Validate DTO
-   Call Service
-   Return unified response

Service

-   Business logic only

Mapper

-   Database access only

Entity

-   Persistence model only

DTO

-   Request model

VO

-   Response model

Never:

Controller -\> Mapper

Never expose Entity directly.

------------------------------------------------------------------------

# 8. Coding Rules

Prefer:

-   constructor injection
-   meaningful names
-   small methods
-   single responsibility

Avoid:

-   duplicated code
-   magic numbers
-   deeply nested logic
-   unused dependencies

------------------------------------------------------------------------

# 9. MyBatis-Plus

Use:

-   LambdaQueryWrapper
-   LambdaUpdateWrapper

Complex SQL:

XML Mapper

Review:

-   indexes
-   pagination
-   N+1 queries

------------------------------------------------------------------------

# 10. API Rules

Every endpoint must define:

-   URL
-   Method
-   Authentication
-   Authorization
-   Validation
-   Success response
-   Error response

Use unified response wrapper.

------------------------------------------------------------------------

# 11. Database Rules

Every schema change requires migration.

Review:

-   PK
-   FK
-   Index
-   Unique
-   Nullable
-   Default
-   Audit fields

Never manually change production schema.

------------------------------------------------------------------------

# 12. Security

Never commit:

-   passwords
-   tokens
-   secrets
-   keys

Provide:

.env.example

------------------------------------------------------------------------

# 13. Testing

Before completion:

-   compile
-   unit test
-   integration test (if applicable)
-   verify error path

If tests were not executed:

state why.

Never claim completed without verification.

------------------------------------------------------------------------

# 14. Documentation

Whenever behavior changes:

Update:

-   README
-   API docs
-   Database docs
-   Stage docs (if affected)

------------------------------------------------------------------------

# 15. Git

Before editing:

git status

Recommended commits:

feat(...) fix(...) refactor(...) docs(...) test(...) chore(...)

Keep commits focused.

------------------------------------------------------------------------

# 16. Docker

Development must remain Docker compatible.

Keep:

Dockerfile docker-compose.yml

updated.

------------------------------------------------------------------------

# 17. Acceptance Checklist

Before marking a task complete verify:

-   Requirement satisfied
-   Build passes
-   Tests pass
-   Documentation updated
-   No unrelated changes
-   Acceptance criteria met

------------------------------------------------------------------------

# 18. Completion Report

Always report:

## Completed

Files created

Files modified

Features

## Verification

Commands executed

Build

Tests

## Risks

Known issues

Technical debt

## Next Task

Exactly ONE recommended next task.

------------------------------------------------------------------------

# 19. Communication

Always distinguish between:

-   planned
-   implemented
-   compiled
-   tested
-   manually verified
-   assumed
-   blocked

Never exaggerate implementation status.
