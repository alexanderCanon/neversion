-- ---------------------------------------------------------------
-- V9: Link clients to users and vendors
-- US-003: Each client must be linked to an authenticated user
--         and to a specific vendor for multi-tenancy isolation (ADR-02).
-- Columns are NULLABLE because existing rows have no user/vendor yet
-- (confirmed: no live data to migrate).
-- ---------------------------------------------------------------
ALTER TABLE clients
    ADD COLUMN IF NOT EXISTS user_id   BIGINT REFERENCES users (id),
    ADD COLUMN IF NOT EXISTS vendor_id BIGINT REFERENCES vendors (id);

CREATE INDEX IF NOT EXISTS idx_clients_user_id   ON clients (user_id);
CREATE INDEX IF NOT EXISTS idx_clients_vendor_id ON clients (vendor_id);
