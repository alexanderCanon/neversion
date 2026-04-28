-- ---------------------------------------------------------------
-- V20: Add notes to reservations
-- US-036: Store rejection reason or vendor comments.
-- ---------------------------------------------------------------
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS notes TEXT;
