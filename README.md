# JALA — Shrimp Farm Management System

Backend for managing shrimp aquaculture operations: sites, ponds, grow-out
cycles, feeding, medicine, harvests, feed logistics and reporting.

**Stack:** Java 21 · Spring Boot 3.5 · PostgreSQL + Flyway · Spring Security (JWT) ·
MapStruct · Lombok · Supabase Storage (photos) · Maven

## Domain glossary

| Term | Meaning |
|---|---|
| **Site** | A farm location holding many ponds |
| **Pond** | A single grow-out pond, identified by `pondCode` |
| **Pond cycle** | One stocking-to-harvest run of a pond; only one ACTIVE cycle per pond (enforced by a partial unique index) |
| **Feed schedule / entry** | Planned feeding sessions per cycle and the actual feed drops recorded against them |
| **Medicine entry / photo** | Treatments applied to a cycle, with photo evidence |
| **Harvest** | Shrimp harvested from a cycle (quantity, buyer, revenue); completing one auto-creates the next cycle |
| **Feed delivery / site delivery / receipt** | Feed logistics: a delivery run, its per-site drops, and photo receipts |
| **Feed inventory** | Per-site running feed balance (delivered − consumed) |

## Module map

Package-by-feature under `backend/src/main/java/com/jala/backend/`:
`auth`, `user`, `role`, `siteaccess` (user↔site authorization), `site`, `pond`,
`pondcycle`, `feedschedule`, `feedentry`, `feedinventory`, `feeddelivery`,
`feeddeliveryreceipt`, `medicine`, `medicinephoto`, `harvest`, `dashboard`,
`analytics`, `history`, `reports`, `export`, `search`, `notification`,
`storage`, `security`, `common`.

Each module: `controller → service (interface + impl) → repository`, MapStruct
mapper, request/response DTOs. All responses use the `ApiResponse<T>` envelope.

## Security model

- Stateless JWT (`Authorization: Bearer <token>`); only `POST /api/v1/auth/login` is public.
- Tokens carry a **version claim** — deactivating a user or changing their role revokes outstanding tokens immediately.
- **Roles:** ADMIN, MANAGER (unrestricted) · SUPERVISOR, WORKER, DRIVER (restricted to sites assigned via `user_sites`; admins manage assignments with `POST/DELETE /api/v1/users/{id}/sites/{siteId}`).
- Login is rate-limited (5 failures → 15 min lockout).

## Running locally

```bash
# Option A: everything in Docker
docker compose up --build

# Option B: local JVM against your own Postgres
cd backend
cp .env.example .env.properties   # fill in values
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui` · Health: `/actuator/health`.

Environment variables are documented in [backend/.env.example](backend/.env.example).
The active profile (`dev`/`prod`) must be set explicitly via
`SPRING_PROFILES_ACTIVE` — there is deliberately no default.

## Tests

```bash
cd backend
./mvnw test              # unit + controller-slice tests (no Docker needed)
./mvnw verify            # + integration tests (*IT) on Testcontainers Postgres (Docker required)
./mvnw verify -DskipITs  # coverage-gated build without Docker
```

- Unit/slice tests run with mocked collaborators; integration tests boot real
  Postgres via Testcontainers and apply the production Flyway migrations.
- The build fails below **90% line coverage** (JaCoCo `check`; DTOs, config,
  mappers, constants and enums are excluded from measurement).

## CI

`.github/workflows/build.yml`: every PR runs `./mvnw verify` (tests +
coverage gate) and SonarCloud analysis with `sonar.qualitygate.wait=true`,
so a failing quality gate fails the build. Artifacts (jar + reports) are
uploaded per run.

## Database migrations

Flyway, `backend/src/main/resources/db/migration` (`V1`…`V26`). Never edit an
applied migration — add a new versioned file. Money/quantities use
`NUMERIC`; hot query paths are covered by indexes (see `V26`).

## Operational notes

- **Supabase bucket**: uploads are validated (path traversal, magic bytes,
  size, type) and object names are server-generated, but the bucket itself
  should be **private** with signed URLs served to clients — flip the bucket
  ACL in Supabase and switch read paths to signed URLs as a follow-up.
- Login rate limiting is in-memory (per instance); move to a shared store
  (e.g. Redis) when running multiple replicas.
