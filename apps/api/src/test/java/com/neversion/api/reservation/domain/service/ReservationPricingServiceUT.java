package com.neversion.api.reservation.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.neversion.api.reservation.domain.model.ReservationDetail;

/**
 * Unit tests for ReservationPricingService — BR-13 tier-based discounts.
 */
@DisplayName("ReservationPricingService unit tests")
class ReservationPricingServiceUT {

    private ReservationPricingService pricingService;

    /**
     * Standard vendor discount_cfg for testing:
     * min_items=2, tier1: 2-3 items = 5%, tier2: 4+ items = 10%
     */
    private static final String DISCOUNT_CFG = """
            {
              "min_items": 2,
              "tiers": [
                { "from": 2, "to": 3, "discount_pct": 5 },
                { "from": 4, "to": null, "discount_pct": 10 }
              ]
            }
            """;

    /**
     * New format discount_cfg with count-based tiers and Q5 rounding (BR-13 v2).
     */
    private static final String DISCOUNT_CFG_V2 = """
            {
              "min_items": 2,
              "max_items": 4,
              "round_to": 5,
              "tiers": [
                { "count": 2, "discount_pct": 25 },
                { "count": 3, "discount_pct": 18 },
                { "count": 4, "discount_pct": 22 }
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        pricingService = new ReservationPricingService();
    }

    private ReservationDetail buildDetail(int qty, String unitPrice) {
        return new ReservationDetail(
                null,
                null, // uuid
                null, // reservationId
                1L,   // serviceId
                qty,
                new BigDecimal(unitPrice),
                new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(qty)));
    }

    @Test
    @DisplayName("calculateGrossTotal - should sum qty times unitPrice for all items")
    void calculateGrossTotal_shouldSumQtyTimesUnitPrice_forAllItems() {
        List<ReservationDetail> details = List.of(
                buildDetail(2, "50.00"),  // 100.00
                buildDetail(1, "30.00"),  // 30.00
                buildDetail(3, "10.00")); // 30.00

        BigDecimal grossTotal = pricingService.calculateGrossTotal(details);

        assertThat(grossTotal).isEqualByComparingTo(new BigDecimal("160.00"));
    }

    @Test
    @DisplayName("calculateGrossTotal - should return zero when no items")
    void calculateGrossTotal_shouldReturnZero_whenNoItems() {
        BigDecimal grossTotal = pricingService.calculateGrossTotal(Collections.emptyList());

        assertThat(grossTotal).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateComboDiscount - should return zero when below min_items threshold")
    void calculateComboDiscount_shouldReturnZero_whenBelowThreshold() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("100.00"), 1, DISCOUNT_CFG);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateComboDiscount - should apply 5% for 2-3 items (BR-13 tier 1)")
    void calculateComboDiscount_shouldApply5Percent_forTier1() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("200.00"), 2, DISCOUNT_CFG);

        // 200 * 5 / 100 = 10.00
        assertThat(discount).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("calculateComboDiscount - should apply 10% for 4+ items (BR-13 tier 2)")
    void calculateComboDiscount_shouldApply10Percent_forTier2() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("200.00"), 5, DISCOUNT_CFG);

        // 200 * 10 / 100 = 20.00
        assertThat(discount).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("calculateComboDiscount - should return zero when discountCfg is null")
    void calculateComboDiscount_shouldReturnZero_whenCfgNull() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("200.00"), 3, null);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateComboDiscount - should return zero when discountCfg is malformed JSON")
    void calculateComboDiscount_shouldReturnZero_whenCfgMalformed() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("200.00"), 3, "not-valid-json");

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateFinalTotal - should subtract discount from gross total")
    void calculateFinalTotal_shouldSubtractDiscount() {
        BigDecimal finalTotal = pricingService.calculateFinalTotal(
                new BigDecimal("200.00"), new BigDecimal("10.00"));

        assertThat(finalTotal).isEqualByComparingTo(new BigDecimal("190.00"));
    }

    // ── BR-13 v2: count-based tiers with rounding ───────────────────────────

    @Test
    @DisplayName("calculateComboDiscount v2 - should apply 25% for 2 profiles and round to Q5")
    void calculateComboDiscountV2_shouldApply25Percent_for2Profiles() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("100.00"), 2, DISCOUNT_CFG_V2);

        // 100 * 25 / 100 = 25.00 → already a multiple of 5
        assertThat(discount).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("calculateComboDiscount v2 - should apply 18% for 3 profiles and round to Q5")
    void calculateComboDiscountV2_shouldApply18Percent_for3Profiles() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("100.00"), 3, DISCOUNT_CFG_V2);

        // 100 * 18 / 100 = 18.00 → round to nearest 5 → 20.00
        assertThat(discount).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("calculateComboDiscount v2 - should apply 22% for 4 profiles and round to Q5")
    void calculateComboDiscountV2_shouldApply22Percent_for4Profiles() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("100.00"), 4, DISCOUNT_CFG_V2);

        // 100 * 22 / 100 = 22.00 → round to nearest 5 → 20.00
        assertThat(discount).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("calculateComboDiscount v2 - should round 17.50 up to 20.00 (Q5 rounding)")
    void calculateComboDiscountV2_shouldRound17_50To20() {
        // grossTotal=70, 25% → 17.50 → round to 20.00
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("70.00"), 2, DISCOUNT_CFG_V2);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("calculateComboDiscount v2 - should round 21.60 down to 20.00 (Q5 rounding)")
    void calculateComboDiscountV2_shouldRound21_60To20() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("120.00"), 3, DISCOUNT_CFG_V2);

        // 120 * 18 / 100 = 21.60 → round to nearest 5 → 20.00
        assertThat(discount).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("calculateComboDiscount v2 - should return zero for 1 profile (below min_items)")
    void calculateComboDiscountV2_shouldReturnZero_for1Profile() {
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("100.00"), 1, DISCOUNT_CFG_V2);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateComboDiscount v2 - should return zero when no tier matches")
    void calculateComboDiscountV2_shouldReturnZero_whenNoTierMatches() {
        // 5 profiles but max tier is count=4
        BigDecimal discount = pricingService.calculateComboDiscount(
                new BigDecimal("100.00"), 5, DISCOUNT_CFG_V2);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
