package com.neversion.api.infrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public abstract class EnumConverter<T extends Enum<T>> implements AttributeConverter<T, String> {

    private final Class<T> clazz;

    protected EnumConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return (attribute != null) ? attribute.name().toLowerCase() : null;
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        return (dbData != null) ? Enum.valueOf(clazz, dbData.toUpperCase()) : null;
    }
}
