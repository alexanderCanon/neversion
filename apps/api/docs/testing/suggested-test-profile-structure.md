# Suggested Test Profile Structure

To ensure integration tests (`*IT.java`) run correctly, isolated from the development and production databases, the project must utilize a specific Spring profile (`test`).

## `application-test.yaml`

This file should be placed in `src/test/resources/application-test.yaml`.

```yaml
spring:
  datasource:
    # Testcontainers will automatically override the JDBC URL, username, and password
    # when @ServiceConnection is used, but providing dummy values prevents context startup errors.
    url: jdbc:postgresql://localhost:5432/testdb
    username: test
    password: test
    driver-class-name: org.postgresql.Driver

  flyway:
    enabled: true
    clean-disabled: false
    locations: classpath:db/migration

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true

supabase:
  jwt:
    # Dummy secret for tests to prevent ContextLoad failures due to missing properties
    secret: "dummy-secret-key-for-testing-only-1234567890"

logging:
  level:
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

## `BaseIntegrationTest.java` structure

It is critical that all integration tests inherit from a common base class to reuse the same Testcontainers instance and reduce test execution time.

```java
package com.neversion.api;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test") // Forces loading application-test.yaml
public abstract class BaseIntegrationTest {

    // Starts a single Postgres instance per JVM run.
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
}
```
