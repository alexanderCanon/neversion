-- ---------------------------------------------------------------
-- V40: Create Dashboard Analytics Views and RPC Functions
-- Replaces Java Spring Boot backend analytics with high-performance
-- PostgreSQL Views and RPC functions exposed directly via PostgREST.
-- Enforces 100% multi-tenant isolation via auth.uid().
-- ---------------------------------------------------------------

-- 1. VIEW: v_dashboard_expiring_subscriptions
CREATE OR REPLACE VIEW v_dashboard_expiring_subscriptions AS
SELECT s.uuid               AS subscription_id,
       c.name               AS client_name,
       c.phone              AS client_phone,
       COALESCE(snapshot_svc.name, account_svc.name) AS service_name,
       p.name               AS profile_name,
       s.payment_due_date   AS payment_due_date,
       UPPER(s.status)      AS status,
       u.external_id        AS vendor_external_id
FROM subscriptions s
JOIN clients c ON c.id = s.client_id
JOIN profiles p ON p.id = s.profile_id
LEFT JOIN accounts a ON a.id = p.account_id
LEFT JOIN services account_svc ON account_svc.id = a.service_id
LEFT JOIN services snapshot_svc ON snapshot_svc.id = s.service_id
JOIN vendors v ON v.id = s.vendor_id
JOIN users u ON u.id = v.user_id
WHERE UPPER(s.status) IN ('ACTIVE', 'SUSPENDED')
  AND u.external_id = auth.uid()::text;

GRANT SELECT ON v_dashboard_expiring_subscriptions TO authenticated;

-- 2. VIEW: v_dashboard_expiring_accounts
CREATE OR REPLACE VIEW v_dashboard_expiring_accounts AS
SELECT a.uuid               AS account_id,
       svc.name             AS service_name,
       a.email              AS account_email,
       a.renewal_date       AS renewal_date,
       UPPER(a.status)      AS status,
       u.external_id        AS vendor_external_id
FROM accounts a
JOIN services svc ON svc.id = a.service_id
JOIN vendors v ON v.id = a.vendor_id
JOIN users u ON u.id = v.user_id
WHERE u.external_id = auth.uid()::text;

GRANT SELECT ON v_dashboard_expiring_accounts TO authenticated;

-- 3. VIEW: v_dashboard_inventory_availability
CREATE OR REPLACE VIEW v_dashboard_inventory_availability AS
SELECT s.uuid               AS service_id,
       s.name               AS service_name,
       COUNT(DISTINCT CASE
           WHEN UPPER(a.sale_mode) = 'BY_PROFILE'
            AND UPPER(p.status) = 'AVAILABLE'
           THEN p.id END)   AS available_profiles,
       COUNT(DISTINCT CASE
           WHEN UPPER(a.sale_mode) = 'BY_PROFILE'
            AND UPPER(p.status) IN ('ACTIVE', 'RESERVED', 'OCCUPIED')
           THEN p.id END)   AS occupied_profiles,
       COUNT(DISTINCT CASE
           WHEN UPPER(a.sale_mode) = 'FULL_ACCOUNT'
            AND UPPER(a.status) = 'AVAILABLE'
           THEN a.id END)   AS available_full_accounts,
       COUNT(DISTINCT CASE
           WHEN UPPER(a.sale_mode) = 'FULL_ACCOUNT'
            AND UPPER(a.status) = 'FULL'
           THEN a.id END)   AS occupied_full_accounts,
       u.external_id        AS vendor_external_id
FROM services s
LEFT JOIN accounts a ON a.service_id = s.id AND a.vendor_id = s.vendor_id
LEFT JOIN profiles p ON p.account_id = a.id AND p.vendor_id = s.vendor_id
JOIN vendors v ON v.id = s.vendor_id
JOIN users u ON u.id = v.user_id
WHERE u.external_id = auth.uid()::text
GROUP BY s.uuid, s.name, u.external_id;

GRANT SELECT ON v_dashboard_inventory_availability TO authenticated;

-- 4. VIEW: v_dashboard_active_clients_kpi
CREATE OR REPLACE VIEW v_dashboard_active_clients_kpi AS
SELECT COUNT(DISTINCT s.client_id) AS active_clients_count,
       u.external_id               AS vendor_external_id
FROM subscriptions s
JOIN vendors v ON v.id = s.vendor_id
JOIN users u ON u.id = v.user_id
WHERE UPPER(s.status) = 'ACTIVE'
  AND u.external_id = auth.uid()::text
