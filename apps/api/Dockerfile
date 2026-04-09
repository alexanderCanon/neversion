# ══════════════════════════════════════════════════════════════════
# Stage 1: BUILD — Compiles the application and produces a fat JAR
# Uses the full JDK image because Maven needs the compiler toolchain
# ══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy Maven wrapper files FIRST so Docker can cache the dependency
# download layer. This means re-builds only re-download when pom.xml
# changes, not on every source code edit.
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .

# Make the wrapper executable and download all dependencies offline.
# -B = batch mode (no interactive prompts, cleaner CI logs)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# NOW copy the source code. This layer only invalidates when src/ changes.
COPY src src

# Build the fat JAR, skipping tests (tests should run in CI pipeline).
# The resulting JAR is renamed to api.jar for a predictable COPY target,
# avoiding wildcard ambiguity if multiple JARs exist in target/.
RUN ./mvnw package -DskipTests -B \
    && mv target/api-*.jar target/api.jar

# ══════════════════════════════════════════════════════════════════
# Stage 2: RUN — Lightweight runtime image (JRE only, no compiler)
# This dramatically reduces the final image size (~300MB vs ~600MB)
# ══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jre

# ── Security: run as non-root user ──────────────────────────────
# By default Docker runs as root, which is a security risk.
# If the app is compromised, the attacker gets root inside the container.
# We create a dedicated user 'appuser' with UID 1001 (no login shell).
RUN groupadd --system appgroup && \
    useradd --system --gid appgroup --uid 1001 --shell /usr/sbin/nologin appuser

WORKDIR /app

# Copy the fat JAR from the build stage with a predictable name
COPY --from=build /app/target/api.jar app.jar

# ── Ensure the app user owns the working directory ──────────────
RUN chown -R appuser:appgroup /app

# Switch to non-root user for all subsequent commands and runtime
USER appuser

# ── Port exposure (documentation only, doesn't publish the port) ─
EXPOSE 8080

# ── JVM Memory Optimization ────────────────────────────────────
# JAVA_OPTS is an environment variable that injects JVM flags into
# the startup command. These flags are critical for running Java
# inside containers with limited memory (e.g., 512m in docker-compose).
#
# -XX:+UseContainerSupport
#   Tells the JVM to detect container memory/CPU limits (cgroups).
#   Enabled by default in JDK 17, but set explicitly for clarity.
#
# -XX:MaxRAMPercentage=75.0
#   The JVM heap will use at most 75% of the container's memory limit.
#   Example: 512m container → ~384m max heap. The remaining 25% is for
#   metaspace, thread stacks, NIO buffers, and OS overhead.
#
# -XX:InitialRAMPercentage=50.0
#   Start the heap at 50% of container memory. This avoids expensive
#   heap resizing during startup when the app loads Spring context.
#
# -XX:+UseG1GC
#   The Garbage-First collector is ideal for API workloads:
#   low-latency, predictable pause times, good for 256m–4g heaps.
#
# -XX:+ExitOnOutOfMemoryError
#   If the JVM runs out of memory, EXIT immediately instead of hanging.
#   This lets Docker/ECS/K8s detect the crash and restart the container.
#
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+ExitOnOutOfMemoryError"

# ── Health check ────────────────────────────────────────────────
# Docker will periodically hit the Actuator health endpoint to verify
# the app is alive. If it fails 3 times, the container is marked unhealthy
# and docker-compose can restart it (restart: unless-stopped).
# - interval: check every 30s
# - timeout: fail if no response in 10s
# - start_period: give the app 40s to boot before first check
# - retries: 3 consecutive failures = unhealthy
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# ── Entrypoint ──────────────────────────────────────────────────
# Uses shell form so that $JAVA_OPTS is expanded at runtime.
# This allows overriding JAVA_OPTS via docker-compose environment
# without rebuilding the image.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
