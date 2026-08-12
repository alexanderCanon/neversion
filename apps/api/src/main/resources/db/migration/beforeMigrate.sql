-- ---------------------------------------------------------------
-- Flyway Callback: beforeMigrate.sql
-- Ensures required Supabase environment objects (roles, 'auth' schema,
-- and 'auth.uid()' function stub) exist idempotently in vanilla
-- PostgreSQL environments (local dev / Docker / CI / Testcontainers)
-- before running any schema migrations.
-- Safe and no-op on production Supabase where objects already exist.
-- ---------------------------------------------------------------

DO $$
BEGIN
  -- 1. Ensure Roles
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'anon') THEN
    CREATE ROLE anon NOLOGIN;
  END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'authenticated') THEN
    CREATE ROLE authenticated NOLOGIN;
  END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'service_role') THEN
    CREATE ROLE service_role NOLOGIN;
  END IF;

  -- 2. Ensure Schema 'auth'
  CREATE SCHEMA IF NOT EXISTS auth;

  -- 3. Ensure Function 'auth.uid()' stub for local Postgres
  IF NOT EXISTS (
    SELECT 1 FROM pg_proc p
    JOIN pg_namespace n ON n.oid = p.pronamespace
    WHERE n.nspname = 'auth' AND p.proname = 'uid'
  ) THEN
    CREATE FUNCTION auth.uid() RETURNS uuid AS 'SELECT NULL::uuid;' LANGUAGE sql STABLE;
  END IF;
END $$;
