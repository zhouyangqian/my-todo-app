# Repository Guidelines

## Project Structure & Module Organization

SaaS multi-tenant enterprise management system: Spring Cloud microservices backend with a Vue 3 frontend.

- `common/` -- Shared libraries (`common-core`, `common-mybatis`, `common-security`, `common-web`, `common-redis`, `common-feign`). Must be installed before other modules build.
- `services/` -- Microservices, each with a `*-service` (implementation) and `*-service-api` (shared DTOs/interfaces) submodule.
- `gateway/` -- Spring Cloud API Gateway (JWT validation, routing, rate limiting).
- `frontend/` -- Vue 3 + TypeScript + Vite. API calls in `src/api/`, views in `src/views/`, state in `src/stores/`.
- `specs/` -- Feature specifications. `init-scripts/` -- DB initialization SQL.

Each service follows a layered structure: `controller/` -> `service/` (interface + impl) -> `mapper/` -> `entity/` + `dto/` + `config/`.

## Build, Test, and Development Commands

**Backend** (Java 17, Maven):

```bash
mvn clean install -DskipTests   # Full build (common first, then services/gateway)
mvn test                        # Unit tests
mvn verify                      # Integration tests (Testcontainers)
cd services/auth-service && mvn spring-boot:run  # Run a single service
```

**Frontend** (Node 18+, pnpm 9+):

```bash
cd frontend
pnpm install
pnpm run dev            # Dev server on :3000
pnpm run test           # Vitest
pnpm run test:coverage  # Vitest with coverage
pnpm run build          # Production build
pnpm run lint           # ESLint with auto-fix
```

**Infrastructure**:

```bash
docker compose up -d mysql redis nacos  # MySQL :3306, Redis :6379, Nacos :8848
```

## Coding Style & Naming Conventions

- **Java**: Google Java Style, 4-space indent. Lombok for boilerplate, MapStruct for object mapping.
- **TypeScript/Vue**: ESLint with `eslint-plugin-vue`, 2-space indent. PascalCase components, camelCase utilities.
- **Naming**: Entities extend `BaseEntity`. DTOs use `*DTO`/`*VO` suffixes. Mappers use `*Mapper`.
- **Packages**: `com.example.{service-name}`. API responses wrapped in `ApiResponse<T>`.
- **Multi-tenancy**: Tenant ID from JWT; MyBatis interceptor auto-injects tenant conditions.

## Testing Guidelines

- **Backend**: JUnit 5 + Testcontainers. `mvn test` for unit tests, `mvn verify` for integration tests.
- **Frontend**: Vitest + Vue Test Utils. `pnpm run test` or `pnpm run test:coverage`.
- Name tests: `should_[expected]_when_[condition]`.
- Backend tests under `src/test/java` mirroring main package structure.

## Commit & Pull Request Guidelines

- **Format**: `type(scope): description` -- e.g., `feat(auth): add session management`.
- **Types**: `feat`, `fix`, `refactor`, `chore`, `docs`.
- **Scopes**: Service/module name in parentheses -- `(auth)`, `(erp)`, `(gateway)`, `(frontend)`.
- **Pull requests**: Clear description, linked issues, screenshots for UI changes.

## Security & Configuration Tips

- Never commit secrets. Use environment variables (`MYSQL_PASSWORD`, `JWT_SECRET`, `REDIS_HOST`) to override `application.yml` defaults.
- Gateway validates JWT on all routes except auth. Rate limit: 100 req/60s per IP.
- API docs at `/doc.html` on each service port (Knife4j/OpenAPI 3).