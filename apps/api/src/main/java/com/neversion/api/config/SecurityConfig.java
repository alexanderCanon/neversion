package com.neversion.api.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    private final AuthJwtRoleConverter authJwtRoleConverter;
    private final MonitoringAwareBearerTokenResolver monitoringAwareBearerTokenResolver;
    private final MonitoringScrapeTokenAuthenticationFilter monitoringScrapeTokenAuthenticationFilter;
    private final List<HttpSecurityCustomizer> securityCustomizers;

    public SecurityConfig(
            AuthJwtRoleConverter authJwtRoleConverter,
            MonitoringAwareBearerTokenResolver monitoringAwareBearerTokenResolver,
            MonitoringScrapeTokenAuthenticationFilter monitoringScrapeTokenAuthenticationFilter,
            List<HttpSecurityCustomizer> securityCustomizers) {
        this.authJwtRoleConverter = authJwtRoleConverter;
        this.monitoringAwareBearerTokenResolver = monitoringAwareBearerTokenResolver;
        this.monitoringScrapeTokenAuthenticationFilter = monitoringScrapeTokenAuthenticationFilter;
        this.securityCustomizers = securityCustomizers;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless REST API
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Add Prometheus monitoring token auth filter before standard Spring Security filters
        http.addFilterBefore(monitoringScrapeTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Public docs, health probe (load balancer), and protected actuator
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/prometheus").access((authentication, context) -> {
                    boolean granted = authentication.get().getAuthorities().stream()
                            .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority())
                                    || MonitoringScrapeTokenAuthenticationFilter.SCRAPER_ROLE.equals(authority.getAuthority()));
                    return new AuthorizationDecision(granted);
                })
                .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN"));

        // -- Delegate per-feature RBAC rules --
        for (HttpSecurityCustomizer customizer : securityCustomizers) {
            customizer.customize(http);
        }

        // -- Catch-all: everything else requires authentication --
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        // -- OAuth2 Resource Server: validate JWTs with our custom converter --
        http.oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(monitoringAwareBearerTokenResolver)
                .jwt(jwt -> jwt
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(authJwtRoleConverter)));

        return http.build();
    }

    /**
     * Decodes JWTs using the project's HS256 secret key.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secretBytes = jwtSecret.getBytes();
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
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
