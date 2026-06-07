# 🏟️ Spring Arena — Live Java Coding Assessment Platform

A full-stack platform that gives candidates **six Java challenges** and grades them with
**real code execution**: the candidate's class is assembled with a hidden test harness into
a single Java program, **compiled and run** on a code-execution API, and the per-test
results stream back to the browser live over WebSocket. 100% free & open source — and
free to host.

```
React + Vite (Monaco)  ──REST/WS──►  Spring Boot API  ──HTTP──►  Code-exec API (Paiza/Piston)
                                          │
                                  PostgreSQL  (+ optional Redis)
```

---

## ✨ How real execution works

There is **no static analysis and no string matching** for the score. When a candidate
clicks **Run Tests**:

1. The backend loads that challenge's **harness** (`backend/src/main/resources/challenges/<slug>.harness.java`)
   — a single file containing any *given* classes plus a `public class Main` that
   instantiates the candidate's class, calls its methods, and prints
   `TEST_PASS::<name>` / `TEST_FAIL::<name>::<message>`.
2. The candidate's code is injected at the `// __CANDIDATE_CODE__` placeholder
   (`HarnessSupport` strips `package`/`import` lines and demotes any `public` top-level
   type so it inlines cleanly), producing one runnable `Main.java`.
3. That file is sent to a **code-execution API**, which **compiles and runs** it.
4. The backend parses the `TEST_PASS::` / `TEST_FAIL::` markers from stdout into per-test
   results, computes the score (`passed / total`), persists everything, and streams
   `COMPILING → RUNNING → TEST_RESULT… → COMPLETE` over STOMP/WebSocket.

### Execution modes (`EXECUTION_MODE`)

| Mode | Provider | Needs | Use |
|------|----------|-------|-----|
| `paiza` *(default)* | **Paiza.io** API (`api_key=guest`, no signup) | just outbound HTTP | local & free hosting |
| `piston` | a **self-hosted** Piston instance (`PISTON_EXECUTE_URL`) | your own Piston | private/high-volume |
| `judge0` | self-hosted **Judge0** (Maven/JUnit sandbox) | privileged Docker + custom worker | full Spring-style grading |
| `demo` | heuristic regex grader | nothing | offline showcase, no execution |

> The public **emkc.org Piston** API became **whitelist-only in Feb 2026**, so the default
> provider is **Paiza**. Point `EXECUTION_MODE=piston` + `PISTON_EXECUTE_URL` at your own
> Piston if you prefer to self-host.

---

## 🧩 The 6 Challenges (plain Java)

Each challenge asks the candidate to implement one small class. The harness (given classes +
tests) is server-side and never shown.

| # | Title | Difficulty | Category | Candidate implements |
|---|-------|------------|----------|----------------------|
| 1 | Product Catalogue | EASY | Collections | `ProductService` — `add`, `getAll`, `findById` (auto-increment ids) |
| 2 | Input Validation | EASY | Validation | `ProductValidator.validate(name, price)` → list of error messages |
| 3 | In-Memory Repository | MEDIUM | Data Filtering | `ProductRepository` — `save`/`findAll`/`findById`/`findByCategory`/`findByPriceLessThan` |
| 4 | Business Rules & Exceptions | MEDIUM | Exceptions | `OrderService.reserve(stock, qty)` (throws `IllegalArgument`/`IllegalState`) |
| 5 | Retry & Fallback | HARD | Resilience | `ResilientClient.callWithFallback(supplier, fallback, maxAttempts)` |
| 6 | Stateless Tokens | MEDIUM | Security | `TokenService` — `issue`/`isValid`/`subject` |

Reference solutions (answer key) live in [`challenge-solutions/`](challenge-solutions/).

---

## 📋 Prerequisites

- **Docker Desktop** (includes Docker Compose) — recommended path
- Outbound internet (the default `paiza` executor calls the Paiza.io API)
- For local dev without Docker: **Java 17+**, **Maven 3.9+**, **Node 18+**

---

## 🚀 Quick Start (Docker)

```bash
git clone <repo>
cd JavaMSAEvaluator
docker compose up --build
# Frontend: http://localhost:5173   Backend: http://localhost:8080
```

This starts **postgres + redis + backend (paiza mode) + frontend** — no privileged
containers. Flyway creates the schema and seeds the 6 challenges on first boot.

Stop and wipe data: `docker compose down -v`.

> Want the heavyweight Judge0/Maven path instead? `docker compose --profile judge0 up --build`
> and set `EXECUTION_MODE=judge0` (see the Judge0 section below).

---

## 🧑‍💻 Local Dev (without Docker)

```bash
# Terminal 1 — Postgres (+ optional Redis)
docker compose up postgres redis

# Terminal 2 — backend (defaults to EXECUTION_MODE=paiza)
cd backend && mvn spring-boot:run

# Terminal 3 — frontend (Vite proxies /api and /ws to :8080)
cd frontend && npm install && npm run dev
```

Root helper scripts: `npm run dev`, `npm run docker:up`, `npm run docker:down`.

---

## ☁️ Free Hosting

Host the whole thing for free **with real execution**: **Render** (backend Docker + frontend
static site) + **Supabase** (PostgreSQL) + **Upstash** (optional Redis) + **Paiza** (execution).
A Render Blueprint ([`render.yaml`](render.yaml)) defines both services.

👉 Full step-by-step in **[DEPLOY.md](DEPLOY.md)**.

