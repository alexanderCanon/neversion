package com.neversion.api.vendor.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.vendor.application.port.in.UpdateRewardsConfigUseCase;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * Application service for updating a vendor's loyalty points configuration.
 * <p>
 * Validates the JSON structure before persisting:
 * <ul>
 *   <li>Must be parseable JSON.</li>
 *   <li>{@code enabled} must be a boolean.</li>
 *   <li>{@code earn_pct} must be between 0 and 100.</li>
 * </ul>
 */
@Service
public class UpdateRewardsConfigService implements UpdateRewardsConfigUseCase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final VendorSecurityService vendorSecurityService;
    private final VendorRepositoryPort vendorRepositoryPort;

    public UpdateRewardsConfigService(
            VendorSecurityService vendorSecurityService,
            VendorRepositoryPort vendorRepositoryPort) {
        this.vendorSecurityService = vendorSecurityService;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    @Override
    @Transactional
    public String updateRewardsConfig(String callerExternalId, String rewardsCfgJson) {
        validateStructure(rewardsCfgJson);

        Vendor vendor = vendorSecurityService.resolveCallerVendor(callerExternalId);
        vendor.setRewardsCfg(rewardsCfgJson);
        Vendor saved = vendorRepositoryPort.save(vendor);
        return saved.getRewardsCfg();
    }

    private void validateStructure(String json) {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            throw new BusinessRuleException("rewards_cfg must be valid JSON: " + e.getMessage());
        }

        if (!root.has("enabled") || !root.get("enabled").isBoolean()) {
            throw new BusinessRuleException("rewards_cfg.enabled is required and must be a boolean");
        }

        double earnPct = root.path("earn_pct").asDouble(-1);
        if (earnPct < 0 || earnPct > 100) {
            throw new BusinessRuleException("rewards_cfg.earn_pct must be between 0 and 100");
        }
    }
}
