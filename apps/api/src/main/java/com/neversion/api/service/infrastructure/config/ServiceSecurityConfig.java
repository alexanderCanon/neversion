package com.neversion.api.service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Services: GET is public (catalog browsing), mutating is ADMIN-only.
 */
@Configuration
public class ServiceSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/services/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/services/**").hasRole("ADMIN"));
    }
}
