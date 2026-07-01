package com.neversion.api.vendor.infrastructure.adapters.out.mapper;

import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.infrastructure.adapters.out.VendorEntity;

/**
 * Explicit mapper between Vendor domain model and VendorEntity.
 * No cross-module references — userId is a plain Long in both model and entity.
 */
public class VendorPersistenceMapper {

    private VendorPersistenceMapper() {}

    public static Vendor toDomain(VendorEntity entity) {
        return Vendor.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .userId(entity.getUserId())
                .storeName(entity.getStoreName())
                .logoUrl(entity.getLogoUrl())
                .bankDetails(entity.getBankDetails())
                .discountCfg(entity.getDiscountCfg())
                .rewardsCfg(entity.getRewardsCfg())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static VendorEntity toEntity(Vendor domain) {
        return VendorEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .userId(domain.getUserId())
                .storeName(domain.getStoreName())
                .logoUrl(domain.getLogoUrl())
                .bankDetails(domain.getBankDetails())
                .discountCfg(domain.getDiscountCfg())
                .rewardsCfg(domain.getRewardsCfg())
                .build();
    }
}
