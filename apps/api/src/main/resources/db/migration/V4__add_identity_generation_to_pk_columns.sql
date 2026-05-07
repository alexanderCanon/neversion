-- ---------------------------------------------------------------
-- Add IDENTITY generation to BIGINT PK columns.
-- JPA entities use GenerationType.IDENTITY which requires the DB
-- to auto-generate the id value. The original V1 schema declared
-- the columns as plain BIGINT PRIMARY KEY without auto-increment.
-- ---------------------------------------------------------------

-- services.id
CREATE SEQUENCE IF NOT EXISTS services_id_seq OWNED BY services.id;
SELECT setval('services_id_seq', COALESCE((SELECT MAX(id) FROM services), 0) + 1, false);
ALTER TABLE services ALTER COLUMN id SET DEFAULT nextval('services_id_seq');

-- accounts.id
CREATE SEQUENCE IF NOT EXISTS accounts_id_seq OWNED BY accounts.id;
SELECT setval('accounts_id_seq', COALESCE((SELECT MAX(id) FROM accounts), 0) + 1, false);
ALTER TABLE accounts ALTER COLUMN id SET DEFAULT nextval('accounts_id_seq');

-- profiles.id
CREATE SEQUENCE IF NOT EXISTS profiles_id_seq OWNED BY profiles.id;
SELECT setval('profiles_id_seq', COALESCE((SELECT MAX(id) FROM profiles), 0) + 1, false);
ALTER TABLE profiles ALTER COLUMN id SET DEFAULT nextval('profiles_id_seq');

-- clients.id
CREATE SEQUENCE IF NOT EXISTS clients_id_seq OWNED BY clients.id;
SELECT setval('clients_id_seq', COALESCE((SELECT MAX(id) FROM clients), 0) + 1, false);
ALTER TABLE clients ALTER COLUMN id SET DEFAULT nextval('clients_id_seq');

-- subscriptions.id
CREATE SEQUENCE IF NOT EXISTS subscriptions_id_seq OWNED BY subscriptions.id;
SELECT setval('subscriptions_id_seq', COALESCE((SELECT MAX(id) FROM subscriptions), 0) + 1, false);
ALTER TABLE subscriptions ALTER COLUMN id SET DEFAULT nextval('subscriptions_id_seq');
