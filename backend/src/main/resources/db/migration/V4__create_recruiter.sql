-- Recruiter accounts. The default admin user is seeded (and its password hash
-- reconciled to "admin123") at application startup by DataInitializer, so the
-- bcrypt hash never has to be hard-coded here.
CREATE TABLE recruiter_users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(32) NOT NULL DEFAULT 'RECRUITER'
);
