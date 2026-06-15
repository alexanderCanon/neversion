package com.neversion.api.subscription.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Subscriptions: vendor manages their client subscriptions,
 * super_admin has full access.
 * US-015 / ADR-08: RBAC aligned with platform roles.
 */
@Configuration
public class SubscriptionSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/subscriptions/detect-expired")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                .requestMatchers("/api/v1/subscriptions/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
