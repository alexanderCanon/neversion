# 1. Backend
cd apps/api && ./mvnw spring-boot:run

# 2. Generar tipos
cd ../.. && pnpm run api:sync

# 3. Migrar imports (script listo)
bash scripts/migrate-imports.sh

# 4. Levantar el panel
cd apps/panel && pnpm start
