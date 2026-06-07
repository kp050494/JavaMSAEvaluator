# 🚀 Free deployment — Render + Supabase + Upstash + Paiza

This hosts the whole platform for free **with real code execution**. Submissions are
compiled and run as single-file Java programs by an open code-execution API over plain
HTTP — no privileged sandbox required — so it runs on free tiers.

> **Execution provider:** the default is **Paiza.io** (`EXECUTION_MODE=paiza`, open,
> `api_key=guest`, no signup). The public **Piston** API (`emkc.org`) went
> whitelist-only in Feb 2026, so use it only if you self-host Piston
> (`EXECUTION_MODE=piston` + `PISTON_EXECUTE_URL`).

| Layer | Host | Free? |
|-------|------|-------|
| Frontend (React/Vite) | **Render Static Site** | yes |
| Backend (Spring Boot) | **Render** (Docker web service) | yes |
| Code execution | **Paiza.io** (open API) | yes |
| PostgreSQL | **Supabase** | yes |
| Redis (optional) | **Upstash** | yes |

Everything is wired through [`render.yaml`](render.yaml) (a Render Blueprint that
creates both services).

---

## 0. Push the repo to GitHub
Render deploys from a Git repo. Create one and push this project.

---

## 1. PostgreSQL on Supabase
1. https://supabase.com → **New project** (pick a region, set a DB password).
2. **Project Settings → Database → Connection string → JDBC** (or build it yourself).
   You need three values for Render:
   - `SPRING_DATASOURCE_URL` = `jdbc:postgresql://<host>:5432/postgres?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME` = `postgres` (or the pooler user Supabase shows, e.g. `postgres.abcd1234`)
   - `SPRING_DATASOURCE_PASSWORD` = your DB password

Flyway creates all tables (and the V7 challenge content) on first boot.

---

## 2. Redis on Upstash (optional)
The app works without Redis, but to wire it:
1. https://upstash.com → **Create database** (Redis, pick a region, **TLS on**).
2. Copy the connection URL and use the `rediss://` form:
   - `SPRING_DATA_REDIS_URL` = `rediss://default:<PASSWORD>@<host>:<port>`
   (Spring Boot auto-configures Lettuce with TLS from this URL.)
If you skip this, just leave `SPRING_DATA_REDIS_URL` unset.

---

## 3. Deploy on Render (Blueprint = backend + frontend)
1. https://render.com → **New ➜ Blueprint**, point it at your repo. Render reads
   `render.yaml` and creates **`spring-arena-backend`** (Docker) and
   **`spring-arena-frontend`** (static site).
2. On **`spring-arena-backend`**, set the `sync: false` env vars:
   - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` (Supabase)
   - `SPRING_DATA_REDIS_URL` (Upstash, or leave blank)
   - `CORS_ALLOWED_ORIGINS` = `*` and `WEBSOCKET_ALLOWED_ORIGINS` = `*` (tighten in step 5)
   - `EXECUTION_MODE=piston` and `PISTON_EXECUTE_URL` are already set by the blueprint.
3. Deploy the backend (first build runs `mvn package`, ~5–8 min). Verify:
   `https://<backend>.onrender.com/api/health` → `{"status":"UP","executionMode":"paiza"}`

> Free Render services **sleep after ~15 min idle** and cold-start (~50s) on the next
> request — the first action after a nap is slow, then snappy.

---

## 4. Point the frontend at the backend
1. Copy the backend URL (e.g. `https://spring-arena-backend.onrender.com`).
2. On **`spring-arena-frontend`** → Environment, set:
   - `VITE_API_URL` = that backend URL (no trailing slash, no `/api`).
3. **Redeploy the frontend** (`VITE_API_URL` is baked in at build time). Note its URL,
   e.g. `https://spring-arena-frontend.onrender.com`.

---

## 5. Lock down CORS (recommended)
On the backend, set `CORS_ALLOWED_ORIGINS` and `WEBSOCKET_ALLOWED_ORIGINS` to the
frontend URL from step 4 and save (it redeploys).

---

## 6. Try it
- Open the frontend URL.
- **Candidate:** enter name/email → solve a challenge → **Run Tests** compiles & runs
  your Java on Piston and streams `✓ PASS / ✗ FAIL … ✅ CHALLENGE PASSED`.
- **Recruiter:** `/recruiter/login` → `admin` / `admin123`.

---

## Notes
- **Real execution:** each submission is assembled into a single Java file (`Main.java`)
  from the challenge's harness + your class, then compiled and run. Markers
  `TEST_PASS::` / `TEST_FAIL::` printed to stdout are parsed into the score.
- **Provider limits:** the public Paiza/Piston endpoints are shared and rate-limited
  (best-effort). For heavier or private use, self-host Piston and set
  `EXECUTION_MODE=piston` + `PISTON_EXECUTE_URL=https://<your-piston>/api/v2/execute`.
- **Other modes:** `EXECUTION_MODE=judge0` (self-hosted Judge0 Maven/JUnit) and
  `EXECUTION_MODE=demo` (heuristic, no execution) are still available — see the README.
