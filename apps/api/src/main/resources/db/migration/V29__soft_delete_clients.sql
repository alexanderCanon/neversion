-- ---------------------------------------------------------------
-- V29: Soft delete for clients
-- Adds deleted_at column to support logical deletion without
-- breaking referential integrity with subscriptions, reservations
-- and orders. Rows where deleted_at IS NOT NULL are excluded
-- automatically via @SQLRestriction on ClientEntity.
-- ---------------------------------------------------------------
ALTER TABLE clients
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ NULL;

CREATE INDEX IF NOT EXISTS idx_clients_deleted_at ON clients (deleted_at)
    WHERE deleted_at IS NULL;
