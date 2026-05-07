package com.neversion.api.subscription.infrastructure.adapters.out;

import org.springframework.stereotype.Component;

import com.neversion.api.subscription.domain.model.Subscription;

@Component
public class SubscriptionPersistenceMapper {

    public Subscription toDomain(SubscriptionEntity entity) {
        if (entity == null) return null;
        return Subscription.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .clientId(entity.getClientId())
                .profileId(entity.getProfileId())
                .orderId(entity.getOrderId())
                .serviceId(entity.getServiceId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .paymentDueDate(entity.getPaymentDueDate())
                .monthsPaid(entity.getMonthsPaid())
                .priceSold(entity.getPriceSold())
                .discountApplied(entity.getDiscountApplied())
                .saleMode(entity.getSaleMode())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .vendorId(entity.getVendorId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public SubscriptionEntity toEntity(Subscription domain) {
        if (domain == null) return null;
        return SubscriptionEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .clientId(domain.getClientId())
                .profileId(domain.getProfileId())
                .orderId(domain.getOrderId())
                .serviceId(domain.getServiceId())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .paymentDueDate(domain.getPaymentDueDate())
                .monthsPaid(domain.getMonthsPaid())
                .priceSold(domain.getPriceSold())
                .discountApplied(domain.getDiscountApplied())
                .saleMode(domain.getSaleMode())
                .status(domain.getStatus())
                .notes(domain.getNotes())
                .vendorId(domain.getVendorId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
