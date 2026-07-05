package com.neversion.api.game.infrastructure.config;

import com.neversion.api.config.HttpSecurityCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class GameSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // Public store catalog (active games only)
                .requestMatchers(HttpMethod.GET, "/api/v1/games/store/**").permitAll()
                // Public single-game lookup
                .requestMatchers(HttpMethod.GET, "/api/v1/games/{id}").permitAll()
                // Vendor panel list — vendor or super_admin
                .requestMatchers(HttpMethod.GET, "/api/v1/games/vendor/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Create — vendor or super_admin
                .requestMatchers(HttpMethod.POST, "/api/v1/games/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Update — vendor or super_admin
                .requestMatchers(HttpMethod.PUT, "/api/v1/games/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Toggle status — vendor or super_admin
                .requestMatchers(HttpMethod.PATCH, "/api/v1/games/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Soft-delete — vendor or super_admin
                .requestMatchers(HttpMethod.DELETE, "/api/v1/games/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Full admin list — super_admin only
                .requestMatchers(HttpMethod.GET, "/api/v1/games")
                .hasRole("SUPER_ADMIN"));
    }
}
