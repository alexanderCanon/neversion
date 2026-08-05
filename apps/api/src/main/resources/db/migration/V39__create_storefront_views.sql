-- ---------------------------------------------------------------
-- V39: Create Storefront DTO Views and Enforce Security Barrier
-- Restricts direct table access and exposes clean, secure DTO views
-- for the public storefront via PostgREST.
-- ---------------------------------------------------------------

-- 1. Revoke public direct read policies on physical tables (replaces V38 direct policies)
DROP POLICY IF EXISTS "Allow public read access to vendors" ON vendors;
DROP POLICY IF EXISTS "Allow public read access to services" ON services;
DROP POLICY IF EXISTS "Allow public read access to active games" ON games;
DROP POLICY IF EXISTS "Allow public read access to active game skus" ON game_skus;

-- 2. VIEW 1: v_store_vendors (Public vendor store profile)
CREATE OR REPLACE VIEW v_store_vendors AS
SELECT 
    uuid         AS vendor_uuid,
    store_name,
    logo_url,
    bank_details,
    discount_cfg
FROM vendors;

GRANT SELECT ON v_store_vendors TO anon, authenticated;

-- 3. VIEW 2: v_store_services (Public catalog of streaming services)
CREATE OR REPLACE VIEW v_store_services AS
SELECT 
    s.uuid          AS service_uuid,
    s.name          AS service_name,
    s.category,
    s.description,
    s.image_url,
    s.price_profile,
    s.price_full,
    s.duration_days,
    s.max_profiles,
    v.uuid          AS vendor_uuid
FROM services s
JOIN vendors v ON v.id = s.vendor_id
WHERE s.is_active = true;

GRANT SELECT ON v_store_services TO anon, authenticated;

-- 4. VIEW 3: v_store_games (Public catalog of parent games)
CREATE OR REPLACE VIEW v_store_games AS
SELECT 
    g.uuid          AS game_uuid,
    g.name          AS game_name,
    g.slug          AS game_slug,
    g.image_url,
    v.uuid          AS vendor_uuid
FROM games g
JOIN vendors v ON v.id = g.vendor_id
WHERE g.is_active = true;

GRANT SELECT ON v_store_games TO anon, authenticated;

-- 5. VIEW 4: v_store_game_skus (Public catalog of game recharge packages)
CREATE OR REPLACE VIEW v_store_game_skus AS
SELECT 
    gs.uuid         AS sku_uuid,
    gs.code         AS sku_code,
    gs.name         AS sku_name,
    gs.price        AS sku_price,
    gs.image_url    AS sku_image_url,
    g.name          AS game_name,
    g.slug          AS game_slug,
    v.uuid          AS vendor_uuid
FROM game_skus gs
JOIN games g ON g.id = gs.game_id
JOIN vendors v ON v.id = gs.vendor_id
WHERE gs.is_active = true 
  AND g.is_active = true;

GRANT SELECT ON v_store_game_skus TO anon, authenticated;
