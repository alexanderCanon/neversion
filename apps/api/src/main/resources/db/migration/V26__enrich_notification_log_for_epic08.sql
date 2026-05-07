-- ---------------------------------------------------------------
-- V26 — Enrich notification_log for EPIC-08
-- Adds entity tracking (entity_type/entity_id/stage) for
-- deduplication (US-054) and audit, plus processed_at for
-- worker lifecycle tracking.
-- ---------------------------------------------------------------
ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS entity_type VARCHAR(30);
ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS entity_id   BIGINT;
ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS stage       VARCHAR(30);
ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS processed_at TIMESTAMPTZ;
ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS error_message TEXT;

-- Deduplication index for renewal reminders (US-054):
-- ensures only one notification per entity+stage per cycle.
CREATE UNIQUE INDEX IF NOT EXISTS idx_notif_dedup
    ON notification_log(entity_type, entity_id, stage)
    WHERE entity_type IS NOT NULL AND entity_id IS NOT NULL AND stage IS NOT NULL;
