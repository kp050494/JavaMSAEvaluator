# 🚀 Free deployment — Vercel + Render + Neon (demo mode)

This hosts the platform for free with public URLs. Grading runs in **demo mode**
(heuristic, code-responsive pass/fail) because free hosts can't run the privileged
Judge0 sandbox. Everything else — login, challenges, live log/streaming UI, scoring,
session reports, recruiter dashboard — is fully real.

| Layer | Host | Free? |
|-------|------|-------|
| Frontend (React/Vite) | **Vercel** | yes |
| Backend (Spring Boot) | **Render** (Docker, free web service) | yes |
| PostgreSQL | **Neon** (serverless) | yes |

> For *real* Maven/JUnit execution you need the Judge0 stack (privileged Docker +
> ~3 GB RAM) — see the README's "custom Judge0 worker" section and run a VM instead.

---

## 0. Push the repo to GitHub
Render and Vercel deploy from a Git repo. Create one and push this project.

---

## 1. PostgreSQL on Neon
1. Sign up at https://neon.tech → **Create project**.
2. Copy the connection string, e.g.
   `postgresql://alex:npg_xxx@ep-cool-bird-12345.us-east-2.aws.neon.tech/neondb?sslmode=require`
3. Split it into the three values Render needs:
   - `SPRING_DATASOURCE_URL` = `jdbc:postgresql://ep-cool-bird-12345.us-east-2.aws.neon.tech/neondb?sslmode=require`
     (prefix with `jdbc:`, drop the `user:pass@`)
   - `SPRING_DATASOURCE_USERNAME` = `alex`
   - `SPRING_DATASOURCE_PASSWORD` = `npg_xxx`

Flyway creates all tables on first boot.

---

## 2. Backend on Render
1. Sign up at https://render.com → **New ➜ Blueprint**, point it at your repo.
   Render reads [`render.yaml`](render.yaml) and creates the `spring-arena-backend`
   web service (Docker, free plan, health check `/api/health`).
   *(Or: New ➜ Web Service ➜ Docker, Dockerfile `backend/Dockerfile`, context `.`)*
2. Set the environment variables it prompts for (`sync: false`):
   - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` (from Neon)
   - `CORS_ALLOWED_ORIGINS` = `*` (tighten to your Vercel URL after step 3)
   - `WEBSOCKET_ALLOWED_ORIGINS` = `*`
   - `EXECUTION_MODE` = `demo` (already set in the blueprint)
   - `JWT_SECRET` is auto-generated; `RECRUITER_ADMIN_USERNAME/PASSWORD` default to `admin`/`admin123`.
3. Deploy. First build runs `mvn package` (~5-8 min). When live, note the URL,
   e.g. `https://spring-arena-backend.onrender.com`. Verify:
   `curl https://spring-arena-backend.onrender.com/api/health` → `{"status":"UP","executionMode":"demo"}`

> Free Render services **sleep after ~15 min idle** and cold-start (~50s) on the next
> request — the first action after a nap will be slow, then it's snappy.

---

## 3. Frontend on Vercel
1. Sign up at https://vercel.com → **Add New ➜ Project**, import your repo.
2. Set **Root Directory** = `frontend` (Vercel auto-detects Vite; `vercel.json`
   handles the SPA rewrite).
3. Add an environment variable:
   - `VITE_API_URL` = your Render backend URL (e.g. `https://spring-arena-backend.onrender.com`)
4. Deploy. Note the URL, e.g. `https://spring-arena.vercel.app`.

---

## 4. Lock down CORS (optional but recommended)
Back in Render, set both `CORS_ALLOWED_ORIGINS` and `WEBSOCKET_ALLOWED_ORIGINS`
to your exact Vercel origin (`https://spring-arena.vercel.app`) and save (it redeploys).

---

## 5. Try it
- Open the Vercel URL.
- **Candidate:** "Candidate Assessment" → enter name/email → solve challenges →
  Run Tests streams a simulated pass/fail breakdown (paste the reference solutions
  to see `✅ CHALLENGE PASSED — n/n · 100%`).
- **Recruiter:** `/recruiter/login` → `admin` / `admin123` → dashboard + session reports.

---

## What "demo mode" changes
`EXECUTION_MODE=demo` swaps the Judge0 executor for a heuristic grader
(`DemoExecutor`) that pattern-matches your code against each challenge's required
constructs — so a correct solution passes and the empty starter fails, with the
same streaming UI. The log clearly states grading is simulated. Set
`EXECUTION_MODE=judge0` (the default) when running the full stack with the real
sandbox.
