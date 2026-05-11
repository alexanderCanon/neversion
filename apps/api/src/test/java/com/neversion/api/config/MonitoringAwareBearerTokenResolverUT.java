package com.neversion.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

@DisplayName("MonitoringAwareBearerTokenResolver UT")
class MonitoringAwareBearerTokenResolverUT {

    private static final String SCRAPE_TOKEN = "test-prometheus-scrape-token";

    @Test
    @DisplayName("resolve - should ignore configured scrape token for Prometheus endpoint")
    void resolve_prometheusEndpointWithScrapeToken_returnsNull() {
        MonitoringAwareBearerTokenResolver resolver = new MonitoringAwareBearerTokenResolver(SCRAPE_TOKEN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/prometheus");
        request.addHeader("Authorization", "Bearer " + SCRAPE_TOKEN);

        String result = resolver.resolve(request);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve - should keep invalid scrape token for JWT validation")
    void resolve_prometheusEndpointWithInvalidToken_returnsToken() {
        MonitoringAwareBearerTokenResolver resolver = new MonitoringAwareBearerTokenResolver(SCRAPE_TOKEN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/prometheus");
        request.addHeader("Authorization", "Bearer wrong-token");

        String result = resolver.resolve(request);

        assertThat(result).isEqualTo("wrong-token");
    }

    @Test
    @DisplayName("resolve - should keep configured token outside Prometheus endpoint")
    void resolve_otherEndpointWithScrapeToken_returnsToken() {
        MonitoringAwareBearerTokenResolver resolver = new MonitoringAwareBearerTokenResolver(SCRAPE_TOKEN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/accounts");
        request.addHeader("Authorization", "Bearer " + SCRAPE_TOKEN);

        String result = resolver.resolve(request);

        assertThat(result).isEqualTo(SCRAPE_TOKEN);
    }
}
