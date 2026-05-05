package com.neversion.api.auth.infrastructure.config;

import com.neversion.api.config.HttpSecurityCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Security configuration for the auth module.
 * <p>
 * Registers RBAC rules for /api/v1/auth/** endpoints:
 * <ul>
 *   <li>GET /me — authenticated users</li>
 *   <li>POST /vendors — SUPER_ADMIN only (US-012 / ADR-08)</li>
 *   <li>POST /clients — public access (US-013 — store self-registration)</li>
 * </ul>
 */
@Configuration
public class AuthSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // US-013: Client self-registration is public (store visitors)
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/clients")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/me")
                .authenticated()
                // US-012: Only SUPER_ADMIN can register vendors
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/vendors")
                .hasRole("SUPER_ADMIN"));
    }
}
