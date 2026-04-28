-- ---------------------------------------------------------------
-- V17 — notification_log
-- Records every transactional notification event.
-- Backend inserts; Agent Notifications consumes and dispatches.
-- NFR-05: All notifications must be recorded here.
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS notification_log;

CREATE TABLE notification_log (
    id             BIGSERIAL PRIMARY KEY,
    uuid           UUID        NOT NULL DEFAULT gen_random_uuid(),
    type           VARCHAR(50) NOT NULL,   -- e.g. VENDOR_WELCOME, RENEWAL_REMINDER
    recipient_email VARCHAR(255) NOT NULL,
    payload        TEXT,                   -- JSON with template vars (credentials, etc.)
    status         VARCHAR(20) NOT NULL DEFAULT 'pending',
                   -- 'pending'   = waiting for Agent Notifications to process
                   -- 'sent'      = successfully dispatched
                   -- 'failed'    = dispatch failed
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_notification_log_uuid ON notification_log (uuid);
CREATE INDEX idx_notification_log_type        ON notification_log (type);
CREATE INDEX idx_notification_log_status      ON notification_log (status);
CREATE INDEX idx_notification_log_created_at  ON notification_log (created_at DESC);
