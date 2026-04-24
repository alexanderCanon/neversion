package com.neversion.api.service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Services: GET is public (catalog browsing on the store).
 * Mutations (POST/PUT/DELETE) are vendor + super_admin only.
 * US-015 / ADR-08: RBAC aligned with platform roles.
 */
@Configuration
public class ServiceSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/services/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/services/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/services/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/services/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
