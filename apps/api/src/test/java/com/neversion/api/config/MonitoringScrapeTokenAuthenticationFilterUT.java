package com.neversion.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("MonitoringScrapeTokenAuthenticationFilter UT")
class MonitoringScrapeTokenAuthenticationFilterUT {

    private static final String SCRAPE_TOKEN = "test-prometheus-scrape-token";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilter - should authenticate Prometheus scrape token")
    void doFilter_validScrapeToken_authenticatesRequest() throws Exception {
        MonitoringScrapeTokenAuthenticationFilter filter = new MonitoringScrapeTokenAuthenticationFilter(SCRAPE_TOKEN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/prometheus");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + SCRAPE_TOKEN);

        filter.doFilter(request, response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("prometheus");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly(MonitoringScrapeTokenAuthenticationFilter.SCRAPER_ROLE);
    }

    @Test
    @DisplayName("doFilter - should not authenticate invalid scrape token")
    void doFilter_invalidScrapeToken_doesNotAuthenticateRequest() throws Exception {
        MonitoringScrapeTokenAuthenticationFilter filter = new MonitoringScrapeTokenAuthenticationFilter(SCRAPE_TOKEN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/prometheus");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer wrong-token");

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilter - should skip non Prometheus endpoints")
    void doFilter_otherEndpoint_doesNotAuthenticateRequest() throws Exception {
        MonitoringScrapeTokenAuthenticationFilter filter = new MonitoringScrapeTokenAuthenticationFilter(SCRAPE_TOKEN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/accounts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        request.addHeader("Authorization", "Bearer " + SCRAPE_TOKEN);

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainInvoked.set(true));

        assertThat(chainInvoked).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
