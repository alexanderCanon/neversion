package com.neversion.api.subscription.infrastructure.adapters.out.converter;

import com.neversion.api.infrastructure.EnumConverter;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SubStatusConverter extends EnumConverter<SubStatus> {

    public SubStatusConverter() {
        super(SubStatus.class);
    }
}
