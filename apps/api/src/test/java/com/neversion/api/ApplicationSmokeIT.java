package com.neversion.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Smoke integration test that boots the full Spring context against a real
 * PostgreSQL Testcontainer.
 * <p>
 * Verifies in a single test that:
 * <ul>
 *   <li>All {@code @Configuration} classes load without errors</li>
 *   <li>The unified Flyway baseline migration runs cleanly (V1__init_unified_schema.sql)</li>
 *   <li>Every bean (repositories, services, controllers, security) wires correctly</li>
 *   <li>{@code @EnableScheduling} initialises without errors</li>
 * </ul>
 * If this test passes, the Docker image will boot on EC2.
 */
@SpringBootTest
@DisplayName("Application Smoke IT — full context loads")
class ApplicationSmokeIT extends BaseIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("contextLoads - Spring context boots without errors")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
