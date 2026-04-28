-- ---------------------------------------------------------------
-- V22: Add total to orders
-- US-037: De-normalize total from reservation for historical record
--         and efficient listing without JOINs.
-- ---------------------------------------------------------------
ALTER TABLE orders ADD COLUMN IF NOT EXISTS total NUMERIC(10,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS discount NUMERIC(10,2);
