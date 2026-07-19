# JALA Frontend

Mobile-first, installable (PWA) React frontend for the JALA aqua-management
backend. Three role experiences share one codebase:

| Experience | Backend roles                | Routes         |
|------------|------------------------------|----------------|
| **Admin**  | `ADMIN`                      | `/admin/*`     |
| **User**   | `MANAGER`, `SUPERVISOR`, `WORKER` | `/app/*`  |
| **Driver** | `DRIVER`                     | `/driver/*`    |

## Stack

- **React 19** + **TypeScript** + **Vite 6**
- **React Router v6** (role-guarded routes)
- **Axios** client that unwraps the backend's `ApiResponse<T>` envelope and
  attaches the JWT bearer token
- **TanStack React Query v5** for server state
- **Tailwind CSS v4** + **shadcn/ui** (new-york) — the design system is ported
  verbatim from the source design
- **React Hook Form** + **Zod** for forms
- **vite-plugin-pwa** — installable via "Add to Home Screen"
- **Vitest** + **React Testing Library** + **MSW** — ≥90% coverage on app code

## Getting started

```bash
npm install --legacy-peer-deps   # React 19 peer ranges
cp .env.example .env             # optional; defaults work for same-origin
npm run dev                      # http://localhost:5173  (proxies /api -> :8080)
```

`--legacy-peer-deps` is required because some Radix/recharts peer ranges do not
yet list React 19; the packages work correctly with it.

### Scripts

| Script              | Purpose                                        |
|---------------------|------------------------------------------------|
| `npm run dev`       | Dev server with `/api` proxy to the backend    |
| `npm run build`     | Production build to `dist/`                     |
| `npm run preview`   | Serve the production build locally             |
| `npm run typecheck` | `tsc --noEmit`                                  |
| `npm test`          | Run the test suite                              |
| `npm run coverage`  | Run tests with a coverage report (90% gate)     |

## Configuration

| Env var              | Default        | Meaning                                             |
|----------------------|----------------|-----------------------------------------------------|
| `VITE_API_BASE_URL`  | `/api/v1`      | Base URL the SPA calls. Relative → same-origin.     |
| `VITE_DEV_BACKEND`   | `http://localhost:8080` | Dev only: origin the `/api` proxy targets. |

## Deployment scenarios

### 1. Docker (standalone + reverse-proxy) — recommended

The image serves the built SPA and reverse-proxies `/api` to the backend, so
the browser sees a single origin (no CORS needed).

```bash
docker build -t jala-frontend .
docker run -p 8080:80 -e BACKEND_URL=http://your-backend:8080 jala-frontend
```

`docker-compose` alongside the backend:

```yaml
services:
  backend:
    build: ./backend
    # ... backend config ...
  frontend:
    build: ./frontend
    ports: ["80:80"]
    environment:
      BACKEND_URL: http://backend:8080
    depends_on: [backend]
```

### 2. Cross-origin (SPA and API on different hosts)

Build with an absolute API base and allow the SPA origin on the backend:

```bash
VITE_API_BASE_URL=https://api.jala.example.com/api/v1 npm run build
# deploy dist/ to any static host (S3/CloudFront, Netlify, nginx, ...)
```

On the backend set `app.cors.allowed-origins` (env `APP_CORS_ALLOWED_ORIGINS`)
to the SPA origin, e.g. `https://app.jala.example.com`.

### 3. Served by Spring (same JVM)

Point the build output at the backend's static resources and let Spring serve
it. Because the backend's security is `anyRequest().authenticated()`, you must
(a) permit the static paths and (b) forward client-side routes to `index.html`.
Details and the exact snippet are in `DEPLOY_SPRING.md`.

## Project structure

```
src/
  api/         axios client, typed endpoints, React Query hooks, DTO types
  app/         providers (QueryClient, Auth), router, root redirect
  auth/        AuthContext, role→experience mapping, ProtectedRoute
  components/
    common/    StatCard, SiteSelector, state views, loader
    layout/    AppShell, TopBar, SideNav (desktop), BottomNav (mobile)
    ui/         shadcn/ui — ported from the design (excluded from coverage)
  features/
    auth/      LoginPage
    user/      UserHome, PondsPage, PondDetailPage
    driver/    DriverDeliveries
    admin/     AdminDashboard, SitesPage, UsersPage
    shared/    AlertsPage, ProfilePage
  hooks/       useSelectedSite (+ ported use-mobile/use-toast)
  lib/         formatters, cn util
  test/        MSW server + handlers, fixtures, render helpers
```

## Auth flow

`POST /api/v1/auth/login { employeeCode, password }` → `{ accessToken, role, ... }`.
The token is stored in `localStorage`, attached as `Authorization: Bearer` on
every request, and `GET /api/v1/auth/me` hydrates the session on reload. A `401`
clears the token and returns the user to `/login`.
