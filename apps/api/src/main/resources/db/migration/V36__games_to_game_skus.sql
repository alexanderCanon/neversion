-- ---------------------------------------------------------------
-- V36: Restructure games into parent (Game) -> child (GameSku)
--
-- The existing 'games' table (V35) holds recharge SKUs such as
-- "Free Fire 110 Diamonds". To support categorization (parent game
-- -> child SKUs), we:
--   1. Rename the existing 'games' table to 'game_skus'.
--   2. Create a new 'games' parent table (Free Fire, Clash Royale).
--   3. Add a nullable 'game_id' FK on 'game_skus' pointing to 'games'.
--   4. Backfill: auto-create a "Free Fire" parent per vendor and link
--      matching SKUs (name ILIKE 'free fire%' OR code ILIKE 'ff%').
--      Non-matching SKUs remain orphan (game_id NULL) for manual
--      reassignment from the panel.
--
-- See ADR-11: relations between modules use plain FK columns (no
-- @ManyToOne). Game and GameSku are independent bounded contexts.
-- ---------------------------------------------------------------

-- 1. Rename existing games table to game_skus
ALTER TABLE games RENAME TO game_skus;

-- 2. Rename inherited indexes
ALTER INDEX IF EXISTS idx_games_uuid RENAME TO idx_game_skus_uuid;
ALTER INDEX IF EXISTS idx_games_vendor_id RENAME TO idx_game_skus_vendor_id;
ALTER INDEX IF EXISTS idx_games_vendor_code RENAME TO idx_game_skus_vendor_code;

-- 3. Rename inherited check constraint
ALTER TABLE game_skus RENAME CONSTRAINT chk_games_price TO chk_game_skus_price;

-- 4. Create new games parent table
CREATE TABLE IF NOT EXISTS games (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid       UUID NOT NULL DEFAULT gen_random_uuid(),
    vendor_id  BIGINT NOT NULL REFERENCES vendors (id),
    name       VARCHAR(100) NOT NULL,
    slug       VARCHAR(100) NOT NULL,
    image_url  VARCHAR(500) NULL,
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_games_uuid ON games (uuid);
CREATE INDEX IF NOT EXISTS idx_games_vendor_id ON games (vendor_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_games_vendor_slug ON games (vendor_id, slug);

-- 5. Add game_id FK to game_skus (nullable - orphans to be reassigned manually)
ALTER TABLE game_skus ADD COLUMN IF NOT EXISTS game_id BIGINT NULL REFERENCES games (id);
CREATE INDEX IF NOT EXISTS idx_game_skus_game_id ON game_skus (game_id);

-- 6. Backfill: create a "Free Fire" parent per vendor that has matching SKUs
INSERT INTO games (vendor_id, name, slug, image_url, is_active)
SELECT DISTINCT gs.vendor_id, 'Free Fire', 'free-fire', NULL, TRUE
FROM game_skus gs
WHERE gs.name ILIKE 'free fire%'
   OR gs.code ILIKE 'ff%'
ON CONFLICT DO NOTHING;

-- 7. Link matching SKUs to their vendor's "Free Fire" parent
UPDATE game_skus gs
SET game_id = g.id
FROM games g
WHERE g.vendor_id = gs.vendor_id
  AND g.slug = 'free-fire'
  AND (gs.name ILIKE 'free fire%' OR gs.code ILIKE 'ff%')
  AND gs.game_id IS NULL;
