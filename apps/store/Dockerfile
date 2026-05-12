# ══════════════════════════════════════════════════════════════════
# Stage 1: BUILD — install dependencies and compile the SSR bundle.
# ══════════════════════════════════════════════════════════════════
FROM node:20-alpine AS build
WORKDIR /workspace

RUN npm install -g pnpm

# Copy manifests first to reuse Docker cache.
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc ./
COPY apps/store/package.json apps/store/package.json
COPY packages/api-client/package.json packages/api-client/package.json
COPY packages/models/package.json packages/models/package.json
COPY packages/utils/package.json packages/utils/package.json

# Install dependencies.
RUN pnpm install --frozen-lockfile --filter neversion-site...

# Copy source code.
COPY apps/store apps/store
COPY packages packages
COPY tsconfig.json tsconfig.json

# Build the application.
WORKDIR /workspace/apps/store
RUN pnpm run build:ssr

# Use pnpm deploy to isolate the production bundle and its dependencies.
# This eliminates the need to copy the entire monorepo node_modules.
WORKDIR /workspace
RUN pnpm deploy --filter=neversion-site --prod --legacy /prod/store

# ══════════════════════════════════════════════════════════════════
# Stage 2: RUN — Node.js serves the SSR app.
# ══════════════════════════════════════════════════════════════════
FROM node:20-alpine

# ── Security: run as non-root user ──────────────────────────────
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the isolated production directory from the deploy stage.
COPY --from=build /prod/store ./

# Copy the build artifacts (dist) explicitly to ensure they are present.
# pnpm deploy usually doesn't include the 'dist' folder if it's ignored by git/npm.
COPY --from=build /workspace/apps/store/dist ./dist

RUN cat > /usr/local/bin/neversion-store-entrypoint <<'EOF' && \
    chmod +x /usr/local/bin/neversion-store-entrypoint
#!/bin/sh
set -eu

mkdir -p dist/neversion-site/browser/assets
cat > dist/neversion-site/browser/assets/runtime-config.js <<CONFIG
window.__NEVERSION_CONFIG__ = {
  apiUrl: "${STORE_API_URL:-http://localhost:8080}",
  supabaseUrl: "${STORE_SUPABASE_URL:-https://your-project.supabase.co}",
  supabaseKey: "${STORE_SUPABASE_KEY:-your-anon-key}",
  storeVendorUuid: "${STORE_VENDOR_UUID:-00000000-0000-0000-0000-000000000000}"
};
CONFIG

exec node dist/neversion-site/server/main.js
EOF

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 4000

ENTRYPOINT ["neversion-store-entrypoint"]
