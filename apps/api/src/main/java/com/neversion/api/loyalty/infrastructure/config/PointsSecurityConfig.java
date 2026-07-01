package com.neversion.api.loyalty.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Security rules for loyalty points endpoints.
 * <p>
 * Store-facing "me" endpoints require CLIENT role; vendor panel endpoints
 * require VENDOR or SUPER_ADMIN role, with ownership enforced in the
 * application layer (VendorSecurityService).
 */
@Configuration
public class PointsSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/me/points/**")
                        .hasRole("CLIENT")
                .requestMatchers(HttpMethod.GET, "/api/v1/vendor/clients/*/points/**")
                        .hasAnyRole("VENDOR", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/vendor/clients/*/points/adjust")
                        .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
