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
 * Domain service for reservation pricing logic (BR-13 v2, BR-14).
 * <p>
 * Discount tiers are read from the vendor's discount_cfg JSONB:
 * <pre>{@json
 * {
 *   "min_items": 2,
 *   "max_items": 4,
 *   "round_to": 5,
 *   "tiers": [
 *     { "count": 2, "discount_pct": 25 },
 *     { "count": 3, "discount_pct": 18 },
 *     { "count": 4, "discount_pct": 22 }
 *   ]
 * }
 * }</pre>
 * The discount is a percentage applied to the gross total of BY_PROFILE items,
 * then rounded to the nearest multiple of {@code round_to} (e.g. Q5).
 * <p>
 * Backward compatibility: if tiers use the legacy {@code from}/{@code to} format,
 * the service falls back to the old matching logic without rounding.
 */
@Service
public class ReservationPricingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Calculates the gross total before any combo discount.
     *
     * @param details reservation line items with unit_price and qty already set
     * @return sum of (qty * unitPrice) for all items
     */
    public BigDecimal calculateGrossTotal(List<ReservationDetail> details) {
        return details.stream()
                .map(d -> d.unitPrice().multiply(BigDecimal.valueOf(d.qty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the combo discount amount using the vendor's tier configuration (BR-13 v2).
     * <p>
     * The discount is computed as a percentage of the gross total, then rounded to
     * the nearest multiple of {@code round_to}. Only BY_PROFILE items participate
     * in the discount (the caller is responsible for passing only those items).
     *
     * @param grossTotal       total before discount (sum of BY_PROFILE items only)
     * @param profileItemCount number of distinct BY_PROFILE services in the cart
     * @param discountCfgJson  vendor's discount_cfg JSON string (nullable — no discount if null)
     * @return discount amount (0 if no tier matches or discountCfg is absent)
     */
    public BigDecimal calculateComboDiscount(BigDecimal grossTotal, int profileItemCount,
                                              String discountCfgJson) {
        if (discountCfgJson == null || discountCfgJson.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(discountCfgJson);

            int minItems = root.has("min_items") ? root.get("min_items").asInt(2) : 2;
            if (profileItemCount < minItems) {
                return BigDecimal.ZERO;
            }

            JsonNode tiers = root.get("tiers");
            if (tiers == null || !tiers.isArray()) {
                return BigDecimal.ZERO;
            }

            BigDecimal discountPct = BigDecimal.ZERO;
            boolean useNewFormat = false;

            for (JsonNode tier : tiers) {
                JsonNode countNode = tier.get("count");
                if (countNode != null && !countNode.isNull()) {
                    useNewFormat = true;
                    int count = countNode.asInt();
                    if (profileItemCount == count) {
                        discountPct = BigDecimal.valueOf(tier.get("discount_pct").asDouble());
                        break;
                    }
                } else {
                    // Legacy format: from / to
                    int from = tier.get("from").asInt();
                    JsonNode toNode = tier.get("to");
                    int to = (toNode == null || toNode.isNull()) ? Integer.MAX_VALUE : toNode.asInt();
                    if (profileItemCount >= from && profileItemCount <= to) {
                        discountPct = BigDecimal.valueOf(tier.get("discount_pct").asDouble());
                        break;
                    }
                }
            }

            if (discountPct.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            BigDecimal rawDiscount = grossTotal.multiply(discountPct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (useNewFormat) {
                int roundTo = root.has("round_to") ? root.get("round_to").asInt(5) : 5;
                if (roundTo > 0) {
                    return roundToNearest(rawDiscount, roundTo);
                }
            }

            return rawDiscount;

        } catch (JsonProcessingException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Rounds a BigDecimal to the nearest multiple of {@code roundTo} using HALF_UP.
     * Example: roundToNearest(17.50, 5) = 20.00; roundToNearest(12.00, 5) = 10.00.
     */
    private BigDecimal roundToNearest(BigDecimal value, int roundTo) {
        BigDecimal divisor = BigDecimal.valueOf(roundTo);
        BigDecimal quotient = value.divide(divisor, 0, RoundingMode.HALF_UP);
        return quotient.multiply(divisor).setScale(2, RoundingMode.HALF_UP);
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
