-- ---------------------------------------------------------------
-- V12: Complete accounts table
-- US-006: Add acquisition cost, source, purchase date, status,
--         and vendor_id for full operational traceability.
-- No live data to migrate.
-- ---------------------------------------------------------------
ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS cost         NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS source       VARCHAR(255),
    ADD COLUMN IF NOT EXISTS purchased_at DATE,
    ADD COLUMN IF NOT EXISTS status       VARCHAR(20) NOT NULL DEFAULT 'available',
    ADD COLUMN IF NOT EXISTS vendor_id    BIGINT REFERENCES vendors (id);

CREATE INDEX IF NOT EXISTS idx_accounts_vendor_id ON accounts (vendor_id);
CREATE INDEX IF NOT EXISTS idx_accounts_status    ON accounts (status);
