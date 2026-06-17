# syntax=docker/dockerfile:1.7

FROM oven/bun:1.1-alpine AS runtime
WORKDIR /workspace

# Copy the service package.json
COPY apps/notification-service/package.json ./apps/notification-service/

# Install dependencies using Bun
WORKDIR /workspace/apps/notification-service
RUN bun install --production

# Copy source and proto in the exact same directory structure to preserve relative paths
WORKDIR /workspace
COPY apps/notification-service/src ./apps/notification-service/src
COPY proto ./proto

WORKDIR /workspace/apps/notification-service
EXPOSE 50051

# Run the TypeScript server directly using Bun
CMD ["bun", "run", "src/server.ts"]
