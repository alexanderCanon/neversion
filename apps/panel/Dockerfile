# ══════════════════════════════════════════════════════════════════
# Stage 1: BUILD — instala dependencias y compila el bundle de prod
# pnpm para install consistente con el lock file del repo
# ══════════════════════════════════════════════════════════════════
FROM node:20-alpine AS build
WORKDIR /app

# Instala pnpm globalmente
RUN npm install -g pnpm

# Copia manifiestos primero para aprovechar cache de Docker.
# Las deps solo se reinstalan si cambia package.json o pnpm-lock.yaml.
COPY package.json pnpm-lock.yaml ./

# --frozen-lockfile garantiza reproducibilidad exacta del lock file
RUN pnpm install --frozen-lockfile

# Copia el resto del codigo fuente (invalida cache solo si cambia src)
COPY . .

# Build de produccion → output en dist/panel/
RUN pnpm build

# ══════════════════════════════════════════════════════════════════
# Stage 2: RUN — Nginx sirve los static files
# Imagen final sin Node.js ni codigo fuente, ~25MB vs ~400MB
# ══════════════════════════════════════════════════════════════════
FROM nginx:alpine

# Copia los static files compilados desde el stage de build
# Angular 17 con application builder puede outputear a dist/panel/browser/
# Si tu build genera dist/panel/ directamente, ajusta la ruta aqui.
COPY --from=build /app/dist/panel/browser /usr/share/nginx/html

# Config personalizada de Nginx (SPA fallback + gzip + cache headers)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Puerto HTTP estandar
EXPOSE 80

# Nginx se inicia en foreground (requerido por Docker)
CMD ["nginx", "-g", "daemon off;"]
