# ══════════════════════════════════════════════════════════════════
# Stage 1: BUILD — instala dependencias y compila SSR bundle
# npm ci para install reproducible desde package-lock.json
# ══════════════════════════════════════════════════════════════════
FROM node:18-alpine AS build
WORKDIR /app

# Copia manifiestos primero para aprovechar cache de Docker.
# Las deps solo se reinstalan si cambia package.json o package-lock.json.
COPY package*.json ./

# npm ci instala exactamente lo del lock file (equivalente a --frozen-lockfile)
RUN npm ci

# Copia el resto del codigo fuente
COPY . .

# build:ssr ejecuta: ng build && ng run neversion-site:server
# Output:
#   dist/neversion-site/browser/  — static assets
#   dist/neversion-site/server/   — SSR Express bundle (main.js)
RUN npm run build:ssr

# ══════════════════════════════════════════════════════════════════
# Stage 2: RUN — Node.js sirve la app con SSR
# Solo se copia el dist/, sin codigo fuente ni node_modules de dev
# ══════════════════════════════════════════════════════════════════
FROM node:18-alpine

# ── Security: run as non-root user ──────────────────────────────
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copia el bundle compilado desde el stage de build
COPY --from=build /app/dist/neversion-site ./dist/neversion-site

# El servidor Express de @nguniversal necesita las dependencias de runtime.
# Copiamos package*.json y reinstalamos solo las deps de produccion.
COPY package*.json ./
RUN npm ci --omit=dev

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 4000

# Lanza el servidor Express generado por @nguniversal
CMD ["node", "dist/neversion-site/server/main.js"]
