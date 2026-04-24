package com.neversion.api.reservation.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.neversion.api.reservation.domain.model.ReservationDetail;

@DisplayName("ReservationPricingService unit tests")
class ReservationPricingServiceUT {

    private ReservationPricingService pricingService;

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
        // Given
        List<ReservationDetail> details = List.of(
                buildDetail(2, "50.00"),  // 100.00
                buildDetail(1, "30.00"),  // 30.00
                buildDetail(3, "10.00")); // 30.00

        // When
        BigDecimal grossTotal = pricingService.calculateGrossTotal(details);

        // Then
        assertThat(grossTotal).isEqualByComparingTo(new BigDecimal("160.00"));
    }

    @Test
    @DisplayName("calculateGrossTotal - should return zero when no items")
    void calculateGrossTotal_shouldReturnZero_whenNoItems() {
        // Given
        List<ReservationDetail> details = Collections.emptyList();

        // When
        BigDecimal grossTotal = pricingService.calculateGrossTotal(details);

        // Then
        assertThat(grossTotal).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateComboDiscount - should return zero when single item (BR-03)")
    void calculateComboDiscount_shouldReturnZero_whenSingleItem() {
        // Given
        BigDecimal grossTotal = new BigDecimal("100.00");

        // When
        BigDecimal discount = pricingService.calculateComboDiscount(grossTotal, 1);

        // Then
        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateComboDiscount - should return 2 percent when multiple items (BR-03)")
    void calculateComboDiscount_shouldReturn2Percent_whenMultipleItems() {
        // Given
        BigDecimal grossTotal = new BigDecimal("200.00");

        // When
        BigDecimal discount = pricingService.calculateComboDiscount(grossTotal, 2);

        // Then
        assertThat(discount).isEqualByComparingTo(new BigDecimal("4.00")); // 200 * 0.02 = 4.00
    }

    @Test
    @DisplayName("calculateFinalTotal - should subtract discount")
    void calculateFinalTotal_shouldSubtractDiscount() {
        // Given
        BigDecimal grossTotal = new BigDecimal("200.00");
        BigDecimal discount = new BigDecimal("4.00");

        // When
        BigDecimal finalTotal = pricingService.calculateFinalTotal(grossTotal, discount);

        // Then
        assertThat(finalTotal).isEqualByComparingTo(new BigDecimal("196.00"));
    }
}
