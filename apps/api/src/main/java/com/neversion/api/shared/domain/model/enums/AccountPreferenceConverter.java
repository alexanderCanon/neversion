package com.neversion.api.shared.domain.model.enums;

import com.neversion.api.infrastructure.EnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountPreferenceConverter extends EnumConverter<AccountPreference> {

    public AccountPreferenceConverter() {
        super(AccountPreference.class);
    }
}
