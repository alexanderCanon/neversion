-- ---------------------------------------------------------------
-- V23: Order status history (audit trail)
-- US-038 CA3: Historial cronológico de cambios de estado de la orden.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_status_history (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders(id),
    old_status  VARCHAR(30),
    new_status  VARCHAR(30) NOT NULL,
    changed_by  VARCHAR(255),
    notes       TEXT,
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_osh_order_id ON order_status_history(order_id);
