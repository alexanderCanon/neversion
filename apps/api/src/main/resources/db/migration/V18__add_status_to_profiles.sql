-- V18: Add operational status column to profiles table (EPIC-03 / US-022).
-- ProfileStatus values: available | active | reserved | occupied | blocked | expired
-- Default 'available' — all existing profiles are treated as unassigned.
ALTER TABLE profiles
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'available';
