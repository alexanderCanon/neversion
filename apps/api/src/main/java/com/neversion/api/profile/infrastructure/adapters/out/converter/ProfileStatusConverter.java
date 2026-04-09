package com.neversion.api.profile.infrastructure.adapters.out.converter;

import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.infrastructure.EnumConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProfileStatusConverter extends EnumConverter<ProfileStatus> {

    public ProfileStatusConverter() {
        super(ProfileStatus.class);
    }
}
