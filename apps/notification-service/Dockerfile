FROM oven/bun:1.1-alpine AS runtime
WORKDIR /workspace/apps/notification-service

# Copy package.json to install dependencies using standalone context
COPY package.json ./
RUN bun install --production

# Copy local source code and local proto folder
COPY src ./src
COPY proto ./proto

EXPOSE 50051
CMD ["bun", "run", "src/server.ts"]
