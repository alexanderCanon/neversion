package com.neversion.api.service.infrastructure.config;

import com.neversion.api.config.HttpSecurityCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Services RBAC rules (EPIC-02 / US-015 / ADR-08).
 * <p>
 * Public (no auth): GET /store/{vendorUuid}, GET /{id}
 * Vendor + Super Admin: POST, PUT, PATCH /status, DELETE, GET /vendor/{vendorUuid}
 * Super Admin: GET / (full list)
 */
@Configuration
public class ServiceSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // US-021: Public store catalog
                .requestMatchers(HttpMethod.GET, "/api/v1/services/store/**").permitAll()
                // Public single-service lookup
                .requestMatchers(HttpMethod.GET, "/api/v1/services/{id}").permitAll()
                // US-020: Vendor panel list — vendor or super_admin
                .requestMatchers(HttpMethod.GET, "/api/v1/services/vendor/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // US-017: Create — vendor or super_admin
                .requestMatchers(HttpMethod.POST, "/api/v1/services/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // US-018: Update — vendor or super_admin
                .requestMatchers(HttpMethod.PUT, "/api/v1/services/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // US-019: Toggle status — vendor or super_admin
                .requestMatchers(HttpMethod.PATCH, "/api/v1/services/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Delete — vendor or super_admin
                .requestMatchers(HttpMethod.DELETE, "/api/v1/services/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Full admin list — super_admin only
                .requestMatchers(HttpMethod.GET, "/api/v1/services")
                .hasRole("SUPER_ADMIN"));
    }
}
