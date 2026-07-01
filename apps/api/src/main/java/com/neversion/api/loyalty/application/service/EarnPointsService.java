package com.neversion.api.loyalty.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.loyalty.application.port.in.EarnPointsUseCase;
import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryType;
import com.neversion.api.loyalty.domain.port.out.PointsLedgerRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * Credits loyalty points for an approved order based on the vendor's
 * rewards_cfg (BR: earn_pct applied to the order's net total, floored to a whole point).
 * No-op if the vendor has rewards disabled or no config set.
 */
@Service
public class EarnPointsService implements EarnPointsUseCase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PointsLedgerRepositoryPort pointsLedgerRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public EarnPointsService(PointsLedgerRepositoryPort pointsLedgerRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.pointsLedgerRepositoryPort = pointsLedgerRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    @Override
    @Transactional
    public void earnForOrder(Long orderId, Long clientId, Long vendorId, BigDecimal orderTotal) {
        if (clientId == null || orderTotal == null || orderTotal.signum() <= 0) {
            return;
        }

        Vendor vendor = vendorRepositoryPort.findByInternalId(vendorId).orElse(null);
        if (vendor == null || vendor.getRewardsCfg() == null || vendor.getRewardsCfg().isBlank()) {
            return;
        }

        JsonNode cfg;
        try {
            cfg = OBJECT_MAPPER.readTree(vendor.getRewardsCfg());
        } catch (Exception e) {
            return;
        }

        if (!cfg.path("enabled").asBoolean(false)) {
            return;
        }

        double earnPct = cfg.path("earn_pct").asDouble(0);
        if (earnPct <= 0) {
            return;
        }

        long points = orderTotal
                .multiply(BigDecimal.valueOf(earnPct))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR)
                .longValue();

        if (points <= 0) {
            return;
        }

        pointsLedgerRepositoryPort.save(PointsLedgerEntry.builder()
                .clientId(clientId)
                .vendorId(vendorId)
                .orderId(orderId)
                .entryType(PointsEntryType.EARN)
                .status(PointsEntryStatus.AVAILABLE)
                .points(points)
                .notes("Earned from order #" + orderId)
                .build());
    }
}
