package com.neversion.api.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * SPI for decentralized security configuration.
 * <p>
 * Each feature module provides a bean implementing this interface
 * to register its own authorization rules. The central {@link SecurityConfig}
 * collects all implementations and applies them to the filter chain.
 * </p>
 */
public interface HttpSecurityCustomizer {

    /**
     * Contributes authorization rules to the shared {@link HttpSecurity} instance.
     *
     * @param http the security builder to customize
     */
    void customize(HttpSecurity http) throws Exception;
}
