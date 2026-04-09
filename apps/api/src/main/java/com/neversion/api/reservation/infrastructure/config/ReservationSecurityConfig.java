package com.neversion.api.reservation.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.neversion.api.config.HttpSecurityCustomizer;

/**
 * Reservations: public checkout, admin validation/list, public
 * receipt/cancel/guest.
 */
@Configuration
public class ReservationSecurityConfig implements HttpSecurityCustomizer {

    @Override
    public void customize(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/reservations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reservations/{id}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservations/*/receipt").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservations/*/cancel").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservations/*/guest").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservations/*/validate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/reservations").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/reservations/**").hasRole("ADMIN"));
    }
}
