-- ---------------------------------------------------------------
-- V7: Create users table
-- US-001: Internal user registry linking external auth identity
--         to a platform role (super_admin | vendor | client).
-- role values stored in lowercase per ADR-08 / NFR-06.
-- external_id corresponds to the subject (sub) claim from the
-- Supabase Auth JWT (ADR-06 / ADR-09).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID   NOT NULL DEFAULT gen_random_uuid(),
    external_id VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_uuid        ON users (uuid);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_external_id ON users (external_id);
