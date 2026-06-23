-- Add profile delivery type to accounts
ALTER TABLE accounts
    ADD COLUMN profile_delivery_type VARCHAR(30) NULL;

-- Add account preference to reservations (selected by client at checkout)
ALTER TABLE reservations
    ADD COLUMN account_preference VARCHAR(20) NULL;

-- Add account preference to orders (de-normalized from reservation at order creation)
ALTER TABLE orders
    ADD COLUMN account_preference VARCHAR(20) NULL;

-- Add account preference to subscriptions (de-normalized from order)
ALTER TABLE subscriptions
    ADD COLUMN account_preference VARCHAR(20) NULL;
