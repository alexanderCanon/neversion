package com.neversion.api.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authenticates Prometheus scrape requests with a service token.
 */
@Component
public class MonitoringScrapeTokenAuthenticationFilter extends OncePerRequestFilter {

    public static final String PROMETHEUS_PATH = "/actuator/prometheus";
    public static final String SCRAPER_ROLE = "ROLE_MONITORING_SCRAPER";

    private final String scrapeToken;

    public MonitoringScrapeTokenAuthenticationFilter(
            @Value("${neversion.monitoring.scrape-token:}") String scrapeToken) {
        this.scrapeToken = scrapeToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !PROMETHEUS_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String candidateToken = authorization.substring("Bearer ".length());
            if (matchesConfiguredToken(candidateToken)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "prometheus",
                        null,
                        List.of(new SimpleGrantedAuthority(SCRAPER_ROLE)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean matchesConfiguredToken(String candidateToken) {
        if (scrapeToken == null || scrapeToken.isBlank() || candidateToken == null || candidateToken.isBlank()) {
            return false;
        }

        byte[] expected = scrapeToken.getBytes(StandardCharsets.UTF_8);
        byte[] actual = candidateToken.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
