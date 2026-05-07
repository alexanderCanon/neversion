package com.neversion.api.user.infrastructure.adapters.out.converter;

import com.neversion.api.user.domain.model.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists UserRole as lowercase string (NFR-06).
 * e.g. SUPER_ADMIN → "super_admin", VENDOR → "vendor"
 */
@Converter(autoApply = false)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return UserRole.valueOf(dbData.toUpperCase());
    }
}
