package com.neversion.api.notification.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * EPIC-08: Security config for notification endpoints.
 * POST /process is restricted to SUPER_ADMIN.
 * GET /health is public.
 */
@Configuration
public class NotificationSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/notifications/process").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET,  "/api/v1/notifications/health").permitAll());
    }
}
