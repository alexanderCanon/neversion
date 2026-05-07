package com.neversion.api.reservation.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.reservation.domain.model.ReservationDetail;

/**
 * Domain service for reservation pricing logic (BR-13, BR-14).
 * <p>
 * Discount tiers are read from the vendor's discount_cfg JSONB:
 * { "min_items": 2, "tiers": [{ "from": 2, "to": 3, "discount_pct": 5 }, ...] }
 * </p>
 */
@Service
public class ReservationPricingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Calculates the gross total before any combo discount.
     *
     * @param details reservation line items with unit_price and qty already set
     * @return sum of (qty × unitPrice) for all items
     */
    public BigDecimal calculateGrossTotal(List<ReservationDetail> details) {
        return details.stream()
                .map(d -> d.unitPrice().multiply(BigDecimal.valueOf(d.qty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the combo discount amount using the vendor's tier configuration (BR-13).
     *
     * @param grossTotal    total before discount
     * @param totalItemQty  total quantity of items in the cart (sum of all qty, not just line count)
     * @param discountCfgJson vendor's discount_cfg JSON string (nullable — no discount if null)
     * @return discount amount (0 if no tier matches or discountCfg is absent)
     */
    public BigDecimal calculateComboDiscount(BigDecimal grossTotal, int totalItemQty,
                                              String discountCfgJson) {
        if (discountCfgJson == null || discountCfgJson.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(discountCfgJson);

            int minItems = root.has("min_items") ? root.get("min_items").asInt(2) : 2;
            if (totalItemQty < minItems) {
                return BigDecimal.ZERO;
            }

            JsonNode tiers = root.get("tiers");
            if (tiers == null || !tiers.isArray()) {
                return BigDecimal.ZERO;
            }

            // Find the matching tier — tiers are expected sorted by "from" ascending
            BigDecimal discountPct = BigDecimal.ZERO;
            for (JsonNode tier : tiers) {
                int from = tier.get("from").asInt();
                JsonNode toNode = tier.get("to");
                int to = (toNode == null || toNode.isNull()) ? Integer.MAX_VALUE : toNode.asInt();

                if (totalItemQty >= from && totalItemQty <= to) {
                    discountPct = BigDecimal.valueOf(tier.get("discount_pct").asDouble());
                    break;
                }
            }

            if (discountPct.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            // discount_pct is a percentage (e.g. 5 = 5%), convert to decimal
            return grossTotal.multiply(discountPct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        } catch (JsonProcessingException e) {
            // Malformed JSON — log and return zero discount
            return BigDecimal.ZERO;
        }
    }

    /**
     * Returns the final total after applying the combo discount.
     *
     * @param grossTotal total before discount
     * @param discount   discount amount
     * @return grossTotal - discount
     */
    public BigDecimal calculateFinalTotal(BigDecimal grossTotal, BigDecimal discount) {
        return grossTotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }
}
