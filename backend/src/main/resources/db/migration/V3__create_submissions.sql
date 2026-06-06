-- One row per "Run Tests" attempt, plus the per-test outcomes.
CREATE TABLE submissions (
    id              BIGSERIAL PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL REFERENCES candidate_sessions (id) ON DELETE CASCADE,
    challenge_id    BIGINT NOT NULL REFERENCES challenges (id),
    code            TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    score           INT NOT NULL DEFAULT 0,
    passed_count    INT NOT NULL DEFAULT 0,
    total_count     INT NOT NULL DEFAULT 0,
    elapsed_seconds INT NOT NULL DEFAULT 0,
    judge0_token    VARCHAR(64),
    logs            TEXT,
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_submissions_session ON submissions (session_id);
CREATE INDEX idx_submissions_challenge ON submissions (challenge_id);

CREATE TABLE submission_results (
    id            BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES submissions (id) ON DELETE CASCADE,
    test_name     VARCHAR(255) NOT NULL,
    passed        BOOLEAN NOT NULL,
    message       TEXT
);

CREATE INDEX idx_results_submission ON submission_results (submission_id);
