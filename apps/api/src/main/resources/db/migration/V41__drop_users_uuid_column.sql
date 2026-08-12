-- ---------------------------------------------------------------
-- V41: Drop redundant uuid column from users table
-- users.external_id (Supabase Auth sub) is the official public user ID.
-- ---------------------------------------------------------------
DROP INDEX IF EXISTS idx_users_uuid;
ALTER TABLE users DROP COLUMN IF EXISTS uuid;
