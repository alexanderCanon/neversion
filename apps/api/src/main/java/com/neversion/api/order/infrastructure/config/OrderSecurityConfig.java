package com.neversion.api.order.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Orders: GET is authenticated (admin + customer), mutating is ADMIN-only.
 */
@Configuration
public class OrderSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/orders/**").authenticated()
                .requestMatchers("/api/v1/orders/**").hasRole("ADMIN"));
    }
}
