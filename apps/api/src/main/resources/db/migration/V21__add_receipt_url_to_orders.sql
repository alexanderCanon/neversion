-- ---------------------------------------------------------------
-- V21: Add receipt_url to orders
-- US-040: Store proof of payment URL in order for historical record.
-- ---------------------------------------------------------------
ALTER TABLE orders ADD COLUMN IF NOT EXISTS receipt_url TEXT;
