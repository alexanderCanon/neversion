package com.neversion.api.gamesku.infrastructure.config;

import com.neversion.api.config.HttpSecurityCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class GameSkuSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // Public store catalog (active SKUs by game slug)
                .requestMatchers(HttpMethod.GET, "/api/v1/game-skus/store/**").permitAll()
                // Public single-SKU lookup
                .requestMatchers(HttpMethod.GET, "/api/v1/game-skus/{id}").permitAll()
                // Vendor panel list — vendor or super_admin
                .requestMatchers(HttpMethod.GET, "/api/v1/game-skus/vendor/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Create — vendor or super_admin
                .requestMatchers(HttpMethod.POST, "/api/v1/game-skus/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Update — vendor or super_admin
                .requestMatchers(HttpMethod.PUT, "/api/v1/game-skus/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Toggle status — vendor or super_admin
                .requestMatchers(HttpMethod.PATCH, "/api/v1/game-skus/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Soft-delete — vendor or super_admin
                .requestMatchers(HttpMethod.DELETE, "/api/v1/game-skus/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                // Full admin list — super_admin only
                .requestMatchers(HttpMethod.GET, "/api/v1/game-skus")
                .hasRole("SUPER_ADMIN"));
    }
}
