-- Drop the old unique constraint that prevented multiple cancelled subscriptions for the same profile
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS unique_active_profile;

-- Create a partial unique index that only enforces uniqueness for active/non-cancelled subscriptions
CREATE UNIQUE INDEX unique_active_profile ON subscriptions (profile_id) 
WHERE (status NOT IN ('CANCELLED', 'cancelled'));
