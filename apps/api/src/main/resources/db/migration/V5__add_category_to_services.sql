-- ---------------------------------------------------------------
-- Add category to services for dashboard filtering.
-- All current services default to STREAMING.
-- ---------------------------------------------------------------
ALTER TABLE services
    ADD COLUMN IF NOT EXISTS category VARCHAR(50) NOT NULL DEFAULT 'STREAMING';

CREATE INDEX IF NOT EXISTS idx_services_category ON services (category);
