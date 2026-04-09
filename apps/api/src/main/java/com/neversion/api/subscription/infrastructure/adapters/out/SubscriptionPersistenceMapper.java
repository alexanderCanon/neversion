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
                .startDate(entity.getStartDate())
                .paymentDueDate(entity.getPaymentDueDate())
                .monthsPaid(entity.getMonthsPaid())
                .status(entity.getStatus())
                .notes(entity.getNotes())
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
                .startDate(domain.getStartDate())
                .paymentDueDate(domain.getPaymentDueDate())
                .monthsPaid(domain.getMonthsPaid())
                .status(domain.getStatus())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