GROUP BY u.external_id;

GRANT SELECT ON v_dashboard_active_clients_kpi TO authenticated;

-- 5. VIEW: v_dashboard_successful_renewals_kpi
CREATE OR REPLACE VIEW v_dashboard_successful_renewals_kpi AS
SELECT COUNT(*)      AS successful_renewals_count,
       u.external_id AS vendor_external_id
FROM orders o
JOIN reservations r ON r.id = o.reservation_id
JOIN vendors v ON v.id = o.vendor_id
JOIN users u ON u.id = v.user_id
WHERE UPPER(o.status) = 'COMPLETED'
  AND r.renewal_subscription_id IS NOT NULL
  AND o.approved_at >= date_trunc('month', CURRENT_TIMESTAMP)
  AND o.approved_at <  date_trunc('month', CURRENT_TIMESTAMP) + INTERVAL '1 month'
  AND u.external_id = auth.uid()::text
GROUP BY u.external_id;

GRANT SELECT ON v_dashboard_successful_renewals_kpi TO authenticated;

-- 6. VIEW: v_dashboard_gross_profit_kpi
CREATE OR REPLACE VIEW v_dashboard_gross_profit_kpi AS
SELECT COALESCE(SUM(profit_amount), 0) AS gross_profit,
       'GTQ'                           AS currency,
       u.external_id                   AS vendor_external_id
FROM (
    SELECT s.vendor_id,
           (COALESCE(s.price_sold, 0) - COALESCE(s.discount_applied, 0)) - 
           (CASE 
             WHEN UPPER(a.sale_mode) = 'FULL_ACCOUNT' THEN COALESCE(a.cost, 0)
             ELSE COALESCE(a.cost, 0) / COALESCE(NULLIF(a.max_profiles, 0), 1)
           END) AS profit_amount
    FROM subscriptions s
    JOIN profiles p ON p.id = s.profile_id
    JOIN accounts a ON a.id = p.account_id
    WHERE s.created_at >= date_trunc('month', CURRENT_TIMESTAMP)
      AND s.created_at <  date_trunc('month', CURRENT_TIMESTAMP) + INTERVAL '1 month'

    UNION ALL

    SELECT o.vendor_id,
           (COALESCE(s.price_sold, 0) - COALESCE(s.discount_applied, 0)) - 
           (CASE 
             WHEN UPPER(a.sale_mode) = 'FULL_ACCOUNT' THEN COALESCE(a.cost, 0)
             ELSE COALESCE(a.cost, 0) / COALESCE(NULLIF(a.max_profiles, 0), 1)
           END) AS profit_amount
    FROM orders o
    JOIN reservations r ON r.id = o.reservation_id
    JOIN subscriptions s ON s.id = r.renewal_subscription_id
    JOIN profiles p ON p.id = s.profile_id
    JOIN accounts a ON a.id = p.account_id
    WHERE UPPER(o.status) = 'COMPLETED'
      AND o.approved_at >= date_trunc('month', CURRENT_TIMESTAMP)
      AND o.approved_at <  date_trunc('month', CURRENT_TIMESTAMP) + INTERVAL '1 month'
) profit_events
JOIN vendors v ON v.id = profit_events.vendor_id
JOIN users u ON u.id = v.user_id
WHERE u.external_id = auth.uid()::text
GROUP BY u.external_id;

GRANT SELECT ON v_dashboard_gross_profit_kpi TO authenticated;

-- 7. RPC FUNCTION: rpc_get_account_profit_margins
CREATE OR REPLACE FUNCTION rpc_get_account_profit_margins(p_year INT DEFAULT NULL, p_month INT DEFAULT NULL)
RETURNS TABLE (
    account_id UUID,
    email VARCHAR,
    service_name VARCHAR,
    sale_mode VARCHAR,
    account_cost NUMERIC,
    max_profiles INT,
    profiles_sold BIGINT,
    new_revenue NUMERIC,
    renewal_revenue NUMERIC,
    total_revenue NUMERIC,
    total_discount NUMERIC,
    allocated_cost NUMERIC,
    profit NUMERIC,
    profit_margin_pct NUMERIC
) AS $$
DECLARE
    v_target_year INT := COALESCE(p_year, EXTRACT(YEAR FROM CURRENT_DATE)::INT);
    v_target_month INT := COALESCE(p_month, EXTRACT(MONTH FROM CURRENT_DATE)::INT);
    v_period_start TIMESTAMPTZ := make_timestamptz(v_target_year, v_target_month, 1, 0, 0, 0);
    v_next_period_start TIMESTAMPTZ := v_period_start + INTERVAL '1 month';
    v_vendor_id BIGINT;
