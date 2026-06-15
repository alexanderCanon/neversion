package com.neversion.api.subscription.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.neversion.api.subscription.domain.service.InventoryStateDomainService;
import com.neversion.api.subscription.domain.service.SubscriptionRenewalDomainService;

@Configuration
public class SubscriptionDomainConfig {

    @Bean
    SubscriptionRenewalDomainService subscriptionRenewalDomainService() {
        return new SubscriptionRenewalDomainService();
    }

    @Bean
    InventoryStateDomainService inventoryStateDomainService() {
        return new InventoryStateDomainService();
    }
}
