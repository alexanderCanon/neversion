package com.neversion.api.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global security configuration.
 * <p>
 * Handles cross-cutting security concerns only (stateless sessions, CSRF,
 * CORS, JWT decoder, public docs endpoints). Per-feature RBAC rules are
 * contributed by {@link HttpSecurityCustomizer} beans in each module's
 * {@code infrastructure/config} package.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${supabase.jwt.secret}")
    private String supabaseJwtSecret;

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    private final SupabaseJwtAuthConverter supabaseJwtAuthConverter;
    private final List<HttpSecurityCustomizer> securityCustomizers;

    public SecurityConfig(
            SupabaseJwtAuthConverter supabaseJwtAuthConverter,
            List<HttpSecurityCustomizer> securityCustomizers) {
        this.supabaseJwtAuthConverter = supabaseJwtAuthConverter;
        this.securityCustomizers = securityCustomizers;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // -- Stateless: no HTTP session --
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // -- CSRF disabled for stateless REST API --
                .csrf(csrf -> csrf.disable())

                // -- CORS --
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // -- Disable form login & HTTP Basic (API-only) --
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // -- Public docs & health --
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll());

        // -- Delegate per-feature RBAC rules --
        for (HttpSecurityCustomizer customizer : securityCustomizers) {
            customizer.customize(http);
        }

        // -- Catch-all: everything else requires authentication --
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        // -- OAuth2 Resource Server: validate JWTs with our custom converter --
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(supabaseJwtAuthConverter)));

        return http.build();
    }

    /**
     * Decodes Supabase JWTs using the project's HS256 secret key.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secretBytes = supabaseJwtSecret.getBytes();
        SecretKey secretKey = new SecretKeySpec(secretBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    /**
     * CORS configuration. Allows all origins during development.
     * Should be restricted in production.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
