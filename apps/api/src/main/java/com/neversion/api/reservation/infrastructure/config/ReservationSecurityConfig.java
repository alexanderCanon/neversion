package com.neversion.api.reservation.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Reservations: public checkout flow (create, view, receipt, cancel, guest).
 * Vendor/super_admin operations: validate, list all, delete.
 * US-015 / ADR-08: RBAC aligned with platform roles.
 */
@Configuration
public class ReservationSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // Public (store checkout flow)
                .requestMatchers(HttpMethod.POST, "/api/v1/reservations/renew").hasRole("CLIENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/reservations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reservations/{id}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservations/*/receipt").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservations/*/cancel").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservations/*/guest").permitAll()
                // Vendor/Super Admin operations
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservations/*/validate")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/reservations")
                .hasAnyRole("VENDOR", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/reservations/**")
                .hasAnyRole("VENDOR", "SUPER_ADMIN"));
    }
}
