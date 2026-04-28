package com.neversion.api.dashboard.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Dashboard: vendor sees their own KPIs, super_admin has full access.
 * US-015 / ADR-08: RBAC aligned with platform roles.
 */
@Configuration
public class DashboardSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/dashboard/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
