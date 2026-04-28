package com.neversion.api.account.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountRequest;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountResponse;

@Component
public class AccountMapper {

    public Account toDomain(AccountRequest request) {
        return request != null ? Account.builder()
                .email(request.email())
                .password(request.pass())
                .serviceId(request.serviceId())
                .saleMode(request.saleMode())
                .renewalDate(request.renewalDate())
                .plan(request.plan())
                .cost(request.cost())
                .source(request.source())
                .purchasedAt(request.purchasedAt())
                .notes(request.notes())
                .build() : null;
    }

    public AccountResponse toResponse(Account account) {
        return account != null ? AccountResponse.builder()
                .id(account.getUuid())
                .email(account.getEmail())
                // password intentionally omitted from response (security)
                .serviceId(account.getServiceId())
                .saleMode(account.getSaleMode())
                .status(account.getStatus())
                .renewalDate(account.getRenewalDate())
                .plan(account.getPlan())
                .cost(account.getCost())
                .source(account.getSource())
                .purchasedAt(account.getPurchasedAt())
                .notes(account.getNotes())
                .createdAt(account.getCreatedAt())
                .build() : null;
    }
}
