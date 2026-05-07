package com.neversion.api.shared.domain.model.enums;

import com.neversion.api.infrastructure.EnumConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for CategoryType enum.
 * Writes lowercase to DB (e.g. "streaming"), reads via toUpperCase() → enum.
 * This aligns with the project-wide convention: varchar columns store lowercase,
 * Java enums use UPPERCASE.
 */
@Converter(autoApply = true)
public class CategoryTypeConverter extends EnumConverter<CategoryType> {

    public CategoryTypeConverter() {
        super(CategoryType.class);
    }
}
