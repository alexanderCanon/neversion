package com.neversion.api.order.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Orders: GET is accessible by any authenticated user (vendor sees orders,
 * client can check their own). Mutating operations are vendor/super_admin only.
 * US-015 / ADR-08: RBAC aligned with platform roles.
 */
@Configuration
public class OrderSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/orders/**").authenticated()
                .requestMatchers("/api/v1/orders/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
