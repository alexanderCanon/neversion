package com.neversion.api.shared.domain.model.enums;

import com.neversion.api.infrastructure.EnumConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountStatusConverter extends EnumConverter<AccountStatus> {

    public AccountStatusConverter() {
        super(AccountStatus.class);
    }
}