Quick map:
- `EXECUTION_MODE=paiza` — real execution over plain HTTP, no privileged sandbox.
- `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` — Supabase (`?sslmode=require`).
- `SPRING_DATA_REDIS_URL=rediss://…` — Upstash (optional; Lettuce TLS).
- `VITE_API_URL` — the backend's public URL (frontend build-time).
- `CORS_ALLOWED_ORIGINS` / `WEBSOCKET_ALLOWED_ORIGINS` — your frontend origin.

---

## 🔐 Recruiter Login

```
URL:      /recruiter/login
Username: admin
Password: admin123
```
Seeded (and re-hashed with BCrypt) at startup by `DataInitializer`; override with
`RECRUITER_ADMIN_USERNAME` / `RECRUITER_ADMIN_PASSWORD`.

---

## 🎯 Candidate Flow

1. Open the app → **Candidate Assessment** → enter name + email (issues a session JWT).
2. Solve challenges in the **Monaco editor**.
3. **Run Tests** (`Ctrl+Enter`) compiles & runs your code and streams a live
   `✓ PASS / ✗ FAIL …  ✅ CHALLENGE PASSED — n/n · score%` breakdown.
4. **Submit All & Finish** → scored report (radar + per-challenge bars, code viewer).

### Keyboard shortcuts
`Ctrl+Enter` run · `Ctrl+H` hints · `Ctrl+1…6` switch challenge · `Ctrl+S` save snapshot.
Code auto-saves to `localStorage` per (session, challenge).

---

## 🌐 REST API

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET    | `/api/health` | — | liveness + execution mode |
| POST   | `/api/auth/login` | — | candidate login (name+email) → session JWT |
| POST   | `/api/auth/recruiter/login` | — | recruiter login |
| GET    | `/api/challenges` / `/api/challenges/{id}` | candidate | list / detail |
| POST   | `/api/submissions` | candidate | submit code → async execution (202) |
| GET    | `/api/submissions/{id}` | candidate | submission result |
| GET    | `/api/sessions/{id}` · `/report` · POST `/complete` | candidate | session + report |
| GET    | `/api/recruiter/sessions` · `/{id}` · `/{id}/export` | recruiter | dashboard + JSON export |

WebSocket (STOMP/SockJS): connect at `/ws`, subscribe to `/topic/submission/{sessionId}`.

---

## 🏗️ Architecture

```
                ┌───────────────────────────────────────────────────────────┐
  Browser ──────┼─►  Frontend (React + Monaco, static)                       │
  :5173         │        │  REST + WebSocket (SockJS/STOMP)                   │
                │        ▼                                                    │
                │   Backend — Spring Boot :8080                               │
                │     ├ SubmissionExecutor (paiza | piston | judge0 | demo)   │
                │     ├ HarnessSupport (assemble single-file Main.java)       │
                │     ├ WebSocket streaming + JWT security                    │
                │     └ JPA / Flyway                                          │
                │        │                         │                          │
                │        ▼                         ▼ HTTP                     │
                │   PostgreSQL (+ Redis)     Code-exec API (Paiza/Piston)     │
                │   Supabase    Upstash      compiles & runs Java             │
                └───────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack (100% free & open source)

**Backend:** Java 17, Spring Boot 3.2, Spring Web / Data JPA / Security / WebSocket,
PostgreSQL, Flyway, JJWT, Lombok; pluggable executors calling Paiza/Piston/Judge0.
**Frontend:** React 18, TypeScript, Vite, TailwindCSS, Monaco Editor, SockJS + STOMP.js,
Axios, Recharts, React Router v6.
**Execution:** Paiza.io API (default), Piston (self-host), or Judge0 (Maven/JUnit).
**Hosting:** Render + Supabase + Upstash (all free tiers).

---

## 🗂️ Project Layout

```
.
├── docker-compose.yml          # postgres, redis, backend, frontend (+ judge0 profile)
├── render.yaml                 # Render Blueprint: backend (Docker) + frontend (static)
├── DEPLOY.md                   # free deploy guide (Render + Supabase + Upstash + Paiza)
├── challenge-solutions/        # reference answers for the 6 challenges
├── backend/                    # Spring Boot API
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/assessment/{config,controller,service,model,repository,security,dto,websocket,converter,exception}
│       │   service/ ── PaizaService, PaizaExecutor, PistonService, PistonExecutor,
│       │                Judge0Service, Judge0Executor, DemoExecutor, HarnessSupport,
│       │                SubmissionExecutor, SubmissionProcessor, …
│       └── resources/
│           ├── challenges/challenge-{1..6}.harness.java   # server-side test harnesses
│           └── db/migration/V1..V8                        # schema + challenge content
├── frontend/                   # React + Vite SPA (api, hooks, components, pages, store, types)
├── docker/judge0/Dockerfile    # custom Judge0 worker (JDK17 + Maven + warmed repo)
└── challenge-tests/            # legacy Spring/Maven templates (only used by EXECUTION_MODE=judge0)
```

---

## 🧪 Judge0 mode (optional, heavyweight)

The original design ran full **Spring Boot + MockMvc/JUnit** suites in a privileged Judge0
sandbox. That still works: build the custom worker (JDK 17 + Maven + a warmed repo via
`scripts/warm-and-test.sh`), then:

```bash
EXECUTION_MODE=judge0 docker compose --profile judge0 up --build
```

It needs privileged Docker and ~2–4 GB RAM, which is why the default (and the free-hosting
path) uses the lightweight Paiza executor instead.

---

## ✅ Verifying locally

```bash
# Backend compiles / packages
mvn -q -f backend/pom.xml -DskipTests package
# Frontend type-checks + builds
cd frontend && npm install && npm run build
# End-to-end: open http://localhost:5173, run a challenge, paste a challenge-solutions/ answer → 100%
```
