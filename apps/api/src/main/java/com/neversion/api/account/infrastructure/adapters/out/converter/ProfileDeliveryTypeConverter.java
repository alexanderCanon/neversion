package com.neversion.api.account.infrastructure.adapters.out.converter;

import com.neversion.api.account.domain.model.enums.ProfileDeliveryType;
import com.neversion.api.infrastructure.EnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProfileDeliveryTypeConverter extends EnumConverter<ProfileDeliveryType> {

    public ProfileDeliveryTypeConverter() {
        super(ProfileDeliveryType.class);
    }
}
