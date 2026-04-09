-- ---------------------------------------------------------------
-- 1. Add status to reservations
--    Tracks checkout lifecycle: PENDING → UPLOADED → VALIDATED
--    or terminal states EXPIRED / CANCELLED.
-- ---------------------------------------------------------------
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';


-- ---------------------------------------------------------------
-- 2. reservation_details
--    Line items linked to a Reservation header.
--    subtotal is a generated column (qty * unit_price).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservation_details (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id  UUID NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    inventory_id    BIGINT,
    qty             INT NOT NULL,
    unit_price      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    subtotal        NUMERIC(10, 2) GENERATED ALWAYS AS (qty * unit_price) STORED
);

CREATE INDEX IF NOT EXISTS idx_reservation_details_reservation
    ON reservation_details (reservation_id);
