package com.neversion.api.account.infrastructure.adapters.out.converter;

import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.infrastructure.EnumConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for SaleMode enum.
 * Writes lowercase to DB (by_profile | full_account), reads via toUpperCase() → enum.
 * Replaces @Enumerated(EnumType.STRING) which would write UPPERCASE, inconsistent
 * with the V1 migration DEFAULT 'by_profile' (lowercase).
 */
@Converter(autoApply = true)
public class SaleModeConverter extends EnumConverter<SaleMode> {

    public SaleModeConverter() {
        super(SaleMode.class);
    }
}
