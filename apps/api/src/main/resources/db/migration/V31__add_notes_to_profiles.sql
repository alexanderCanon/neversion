-- V31: Add operational notes column to profiles table.
-- Used to store per-slot annotations (e.g. invitation links, personal emails)
-- for Spotify Family BY_PROFILE slots without exposing master credentials.
ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS notes TEXT;
