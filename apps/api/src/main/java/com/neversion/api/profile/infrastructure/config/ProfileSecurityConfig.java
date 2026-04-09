package com.neversion.api.profile.infrastructure.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import com.neversion.api.config.HttpSecurityCustomizer;

@Component
public class ProfileSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/profiles/**").hasRole("ADMIN"));
    }
}
