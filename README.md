# 🏟️ Spring Arena — Live Java Microservices Assessment Platform

A full-stack platform that gives candidates **six Spring Boot challenges** and grades
them with **real code execution** — their code is injected into pre-built Maven
projects, compiled, and run against **JUnit 5 + Spring MockMvc** suites via a
self-hosted **Judge0 CE** engine. Results stream back to the browser live over
WebSocket. 100% free & open source.

```
React + Vite (Monaco)  ──REST/WS──►  Spring Boot API  ──►  Judge0 CE  ──► Maven + JUnit
                                          │
                                  PostgreSQL + Redis
```

---

## ✨ What makes it "real"

There is **no static analysis and no string matching**. When a candidate clicks
**Run Tests**:

1. Their code is injected into a complete Maven project under `challenge-tests/`.
2. The whole project is zipped, base64-encoded, and sent to **Judge0 CE**.
3. Judge0 compiles it and runs the real **JUnit 5 / MockMvc** suite.
4. A JUnit `TestWatcher` prints machine-readable `JUNIT_RESULT::` markers to stdout.
5. The backend parses those markers, persists per-test results, and streams
   `COMPILING → RUNNING_TESTS → TEST_RESULT → COMPLETE` to the UI over STOMP/WebSocket.
6. The score is `passed / total` of actual test outcomes.

---

## 📋 Prerequisites

- **Docker Desktop** (includes Docker Compose) — recommended path
- **Java 17+** and **Maven 3.9+** — only for local dev without Docker
- **Node 18+** — only for local dev without Docker
- **Git**

---

## 🚀 Quick Start (Docker — recommended)

```bash
git clone <repo>
cd JavaMSAEvaluator
cp .env.example .env          # adjust JWT_SECRET / passwords if you like

# 1. Warm a Maven repo for the 6 challenges (also runs the suites once).
#    Produces ./.m2warm, which is baked into the custom Judge0 worker image.
bash scripts/warm-and-test.sh

# 2. Build + start everything.
docker compose up --build
# ⏳ Wait ~2-3 minutes for Judge0 to initialise its DB on first boot.
```

