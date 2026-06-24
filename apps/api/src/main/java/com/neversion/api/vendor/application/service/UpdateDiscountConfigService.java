package com.neversion.api.vendor.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.vendor.application.port.in.UpdateDiscountConfigUseCase;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * Application service for updating a vendor's discount configuration (BR-13 v2).
 * <p>
 * Validates the JSON structure before persisting:
 * <ul>
 *   <li>Must be parseable JSON.</li>
 *   <li>{@code min_items} >= 2.</li>
 *   <li>{@code max_items} <= 4 (hard cap per business rule).</li>
 *   <li>{@code round_to} > 0 (typically 5 for Q5 rounding).</li>
 *   <li>{@code tiers} is a non-empty array with {@code count} and {@code discount_pct} (0-100).</li>
 *   <li>Tier counts must be within [min_items, max_items] and consecutive.</li>
 * </ul>
 */
@Service
public class UpdateDiscountConfigService implements UpdateDiscountConfigUseCase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int HARD_MAX_ITEMS = 4;
    private static final int HARD_MIN_ITEMS = 2;

    private final VendorSecurityService vendorSecurityService;
    private final VendorRepositoryPort vendorRepositoryPort;

    public UpdateDiscountConfigService(
            VendorSecurityService vendorSecurityService,
            VendorRepositoryPort vendorRepositoryPort) {
        this.vendorSecurityService = vendorSecurityService;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    @Override
    @Transactional
    public String updateDiscountConfig(String callerExternalId, String discountCfgJson) {
        validateStructure(discountCfgJson);

        Vendor vendor = vendorSecurityService.resolveCallerVendor(callerExternalId);
        vendor.setDiscountCfg(discountCfgJson);
        Vendor saved = vendorRepositoryPort.save(vendor);
        return saved.getDiscountCfg();
    }

    private void validateStructure(String json) {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            throw new BusinessRuleException("discount_cfg must be valid JSON: " + e.getMessage());
        }

        int minItems = root.path("min_items").asInt(HARD_MIN_ITEMS);
        if (minItems < HARD_MIN_ITEMS) {
            throw new BusinessRuleException("min_items must be at least " + HARD_MIN_ITEMS);
        }

        int maxItems = root.path("max_items").asInt(HARD_MAX_ITEMS);
        if (maxItems > HARD_MAX_ITEMS) {
            throw new BusinessRuleException("max_items cannot exceed " + HARD_MAX_ITEMS);
        }
        if (maxItems < minItems) {
            throw new BusinessRuleException("max_items cannot be less than min_items");
        }

        int roundTo = root.path("round_to").asInt(5);
        if (roundTo <= 0) {
            throw new BusinessRuleException("round_to must be a positive number");
        }

        JsonNode tiers = root.get("tiers");
        if (tiers == null || !tiers.isArray() || tiers.isEmpty()) {
            throw new BusinessRuleException("tiers must be a non-empty array");
        }

        int expectedCount = minItems;
        for (JsonNode tier : tiers) {
            int count = tier.path("count").asInt(-1);
            double pct = tier.path("discount_pct").asDouble(-1);

            if (count != expectedCount) {
                throw new BusinessRuleException(
                        "tier counts must be consecutive starting at min_items. "
                                + "Expected count " + expectedCount + " but got " + count);
            }
            if (pct < 0 || pct > 100) {
                throw new BusinessRuleException(
                        "discount_pct must be between 0 and 100 for tier count " + count);
            }
            if (count > maxItems) {
                throw new BusinessRuleException(
                        "tier count " + count + " exceeds max_items " + maxItems);
            }
            expectedCount++;
        }
    }
}
