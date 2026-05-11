package com.neversion.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Prevents the JWT resource server from parsing the Prometheus service token.
 */
@Component
public class MonitoringAwareBearerTokenResolver implements BearerTokenResolver {

    private final DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
    private final String scrapeToken;

    public MonitoringAwareBearerTokenResolver(
            @Value("${neversion.monitoring.scrape-token:}") String scrapeToken) {
        this.scrapeToken = scrapeToken;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String token = delegate.resolve(request);
        if (MonitoringScrapeTokenAuthenticationFilter.PROMETHEUS_PATH.equals(request.getRequestURI())
                && scrapeToken != null
                && !scrapeToken.isBlank()
                && scrapeToken.equals(token)) {
            return null;
        }

        return token;
    }
}