BEGIN
    SELECT v.id INTO v_vendor_id
    FROM vendors v
    JOIN users u ON u.id = v.user_id
    WHERE u.external_id = auth.uid()::text;

    IF v_vendor_id IS NULL THEN
        RETURN;
    END IF;

    RETURN QUERY
    WITH new_subs AS (
        SELECT p.account_id                            AS account_id,
               COUNT(*)                                AS profiles_sold,
               SUM(COALESCE(s.price_sold, 0)
                   - COALESCE(s.discount_applied, 0)) AS revenue,
               SUM(COALESCE(s.discount_applied, 0))   AS total_discount
        FROM subscriptions s
        JOIN profiles p ON p.id = s.profile_id
        WHERE s.vendor_id = v_vendor_id
          AND s.created_at >= v_period_start
          AND s.created_at <  v_next_period_start
        GROUP BY p.account_id
    ),
    renewals AS (
        SELECT p.account_id                            AS account_id,
               SUM(COALESCE(s.price_sold, 0)
                   - COALESCE(s.discount_applied, 0)) AS revenue,
               SUM(COALESCE(s.discount_applied, 0))   AS total_discount
        FROM orders o
        JOIN reservations r ON r.id = o.reservation_id
        JOIN subscriptions s ON s.id = r.renewal_subscription_id
        JOIN profiles p ON p.id = s.profile_id
        WHERE o.vendor_id = v_vendor_id
          AND UPPER(o.status) = 'COMPLETED'
          AND o.approved_at >= v_period_start
          AND o.approved_at <  v_next_period_start
        GROUP BY p.account_id
    )
    SELECT a.uuid                                      AS account_id,
           a.email,
           svc.name                                    AS service_name,
           a.sale_mode,
           COALESCE(a.cost, 0)                         AS account_cost,
           COALESCE(a.max_profiles, 1)                 AS max_profiles,
           COALESCE(ns.profiles_sold, 0)              AS profiles_sold,
           COALESCE(ns.revenue, 0)                     AS new_revenue,
           COALESCE(rn.revenue, 0)                     AS renewal_revenue,
           (COALESCE(ns.revenue, 0) + COALESCE(rn.revenue, 0)) AS total_revenue,
           (COALESCE(ns.total_discount, 0) + COALESCE(rn.total_discount, 0)) AS total_discount,
           CASE
               WHEN UPPER(a.sale_mode) = 'FULL_ACCOUNT' THEN
                   CASE WHEN COALESCE(ns.profiles_sold, 0) > 0 THEN COALESCE(a.cost, 0) ELSE 0 END
               ELSE
                   ROUND((COALESCE(a.cost, 0) / GREATEST(COALESCE(a.max_profiles, 1), 1)) * COALESCE(ns.profiles_sold, 0), 2)
           END AS allocated_cost,
           ((COALESCE(ns.revenue, 0) + COALESCE(rn.revenue, 0)) - 
            CASE
               WHEN UPPER(a.sale_mode) = 'FULL_ACCOUNT' THEN
                   CASE WHEN COALESCE(ns.profiles_sold, 0) > 0 THEN COALESCE(a.cost, 0) ELSE 0 END
               ELSE
                   ROUND((COALESCE(a.cost, 0) / GREATEST(COALESCE(a.max_profiles, 1), 1)) * COALESCE(ns.profiles_sold, 0), 2)
            END) AS profit,
           CASE
               WHEN (COALESCE(ns.revenue, 0) + COALESCE(rn.revenue, 0)) > 0 THEN
                   ROUND((((COALESCE(ns.revenue, 0) + COALESCE(rn.revenue, 0)) - 
                    CASE
                       WHEN UPPER(a.sale_mode) = 'FULL_ACCOUNT' THEN
                           CASE WHEN COALESCE(ns.profiles_sold, 0) > 0 THEN COALESCE(a.cost, 0) ELSE 0 END
                       ELSE
                           ROUND((COALESCE(a.cost, 0) / GREATEST(COALESCE(a.max_profiles, 1), 1)) * COALESCE(ns.profiles_sold, 0), 2)
                    END) * 100) / (COALESCE(ns.revenue, 0) + COALESCE(rn.revenue, 0)), 2)
               ELSE 0
           END AS profit_margin_pct
    FROM accounts a
    JOIN services svc ON svc.id = a.service_id
    LEFT JOIN new_subs ns ON ns.account_id = a.id
    LEFT JOIN renewals rn ON rn.account_id = a.id
    WHERE a.vendor_id = v_vendor_id
      AND UPPER(a.status) != 'EXPIRED'
    ORDER BY svc.name ASC, a.email ASC;
