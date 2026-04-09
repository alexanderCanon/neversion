package com.neversion.api.shared.domain.model.enums;

import com.neversion.api.infrastructure.EnumConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountTypeConverter extends EnumConverter<AccountType> {

    public AccountTypeConverter() {
        super(AccountType.class);
    }
}
