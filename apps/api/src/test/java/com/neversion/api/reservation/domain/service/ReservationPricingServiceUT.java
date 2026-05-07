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
}
