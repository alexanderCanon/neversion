package com.neversion.api.client.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Security rules for client CRUD endpoints (EPIC-04).
 *
 * All client management operations require VENDOR or SUPER_ADMIN role —
 * clients are managed exclusively from the admin panel (ADR-08, US-029..032).
 *
 * Note: client self-registration via /api/v1/auth/clients is handled by
 * AuthSecurityConfig (US-013, public endpoint).
 */
@Configuration
public class ClientSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // US-031 — Create client manually (vendor panel)
                .requestMatchers(HttpMethod.POST, "/api/v1/clients")
                        .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // US-029 — List clients scoped to vendor
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/vendor/**")
                        .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // US-030 — Client detail with subscriptions + orders
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/*/detail")
                        .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // EPIC-09 / US-061 — Client panel access list used to start renewals
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/me/accesses")
                        .hasRole("CLIENT")
                // EPIC-09 / US-059 — Authenticated client order history
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/me/orders")
                        .hasRole("CLIENT")
                // EPIC-09 / US-060 — Authenticated client reservation/receipt statuses
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/me/reservations")
                        .hasRole("CLIENT")
                // EPIC-09 / US-062 — Authenticated client profile self-service
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/me")
                        .hasRole("CLIENT")
                .requestMatchers(HttpMethod.PUT, "/api/v1/clients/me")
                        .hasRole("CLIENT")
                // Generic get by UUID
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/**")
                        .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // US-032 — Edit basic data
                .requestMatchers(HttpMethod.PUT, "/api/v1/clients/**")
                        .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Delete
                .requestMatchers(HttpMethod.DELETE, "/api/v1/clients/**")
                        .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
