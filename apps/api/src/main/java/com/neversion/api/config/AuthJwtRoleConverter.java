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
 * Extracts the user role from an auth JWT and maps it to Spring Security authorities.
 *
 * It checks the {@code app_metadata} claim.
 * If the claim contains {@code "role": "vendor"}, the user is granted
 * {@code ROLE_VENDOR}, etc. If no role is present the token is authenticated
 * but granted no authorities — it will be denied by any {@code hasRole()} rule.
 */
@Component
public class AuthJwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(AuthJwtRoleConverter.class);

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        log.debug("JWT subject: {}", jwt.getSubject());
        log.debug("JWT claims: {}", jwt.getClaims());
        log.debug("Extracted authorities: {}", authorities);

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        String role = extractRole(jwt);
        if (role != null && !role.isBlank()) {
            String springRole = "ROLE_" + role.toUpperCase();
            authorities.add(new SimpleGrantedAuthority(springRole));
            log.debug("Mapped role '{}' -> '{}'", role, springRole);
        } else {
            log.debug("No role found in JWT claims for subject: {}", jwt.getSubject());
        }

        return authorities;
    }

    private String extractRole(Jwt jwt) {
        Map<String, Object> appMetadata = jwt.getClaim("app_metadata");
        if (appMetadata != null && appMetadata.containsKey("role")) {
            return String.valueOf(appMetadata.get("role"));
        }

        return null;
    }
}
