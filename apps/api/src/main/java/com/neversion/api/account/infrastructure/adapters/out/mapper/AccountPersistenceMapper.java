package com.neversion.api.account.infrastructure.adapters.out.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.infrastructure.adapters.out.AccountEntity;

@Component
public class AccountPersistenceMapper {

    public Account toDomain(AccountEntity entity) {
        if (entity == null) return null;
        return Account.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .serviceId(entity.getServiceId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .renewalDate(entity.getRenewalDate())
                .plan(entity.getPlan())
                .saleMode(entity.getSaleMode())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AccountEntity toEntity(Account domain) {
        if (domain == null) return null;
        return AccountEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .serviceId(domain.getServiceId())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .renewalDate(domain.getRenewalDate())
                .plan(domain.getPlan())
                .saleMode(domain.getSaleMode())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
