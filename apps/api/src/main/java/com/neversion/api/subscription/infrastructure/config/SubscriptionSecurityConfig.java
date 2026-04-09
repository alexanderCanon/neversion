package com.neversion.api.subscription.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Subscriptions: all operations require authentication (admin-only via
 * catch-all).
 */
@Configuration
public class SubscriptionSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/subscriptions/**").hasRole("ADMIN"));
    }
}
