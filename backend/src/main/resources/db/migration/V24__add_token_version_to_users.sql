-- Token versioning enables immediate JWT revocation on deactivation,
-- role change or logout: tokens carry the version they were issued with
-- and are rejected once the stored version moves past it.
ALTER TABLE users
    ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
