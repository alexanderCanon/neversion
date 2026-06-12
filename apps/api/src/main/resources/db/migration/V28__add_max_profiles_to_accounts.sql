-- V28: Decouple maxProfiles from Service catalog to Account level.
-- Each account now carries its own profile limit, initialized from the service template.

-- 1. Add nullable column first
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS max_profiles INT;

-- 2. Migrate existing data: copy the current limit from the linked service
UPDATE accounts a
SET max_profiles = s.max_profiles
FROM services s
WHERE a.service_id = s.id;

-- 3. Fallback for accounts whose service has NULL max_profiles
UPDATE accounts SET max_profiles = 1 WHERE max_profiles IS NULL;

-- 4. Set NOT NULL constraint and default for new rows
ALTER TABLE accounts ALTER COLUMN max_profiles SET NOT NULL;
ALTER TABLE accounts ALTER COLUMN max_profiles SET DEFAULT 1;
