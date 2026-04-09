package com.neversion.api.client.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * User Guests: create & view/edit by UUID is public, delete is ADMIN-only.
 */
@Configuration
public class ClientSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/clients").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/clients/{id}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/clients/{id}").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/clients/**").hasRole("ADMIN"));
    }
}
