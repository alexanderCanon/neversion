package com.neversion.api;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for all Spring Boot integration tests.
 * <p>
 * Uses a singleton PostgreSQL container started once per JVM, shared across
 * all subclasses. This prevents the container from being stopped and restarted
 * between test classes, which would invalidate the cached Spring application context.
 * Cleanup is handled by Testcontainers Ryuk on JVM exit.
 */
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        DockerImageName myImage = DockerImageName.parse("mirror.gcr.io/library/postgres:16-alpine").asCompatibleSubstituteFor("postgres");
        POSTGRES = new PostgreSQLContainer<>(myImage)
                .withCommand("postgres", "-c", "max_connections=200")
                .withTmpFs(java.util.Map.of("/var/lib/postgresql/data", "rw"));
        POSTGRES.start();


    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("neversion.gateway.secret", () -> "test-gateway-secret-change-in-prod");
    }
}
