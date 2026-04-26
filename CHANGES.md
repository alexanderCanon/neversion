# Summary of Changes

## Frontend (`@apps/panel` & `@apps/store`)
- **Module Resolution:** Fixed TypeScript module resolution errors by adding path mapping aliases in `apps/store/tsconfig.json`. Also installed necessary missing dependencies like `@angular/core` and `@supabase/supabase-js`.
- **Typing & Linting:**
  - Corrected `isLoading` usage in the `LoginComponent` template by leveraging the async pipe (`isLoading$ | async`).
  - Added the `isLoggedIn` method to `AuthService`.
  - Addressed strict TypeScript linting errors in `@apps/panel` by replacing blanket `any` type casts with proper or explicit typings, and resolving unused catch variables.
- **Routing:** Created missing `customer-panel` module/component to fix an `app-routing.module.ts` loading error.

## Backend (`@apps/api`)
- **Testcontainers Rate Limits:** Configured `BaseIntegrationTest.java` to use the `mirror.gcr.io` Docker registry mirror for the PostgreSQL image to avoid Docker Hub rate limiting issues during `mvn test`.
- **Database Migrations:** Fixed `V17__create_notification_log_table.sql` by explicitly dropping the `notification_log` table (created as part of `V2`) if it already exists, preventing constraint/schema conflicts when standing up the database context.

With these changes, `pnpm build` and `pnpm lint` succeed on the frontends, and all 121 backend integration tests pass reliably.
