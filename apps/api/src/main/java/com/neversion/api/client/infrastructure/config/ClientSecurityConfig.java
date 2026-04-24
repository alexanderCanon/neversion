package com.neversion.api.client.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Client CRUD endpoints (legacy /api/v1/clients).
 * Create and view are public (store usage). Edit is public for self-service.
 * Delete is vendor/super_admin only.
 * <p>
 * Note: client self-registration via /api/v1/auth/clients is handled by
 * AuthSecurityConfig (US-013).
 * US-015 / ADR-08: RBAC aligned with platform roles.
 */
@Configuration
public class ClientSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/clients").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/{id}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/clients/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/clients")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/clients/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
