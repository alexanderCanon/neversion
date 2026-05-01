package com.neversion.api.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Extracts the user role from a Supabase JWT and maps it to Spring Security
 * authorities.
 *
 * Supabase stores custom roles in the {@code raw_app_meta_data} claim.
 * If the claim contains {@code "role": "admin"}, the user is granted
 * {@code ROLE_ADMIN}.
 */
@Component
public class SupabaseJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(SupabaseJwtAuthConverter.class);

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        log.debug("JWT subject: {}", jwt.getSubject());
        log.debug("JWT claims: {}", jwt.getClaims());
        log.debug("Extracted authorities: {}", authorities);

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    /**
     * Extracts granted authorities from the JWT's {@code raw_app_meta_data.role}
     * claim.
     * Falls back to {@code user_metadata.role} if {@code raw_app_meta_data} is
     * absent.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        String role = extractRole(jwt);
        if (role != null && !role.isBlank()) {
            // Map the Supabase role to a Spring Security authority (e.g., "admin" →
            // "ROLE_ADMIN")
            String springRole = "ROLE_" + role.toUpperCase();
            authorities.add(new SimpleGrantedAuthority(springRole));
            log.debug("Mapped Supabase role '{}' → '{}'", role, springRole);
        } else {
            log.debug("No role found in JWT claims for subject: {}", jwt.getSubject());
        }

        return authorities;
    }

    private String extractRole(Jwt jwt) {
        // Strict: raw_app_meta_data.role (Supabase Admin stores custom roles here)
        Map<String, Object> appMetadata = jwt.getClaim("app_metadata");
        if (appMetadata != null && appMetadata.containsKey("role")) {
            return String.valueOf(appMetadata.get("role"));
        }

        return null;
    }
}
