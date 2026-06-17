# syntax=docker/dockerfile:1.7

# Stage 1: build the Angular SPA.
FROM node:20-alpine AS build
WORKDIR /workspace

RUN corepack enable && corepack prepare pnpm@10.33.3 --activate

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc ./
COPY apps/store/package.json apps/store/package.json
COPY packages/api-client/package.json packages/api-client/package.json
COPY packages/models/package.json packages/models/package.json

RUN --mount=type=cache,id=pnpm-store,target=/pnpm/store \
    pnpm config set store-dir /pnpm/store && \
    pnpm install --frozen-lockfile --filter neversion-site...

COPY apps/store apps/store
COPY packages packages
COPY tsconfig.json tsconfig.json

WORKDIR /workspace/apps/store
ENV NG_BUILD_MAX_WORKERS=1
RUN --mount=type=cache,id=store-angular-cache,target=/workspace/apps/store/.angular/cache \
    pnpm build

# Stage 2: serve static files with Nginx.
FROM nginx:alpine

COPY --from=build /workspace/apps/store/dist/neversion-site/browser /usr/share/nginx/html
COPY apps/store/nginx.conf /etc/nginx/conf.d/default.conf

RUN mkdir -p /usr/share/nginx/html/assets && \
    cat > /docker-entrypoint.d/10-neversion-runtime-config.sh <<'EOF' && \
    chmod +x /docker-entrypoint.d/10-neversion-runtime-config.sh
#!/bin/sh
set -eu

cat > /usr/share/nginx/html/assets/runtime-config.js <<CONFIG
window.__NEVERSION_CONFIG__ = {
  apiUrl: "${STORE_API_URL:-http://localhost:8080}",
  supabaseUrl: "${STORE_SUPABASE_URL:-https://your-project.supabase.co}",
  supabaseKey: "${STORE_SUPABASE_KEY:-your-anon-key}",
  storeVendorUuid: "${STORE_VENDOR_UUID:-00000000-0000-0000-0000-000000000000}"
};
CONFIG
EOF

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
