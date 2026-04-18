# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SaaS multi-tenant enterprise management system (My Todo App). Spring Cloud microservices backend with Vue 3 frontend. Chinese-language business domain (ERP, finance, permissions).

## Build Commands

**Maven build order matters** — `common/` must be installed before `services/` and `gateway/`:

```bash
# Full build (from project root — pom.xml module ordering handles dependencies)
mvn clean install -DskipTests

# Build a single service (requires common modules in local Maven repo first)
cd services/auth-service && mvn compile

# Run tests
mvn test                    # unit tests
mvn verify                  # integration tests (uses Testcontainers)

# Run a single service
cd services/auth-service && mvn spring-boot:run
```

**Frontend** (requires Node 18+):

```bash
cd frontend
npm install
npm run dev                 # dev server on :3000, proxies API to :8080
npm run test                # Vitest
npm run test:coverage       # Vitest with coverage
npm run build               # production build
```

**Infrastructure** (Docker Compose):

```bash
docker compose up -d mysql redis nacos    # MySQL :3306, Redis :6379, Nacos :8848
```

MySQL auto-initializes databases via `init-scripts/01-init-databases.sql` on first run.

## Architecture

Multi-module Maven project: Spring Cloud microservices behind an API Gateway.

### Service Map

| Service | Port | Package | Database | Role |
|---------|------|---------|----------|------|
| gateway | 8080 | `com.example.gateway` | — | JWT validation, routing, rate limiting |
| auth-service | 8081 | `com.example.auth` | `my_todo_auth` | Login, register, token refresh |
| user-service | 8082 | `com.example.user` | `my_todo_user` | User CRUD, profiles |
| permission-service | 8083 | `com.example.permission` | `my_todo_permission` | Roles, permissions, departments |
| dict-service | 8084 | `com.example.dict` | `my_todo_dict` | System dictionaries, config |
| erp-service | 8085 | `com.example.erp` | `my_todo_erp` | Products, warehouses, purchase/sales orders |
| finance-service | 8086 | `com.example.finance` | `my_todo_finance` | AR/AP, invoices, payments |

**Startup order**: auth-service first (token validation dependency), gateway last (waits for service registry).

### Common Modules (`common/`)

Shared libraries that all services depend on:

- **common-core** — BaseEntity, ApiResponse, PageResult, BusinessException, ErrorCodes, SecurityConstants, JWT utilities
- **common-mybatis** — MyBatis-Plus config, Druid pool, MySQL connector, tenant interceptors
- **common-security** — Spring Security config, JWT token provider, custom auth exceptions
- **common-web** — Spring Web starter, Jakarta Validation, Knife4j/OpenAPI config, global exception handler
- **common-redis** — Spring Data Redis, Jedis, cache and distributed lock utilities
- **common-feign** — Spring Cloud OpenFeign, LoadBalancer, inter-service call interceptors

### Service Internal Structure

Every service follows the same layered pattern:

```
src/main/java/com/example/{service}/
├── controller/     # REST endpoints
├── service/        # Business logic (interface + impl)
├── mapper/         # MyBatis-Plus mapper interfaces
├── entity/         # JPA/MyBatis entities (extend BaseEntity)
├── dto/            # Request/response DTOs
└── config/         # Service-specific Spring config
src/main/resources/
├── application.yml # Service config (Nacos, MySQL, Redis)
└── mapper/         # MyBatis XML mapper files
```

### Gateway Routing

Gateway (`gateway/`) routes requests by path prefix to services via Spring Cloud Gateway. `TokenValidationFilter` validates JWT on all routes except auth. Rate limit: 100 requests/60s per IP (Redis-backed).

### Frontend Architecture

Vue 3 + TypeScript + Vite. Key patterns:
- **API layer**: `src/api/` — one module per backend service, uses Axios wrapper (`src/utils/request.ts`)
- **Routing**: `src/router/` — Vue Router with auth guard, NProgress
- **State**: Pinia stores in `src/stores/`
- **UI**: Element Plus with auto-import (configured in `vite.config.ts`)
- **Layout**: `src/layouts/BasicLayout.vue` — sidebar + main content
- **Views**: `src/views/` organized by business module (dashboard, system, dict, erp, finance)

## Key Conventions

- **Entities** extend `BaseEntity` which provides `id`, `createTime`, `updateTime`, `deleted` fields
- **MyBatis-Plus** handles logical delete via `deleted` field, auto ID generation
- **Mapper XML** location: `classpath*:/mapper/**/*.xml`
- **API responses** wrapped in `ApiResponse<T>` with unified error codes
- **Multi-tenancy**: tenant ID in JWT, MyBatis interceptor injects tenant conditions into queries
- **Config overrides**: environment variables (`MYSQL_HOST`, `REDIS_HOST`, `NACOS_SERVER`, `JWT_SECRET`) override `application.yml` defaults
- **API docs**: Knife4j at `/doc.html` on each service's port
- **Lombok** + **MapStruct** used throughout — annotation processors configured in parent POM
- **Chinese locale**: business terms, comments, and specs are in Chinese

## Tech Stack Versions

- Java 17, Spring Boot 3.2.0, Spring Cloud 2023.0.0, Spring Cloud Alibaba 2023.0.0.0-RC1
- MyBatis-Plus 3.5.5, MySQL 8.0.33, Druid 1.2.20, Redis 7+ (Lettuce/Jedis)
- Nacos 2.3.0 (service discovery + config center)
- JWT (jjwt 0.12.3), Hutool 5.8.24, MapStruct 1.5.5, Knife4j 4.4.0
- Vue 3.4, Vite 5.0, Element Plus 2.4, Pinia 2.1, Vitest 1.1

# CLAUDE.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
