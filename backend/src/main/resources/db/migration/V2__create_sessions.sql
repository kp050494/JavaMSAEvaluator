-- Candidate assessment sessions. The id is a client-visible UUID string.
CREATE TABLE candidate_sessions (
    id             VARCHAR(36) PRIMARY KEY,
    candidate_name VARCHAR(160) NOT NULL,
    email          VARCHAR(200) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_score    INT NOT NULL DEFAULT 0,
    started_at     TIMESTAMP NOT NULL,
    completed_at   TIMESTAMP
);

CREATE INDEX idx_sessions_email ON candidate_sessions (email);
CREATE INDEX idx_sessions_started_at ON candidate_sessions (started_at);
