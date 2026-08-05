-- V37: Add check constraint to reservations status column to enforce database-level validation
-- Allowed statuses: pending, uploaded, validated, rejected, expired, cancelled

ALTER TABLE reservations
  ADD CONSTRAINT chk_reservations_status
  CHECK (status IN ('pending', 'uploaded', 'validated', 'rejected', 'expired', 'cancelled'));
