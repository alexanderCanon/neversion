-- Vista útil: renovaciones próximas (base para el workflow de n8n)
CREATE VIEW upcoming_renewals AS
SELECT
  s.id              AS subscription_id,
  c.name            AS client_name,
  c.phone           AS client_phone,
  c.email           AS client_email,
  sv.name           AS service_name,
  a.email           AS account_email,
  p.name            AS profile_name,
  s.payment_due_date,
  s.status,
  s.months_paid,
  (s.payment_due_date - CURRENT_DATE) AS days_until_due
FROM subscriptions s
JOIN profiles   p  ON p.id  = s.profile_id
JOIN accounts   a  ON a.id  = p.account_id
JOIN services   sv ON sv.id = a.service_id
JOIN clients    c  ON c.id  = s.client_id
WHERE s.status IN ('active', 'pending');

-- Vista: renovaciones de cuentas (las que tú pagas al servicio)
CREATE VIEW upcoming_account_renewals AS
SELECT
  a.id,
  sv.name        AS service_name,
  a.email,
  a.renewal_date,
  (a.renewal_date - CURRENT_DATE) AS days_until_due
FROM accounts a
JOIN services sv ON sv.id = a.service_id
WHERE a.renewal_date >= CURRENT_DATE - INTERVAL '7 days';

-- Log de notificaciones enviadas (equivalente al notif_log del sheet)
CREATE TABLE notification_log (
  id         SERIAL PRIMARY KEY,
  entity_type VARCHAR(20) NOT NULL, -- 'subscription' | 'account'
  entity_id  INT NOT NULL,
  stage      VARCHAR(20) NOT NULL,  -- '7d', '3d', '1d', 'due', 'overdue'
  sent_at    TIMESTAMPTZ DEFAULT NOW(),

  UNIQUE (entity_type, entity_id, stage)
);