> **Why the warm step?** Real test execution needs a Java 17 + Maven toolchain
> *inside* the Judge0 sandbox, which is network-isolated. The custom worker image
> (`docker/judge0/Dockerfile`) bakes in JDK 17, Maven, and the pre-warmed offline
> repository so `mvn -o test` runs without network access. See
> [Real execution & the custom Judge0 worker](#-real-execution--the-custom-judge0-worker).

| Service       | URL                     |
|---------------|-------------------------|
| Frontend      | http://localhost:5173   |
| Backend API   | http://localhost:8080   |
| Judge0 CE     | http://localhost:2358   |
| PostgreSQL    | localhost:5432          |
| Redis         | localhost:6379          |

Stop and wipe volumes: `docker compose down -v`.

---

## 🧑‍💻 Local Dev (without Docker)

```bash
# Terminal 1 — infrastructure + execution engine
docker compose up postgres redis judge0-server judge0-workers

# Terminal 2 — backend (reads challenge-tests from ../challenge-tests by default)
cd backend && mvn spring-boot:run

# Terminal 3 — frontend (Vite proxies /api and /ws to :8080)
cd frontend && npm install && npm run dev
```

Or, with the root helper scripts:

```bash
npm run dev        # runs backend + frontend together (needs infra already up)
npm run docker:up  # docker compose up --build
npm run docker:down
```

---

## 🔐 Recruiter Login

```
URL:      http://localhost:5173/recruiter/login
Username: admin
Password: admin123
```

The admin account is seeded (and its password re-hashed with BCrypt) on startup by
`DataInitializer`, so the documented credentials always work. Change them via the
`RECRUITER_ADMIN_USERNAME` / `RECRUITER_ADMIN_PASSWORD` environment variables.

---

## 🎯 Candidate Flow

1. Open http://localhost:5173 → **Candidate Assessment**.
2. Enter name + email → a session JWT is issued and a `CandidateSession` is created.
3. Solve challenges in the **Monaco editor** (VS Code in the browser).
4. Click **Run Tests** (or `Ctrl+Enter`) — code is compiled and tested *for real*.
   Watch the live log + animated pass/fail list.
5. Click **Submit All & Finish** → the session is scored and you land on the report.

### Keyboard shortcuts

| Shortcut       | Action                      |
|----------------|-----------------------------|
| `Ctrl+Enter`   | Run tests for the challenge |
| `Ctrl+H`       | Toggle hints drawer         |
| `Ctrl+1…6`     | Switch to challenge N       |
| `Ctrl+S`       | Force-save code snapshot    |

Code is auto-saved to `localStorage` every 30s per (session, challenge).

---

## 🧩 The 6 Challenges

| # | Title                              | Difficulty | Category        | What it tests |
|---|------------------------------------|------------|-----------------|---------------|
| 1 | REST Controller Basics             | EASY       | REST API        | `@RestController`, `@GetMapping`/`@PostMapping`, `@PathVariable`, `ResponseEntity`, 404 via `ResponseStatusException` |
| 2 | Service Layer & Bean Validation    | EASY       | Validation      | `@Service` + constructor injection, `@Valid`, `@NotBlank`/`@Positive` → 400, 201 Created |
| 3 | Spring Data JPA Repository         | MEDIUM     | Persistence     | `JpaRepository`, derived queries `findByCategory` / `findByPriceLessThan`, H2 schema |
| 4 | Global Exception Handling          | MEDIUM     | Error Handling  | `@RestControllerAdvice`, consistent `ErrorResponse` (timestamp/status/message/path), field errors, 500 mapping |
| 5 | Resilience: Circuit Breaker        | HARD       | Resilience      | Resilience4j `@CircuitBreaker` + fallback, RestTemplate timeouts (verified with WireMock) |
| 6 | Stateless JWT Security             | MEDIUM     | Security        | Spring Security 6 `SecurityFilterChain`, `OncePerRequestFilter`, JWT issue/verify, 401 on missing/expired |

Each challenge folder under `challenge-tests/` is a complete Maven project:

```
challenge-tests/challenge-1/
├── pom.xml
├── src/main/java/com/assessment/
│   ├── Application.java            ← always present
│   ├── model/Product.java         ← provided (fixed)
│   └── CANDIDATE_SUBMISSION.java   ← replaced at runtime with candidate code
└── src/test/java/com/assessment/
    ├── ProductControllerTest.java  ← pre-written MockMvc suite
    └── support/ResultPrinter.java  ← emits JUNIT_RESULT:: markers to stdout
```

> The reference implementation inside `CANDIDATE_SUBMISSION.java` keeps each
> template compilable and serves as the "correct answer" the suite targets.
> When a candidate submits a `public` type, the backend names the file after it
> so `javac` is satisfied.

---

## 🌐 REST API

| Method | Endpoint                                   | Auth      | Purpose |
|--------|--------------------------------------------|-----------|---------|
| POST   | `/api/auth/login`                          | —         | Candidate login (name+email) → session JWT |
| POST   | `/api/auth/recruiter/login`                | —         | Recruiter login (admin/admin123) |
| GET    | `/api/challenges`                          | candidate | List all challenges |
| GET    | `/api/challenges/{id}`                      | candidate | Challenge detail (description, hints, concepts, test cases) |
| POST   | `/api/submissions`                         | candidate | Submit code → async Judge0 run (202) |
| GET    | `/api/submissions/{id}`                     | candidate | Submission result |
| GET    | `/api/sessions/{sessionId}`                 | candidate | Full session with submissions |
| GET    | `/api/sessions/{sessionId}/report`          | candidate | Computed report |
| POST   | `/api/sessions/{sessionId}/complete`        | candidate | End session, return report |
| GET    | `/api/recruiter/sessions`                   | recruiter | All sessions |
| GET    | `/api/recruiter/sessions/{id}`              | recruiter | Full session report |
| GET    | `/api/recruiter/sessions/{id}/export`       | recruiter | Export report as downloadable JSON |

WebSocket (STOMP over SockJS): connect at `/ws`, subscribe to
`/topic/submission/{sessionId}`.

---

## 🏗️ Architecture

```
                ┌──────────────────────────────────────────────────────────┐
                │                   assessment-network                       │
                │                                                            │
  Browser ──────┼──►  ┌─────────────┐   REST/WS    ┌──────────────────────┐ │
  :5173         │     │  frontend    │ ───────────► │      backend          │ │
  (nginx/Vite)  │     │ React+Monaco │              │  Spring Boot :8080     │ │
                │     └─────────────┘              │  ├ Judge0Service        │ │
                │                                   │  ├ CodeInjectionService │ │
                │                                   │  ├ WebSocket (STOMP)    │ │
                │                                   │  └ JWT security         │ │
                │                                   └───────┬───────┬────────┘ │
                │                                           │       │          │
                │                  ┌────────────────────────┘       │          │
                │                  ▼                                 ▼          │
                │      ┌─────────────────────┐            ┌──────────────────┐ │
                │      │     Judge0 CE        │            │  PostgreSQL :5432 │ │
                │      │  server + workers    │            │  assessment_db    │ │
                │      │  (compiles & runs    │            │  judge0 (db)      │ │
                │      │   Maven + JUnit)     │            └──────────────────┘ │
                │      └──────────┬──────────┘            ┌──────────────────┐ │
                │                 └──────────────────────►│    Redis :6379    │ │
                │                  (shared job queue)      │ sessions + queue  │ │
                │                                          └──────────────────┘ │
                └──────────────────────────────────────────────────────────┘
```

- **PostgreSQL** hosts both `assessment_db` (the app, via Flyway migrations) and a
  separate `judge0` database on the same instance.
- **Redis** is shared between the backend (caching/pub-sub) and Judge0 (job queue).
- The backend image **bundles `challenge-tests/`** at `/challenge-tests` so
  `CodeInjectionService` can read the templates at runtime.

---

## 🛠️ Tech Stack (100% free & open source)

**Backend:** Java 17, Spring Boot 3.2, Spring Web/Data JPA/Security/WebSocket,
PostgreSQL 15, Redis 7, Flyway, JJWT, Lombok.
**Frontend:** React 18, TypeScript, Vite, TailwindCSS, Monaco Editor, SockJS +
STOMP.js, Axios, Recharts, React Router v6.
**Execution:** Judge0 CE 1.13.0 (self-hosted), Maven, JUnit 5, MockMvc, AssertJ,
WireMock (challenge 5), Resilience4j.
**Runtime:** Docker + Docker Compose.

---

## 🧪 Real execution & the custom Judge0 worker

Judge0 CE runs each submission in an isolated, **network-isolated** `isolate`
sandbox, and stock CE only ships **JDK 13** with **no Maven**. The challenges target
Java 17 and need Maven, so this project ships a **custom worker image**
([`docker/judge0/Dockerfile`](docker/judge0/Dockerfile)) built on
`judge0/judge0:1.13.0` that bakes in, under `/usr/local` (which the sandbox exposes
read-only):

- **JDK 17** (`/usr/local/jdk17`)
- **Maven 3.9** (`/usr/local/maven`)
- a **pre-warmed offline repository** (`/usr/local/m2repo`, ~90 MB) produced by
  `scripts/warm-and-test.sh`

Each submission is sent as a **multi-file submission** (`language_id` 89) whose
`run` script does:

```bash
export JAVA_HOME=/usr/local/jdk17
export PATH=$JAVA_HOME/bin:/usr/local/maven/bin:$PATH
mvn -o -q -Dsurefire.useFile=false -Dmaven.repo.local=/usr/local/m2repo test
```

`judge0.conf` raises the sandbox caps to fit a Spring Boot test run
(`MAX_CPU_TIME_LIMIT=120`, `MAX_WALL_TIME_LIMIT=300`, `MAX_MEMORY_LIMIT=2048000`,
`MAX_MAX_PROCESSES_AND_OR_THREADS=256`), and the backend sends matching per-submission
limits. A full run takes ~25-35s of CPU and ~380 MB.

This has been verified end-to-end: submitting the reference solution for challenge 1
scores **100 (3/3)**, deliberately broken code scores **67 (2/3)** with the right
test failing, and challenge 6 (security/JWT, a different dependency set) scores
**100 (5/5)** — all executed for real inside the Judge0 sandbox.

---

## 🗂️ Project Layout

```
.
├── docker-compose.yml          # all five services on assessment-network
├── judge0.conf                 # Judge0 CE configuration
├── db/init-judge0-db.sql       # creates the separate judge0 database
├── challenge-tests/            # 6 Maven projects (templates + JUnit suites)
│   └── challenge-1 … challenge-6
├── backend/                    # Spring Boot API
│   ├── Dockerfile
│   └── src/main/java/com/assessment/{config,controller,service,model,repository,security,dto,websocket,converter,exception}
└── frontend/                   # React + Vite SPA
    ├── Dockerfile, nginx.conf
    └── src/{api,hooks,components,pages,store,types}
```

---

## 🧪 Verifying the build locally

```bash
# Challenge templates compile (run inside any challenge folder):
mvn -q -f challenge-tests/challenge-1/pom.xml test-compile

# Backend compiles:
mvn -q -f backend/pom.xml -DskipTests package

# Frontend type-checks + builds:
cd frontend && npm install && npm run build
```
