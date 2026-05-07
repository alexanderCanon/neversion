# OpenAPI JSON Contract Verification Prompt

Use this prompt in a fresh agent session to verify the OpenAPI JSON contract cleanup.

```text
The goal of this session is to verify that the OpenAPI JSON contract cleanup is complete and that no defensive `Accept: application/json` overrides remain in the Angular apps.

Context:
- The backend previously generated Angular API-client methods with `httpHeaderAccept?: '*/*'`.
- That caused Angular to sometimes treat valid JSON responses as `Blob`, producing false empty states, non-iterable responses, or runtime errors.
- The intended root fix was to declare `produces = application/json` in backend controllers, rebuild the backend, run `pnpm api:sync`, and then remove temporary frontend overrides.

Scope:
- Do not write or modify application code unless a real regression is found and explicitly approved.
- Do not start backend, frontend dev servers, or Docker.
- Do not run full test suites.
- Prefer read-only inspection first.

Verification checklist:
1. Inspect `packages/api-client/src/api`.
   - Confirm there are no generated signatures with `httpHeaderAccept?: '*/*'`.
   - Confirm JSON endpoints now use `httpHeaderAccept?: 'application/json'`.

2. Inspect Angular apps.
   - Confirm `apps/panel/src` and `apps/store/src` do not contain `jsonResponseOptions`.
   - Confirm no wrapper manually passes `{ httpHeaderAccept: 'application/json' as '*/*' }`.
   - Confirm wrappers still normalize arrays where the backend contract may return either direct arrays or paged/content responses.

3. Inspect subscription access contract.
   - Confirm `packages/api-client/src/model/accessSummary.ts` exists.
   - Confirm `SubscriptionDetailResponse` imports and exposes `access?: AccessSummary`.
   - Confirm `SubscriptionDetailComponent` uses the generated `SubscriptionDetailResponse` type directly, not a local temporary access type.

4. Inspect documentation.
   - Confirm `docs/implementation/backend-construction.md`, `panel-construction.md`, `store-construction.md`, and `qa-followup-2026-05-05.md` describe the final state accurately.
   - The docs should not imply that frontend `Accept` overrides are the desired final solution.

5. Run only lightweight validation:
   - `rg "httpHeaderAccept\\?: '\\*/\\*'" packages/api-client/src/api`
   - `rg "jsonResponseOptions|httpHeaderAccept: 'application/json' as '\\*/\\*'" apps/panel/src apps/store/src`
   - `pnpm build` in `apps/panel`
   - `pnpm build` in `apps/store`

Expected outcome:
- A concise markdown report stating PASS/FAIL for each checklist item.
- If failures are found, list exact files and lines, explain why each is a real issue, and propose the smallest correction.
- Do not update the bitácoras unless a code/doc change is actually made and verified.
```
