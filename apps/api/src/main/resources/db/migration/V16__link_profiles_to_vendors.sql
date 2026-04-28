-- ---------------------------------------------------------------
-- V16: Link profiles to vendors
-- Each profile must be scoped to a vendor for multi-tenancy
-- isolation (ADR-02). Column is NULLABLE.
-- ---------------------------------------------------------------
ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS vendor_id BIGINT REFERENCES vendors (id);

CREATE INDEX IF NOT EXISTS idx_profiles_vendor_id ON profiles (vendor_id);
