package com.neversion.api.vendor.infrastructure.config;

import com.neversion.api.config.HttpSecurityCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Vendor RBAC rules (ADR-08).
 * <p>
 * Public:           GET /api/v1/vendors/public/** — storefront multi-tenancy resolution
 * VENDOR/SUPER_ADMIN: all other mutations on /api/v1/vendors/**
 */
@Configuration
public class VendorSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // Public endpoint — storefront vendor identity resolution (US multitenancy)
                .requestMatchers(HttpMethod.GET, "/api/v1/vendors/public/**").permitAll()
                // All other vendor operations require VENDOR or SUPER_ADMIN
                .requestMatchers("/api/v1/vendors/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
