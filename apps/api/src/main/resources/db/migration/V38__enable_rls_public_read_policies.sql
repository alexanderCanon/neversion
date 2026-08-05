-- ---------------------------------------------------------------
-- V37: Enable Row Level Security (RLS) and Public Read Policies
-- Allows anonymous (anon) and authenticated users to fetch catalog
-- data directly from Supabase via PostgREST.
-- ---------------------------------------------------------------

-- 1. Vendors
ALTER TABLE vendors ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read access to vendors"
ON vendors FOR SELECT
TO anon, authenticated
USING (true);

-- 2. Services
ALTER TABLE services ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read access to services"
ON services FOR SELECT
TO anon, authenticated
USING (true);

-- 3. Games
ALTER TABLE games ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read access to active games"
ON games FOR SELECT
TO anon, authenticated
USING (is_active = true);

-- 4. Game SKUs
ALTER TABLE game_skus ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read access to active game skus"
ON game_skus FOR SELECT
TO anon, authenticated
USING (is_active = true);
