-- ---------------------------------------------------------------
-- V10: Link services to vendors
-- US-004: Each service must belong to a specific vendor
--         for independent catalog management (ADR-02).
-- Column is NULLABLE — no live data to migrate.
-- ---------------------------------------------------------------
ALTER TABLE services
    ADD COLUMN IF NOT EXISTS vendor_id BIGINT REFERENCES vendors (id);

CREATE INDEX IF NOT EXISTS idx_services_vendor_id ON services (vendor_id);
