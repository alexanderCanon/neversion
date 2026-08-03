# Stage 1: Build
FROM rust:1.80-alpine AS builder
RUN apk add --no-cache musl-dev protobuf-dev
WORKDIR /app
COPY Cargo.toml build.rs ./
COPY proto ./proto
COPY src ./src
RUN cargo build --release

# Stage 2: Runtime
FROM alpine:3.20 AS runtime
RUN apk add --no-cache ca-certificates sqlite-libs
WORKDIR /app
COPY --from=builder /app/target/release/neversion-reservation-service /app/reservation-service

EXPOSE 50052
CMD ["/app/reservation-service"]