END;
$$ LANGUAGE plpgsql SECURITY INVOKER;

GRANT EXECUTE ON FUNCTION rpc_get_account_profit_margins TO authenticated;

-- 8. VIEW: v_dashboard_products_summary
CREATE OR REPLACE VIEW v_dashboard_products_summary AS
SELECT s.uuid               AS product_id,
       s.name               AS product_name,
       UPPER(s.category)    AS category,
       COUNT(DISTINCT a.id) AS total_accounts,
       u.external_id        AS vendor_external_id
FROM services s
LEFT JOIN accounts a ON a.service_id = s.id
JOIN vendors v ON v.id = s.vendor_id
JOIN users u ON u.id = v.user_id
WHERE u.external_id = auth.uid()::text
GROUP BY s.uuid, s.name, s.category, u.external_id;

GRANT SELECT ON v_dashboard_products_summary TO authenticated;

-- 9. VIEW: v_dashboard_accounts_by_product
CREATE OR REPLACE VIEW v_dashboard_accounts_by_product AS
SELECT a.uuid               AS account_id,
       a.email              AS email,
       a.password           AS password,
       a.renewal_date       AS cut_off_date,
       CASE WHEN UPPER(a.sale_mode) = 'BY_PROFILE' THEN 'FAMILY' ELSE 'INDIVIDUAL' END
                            AS account_type,
       CASE WHEN a.renewal_date >= CURRENT_DATE THEN 'ACTIVE' ELSE 'EXPIRED' END
                            AS account_status,
       svc.max_profiles     AS max_profiles,
       COUNT(DISTINCT CASE WHEN UPPER(s.status) IN ('ACTIVE', 'SUSPENDED') THEN p.id END)
                            AS occupied_profiles,
       svc.uuid             AS product_id,
       u.external_id        AS vendor_external_id
FROM accounts a
JOIN services svc ON a.service_id = svc.id
LEFT JOIN profiles p ON p.account_id = a.id
LEFT JOIN subscriptions s ON s.profile_id = p.id AND UPPER(s.status) IN ('ACTIVE', 'SUSPENDED')
JOIN vendors v ON v.id = a.vendor_id
JOIN users u ON u.id = v.user_id
WHERE u.external_id = auth.uid()::text
GROUP BY a.uuid, a.email, a.password, a.renewal_date, a.sale_mode, svc.max_profiles, svc.uuid, u.external_id;

GRANT SELECT ON v_dashboard_accounts_by_product TO authenticated;

-- 10. VIEW: v_dashboard_profiles_by_account
CREATE OR REPLACE VIEW v_dashboard_profiles_by_account AS
SELECT sl.uuid              AS profile_id,
       sl.name              AS profile_name,
       sl.pin               AS pin,
       CASE
           WHEN UPPER(s.status) = 'ACTIVE'    THEN 'OCCUPIED'
           WHEN UPPER(s.status) = 'SUSPENDED' THEN 'BLOCKED'
           ELSE 'AVAILABLE'
       END                  AS profile_status,
       s.uuid               AS sub_id,
       s.start_date         AS start_date,
       s.payment_due_date   AS end_date,
       CASE
           WHEN UPPER(s.status) = 'ACTIVE' AND s.payment_due_date <= (CURRENT_DATE + INTERVAL '7 days') THEN 'EXPIRING_SOON'
           ELSE UPPER(s.status)
       END                  AS sub_status,
       c.uuid               AS customer_id,
       c.name               AS customer_name,
       c.phone              AS customer_phone,
       a.uuid               AS account_id,
       u.external_id        AS vendor_external_id
FROM profiles sl
JOIN accounts a ON a.id = sl.account_id
LEFT JOIN subscriptions s ON s.profile_id = sl.id AND UPPER(s.status) IN ('ACTIVE', 'SUSPENDED')
LEFT JOIN clients c ON c.id = s.client_id
JOIN vendors v ON v.id = sl.vendor_id
JOIN users u ON u.id = v.user_id
WHERE u.external_id = auth.uid()::text;

GRANT SELECT ON v_dashboard_profiles_by_account TO authenticated;
