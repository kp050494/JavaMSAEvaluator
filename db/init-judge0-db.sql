-- Judge0 uses its own database on the same Postgres instance.
-- The main application uses assessment_db (created by POSTGRES_DB env).
-- This script provisions the separate judge0 database.
CREATE DATABASE judge0 OWNER assessment;
