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
                .notes(request.notes())
                .build() : null;
    }

    public AccountResponse toResponse(Account account) {
        return account != null ? AccountResponse.builder()
                .id(account.getUuid())
                .email(account.getEmail())
                .pass(account.getPassword())
                .serviceId(account.getServiceId())
                .saleMode(account.getSaleMode())
                .renewalDate(account.getRenewalDate())
                .notes(account.getNotes())
                .createdAt(account.getCreatedAt())
                .build() : null;
    }
}
