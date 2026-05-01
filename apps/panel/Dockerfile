# ══════════════════════════════════════════════════════════════════
# Stage 1: BUILD — install dependencies and compile the production bundle.
# pnpm keeps installs consistent with the repository lock file.
# ══════════════════════════════════════════════════════════════════
FROM node:20-alpine AS build
WORKDIR /workspace

# Install pnpm globally.
RUN npm install -g pnpm

# Copy manifests first to reuse Docker cache.
# Dependencies are reinstalled only when manifests or the lock file change.
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc ./
COPY apps/panel/package.json apps/panel/package.json
COPY packages/api-client/package.json packages/api-client/package.json
COPY packages/models/package.json packages/models/package.json
COPY packages/utils/package.json packages/utils/package.json

# --frozen-lockfile guarantees exact lock file reproducibility.
RUN pnpm install --frozen-lockfile --filter panel...

# Copy the rest of the source code after dependencies.
COPY apps/panel apps/panel
COPY packages packages
COPY tsconfig.json tsconfig.json

# Production build output goes to dist/panel/.
WORKDIR /workspace/apps/panel
RUN pnpm build

# ══════════════════════════════════════════════════════════════════
# Stage 2: RUN — Nginx serves static files.
# The final image excludes Node.js and source files.
# ══════════════════════════════════════════════════════════════════
FROM nginx:alpine

# Copy compiled static files from the build stage.
# Angular 17 application builder outputs to dist/panel/browser/.
COPY --from=build /workspace/apps/panel/dist/panel/browser /usr/share/nginx/html

# Custom Nginx config: SPA fallback, gzip, cache headers, and API proxy.
COPY apps/panel/nginx.conf /etc/nginx/conf.d/default.conf

RUN mkdir -p /usr/share/nginx/html/assets && \
    cat > /docker-entrypoint.d/10-neversion-runtime-config.sh <<'EOF' && \
    chmod +x /docker-entrypoint.d/10-neversion-runtime-config.sh
#!/bin/sh
set -eu

cat > /usr/share/nginx/html/assets/runtime-config.js <<CONFIG
window.__NEVERSION_CONFIG__ = {
  apiUrl: "${PANEL_API_URL:-}",
  supabaseUrl: "${PANEL_SUPABASE_URL:-https://your-project.supabase.co}",
  supabaseKey: "${PANEL_SUPABASE_KEY:-your-anon-key}"
};
CONFIG
EOF

# Standard HTTP port.
EXPOSE 80

# Nginx runs in the foreground as required by Docker.
CMD ["nginx", "-g", "daemon off;"]
