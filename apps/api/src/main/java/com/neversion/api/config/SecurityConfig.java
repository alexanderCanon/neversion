package com.neversion.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Global security configuration.
 * <p>
 * Handles cross-cutting security concerns only (stateless sessions, CSRF,
 * CORS, public docs endpoints). JWT validation is now handled by the
 * Cloudflare API Gateway. Per-feature RBAC rules are
 * contributed by {@link HttpSecurityCustomizer} beans in each module's
 * {@code infrastructure/config} package.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;
    private final List<HttpSecurityCustomizer> securityCustomizers;

    public SecurityConfig(
            GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter,
            List<HttpSecurityCustomizer> securityCustomizers) {
        this.gatewayHeaderAuthenticationFilter = gatewayHeaderAuthenticationFilter;
        this.securityCustomizers = securityCustomizers;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless REST API
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)));

        http.addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Public docs, health probe (load balancer), and protected actuator
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN"));

        // -- Delegate per-feature RBAC rules --
        for (HttpSecurityCustomizer customizer : securityCustomizers) {
            customizer.customize(http);
        }

        // -- Catch-all: everything else requires authentication --
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        // -- HTTP Security Headers (second line of defense after Traefik) --
        http.headers(headers -> headers
                // Prevent MIME-type sniffing
                .contentTypeOptions(Customizer.withDefaults())
                // Deny embedding in iframes (clickjacking protection)
                .frameOptions(frame -> frame.deny())
                // HSTS: force HTTPS for 1 year including subdomains
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31_536_000))
                // Referrer-Policy: only send origin on cross-origin requests
                .referrerPolicy(referrer -> referrer.policy(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // X-XSS-Protection deliberately set to 0 — modern browsers handle XSS
                // natively; the legacy header can introduce vulnerabilities in old IE.
                .addHeaderWriter((request, response) ->
                        response.setHeader("X-XSS-Protection", "0")));

        return http.build();
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
