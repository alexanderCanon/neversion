package com.neversion.api.subscription.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SubscriptionRenewalDomainService — BR-07 unit tests")
class SubscriptionRenewalDomainServiceUT {

    private final SubscriptionRenewalDomainService service = new SubscriptionRenewalDomainService();

    @Test
    @DisplayName("should calculate from original due date when payment is within grace period")
    void calculateNewDueDate_withinGrace_shouldUseOriginalDueDate() {
        LocalDate result = service.calculateNewDueDate(
                LocalDate.of(2026, 4, 27),
                LocalDate.of(2026, 4, 29),
                2);

        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 27));
    }

    @Test
    @DisplayName("should calculate from payment date when payment is outside grace period")
    void calculateNewDueDate_outsideGrace_shouldUsePaymentDate() {
        LocalDate result = service.calculateNewDueDate(
                LocalDate.of(2026, 4, 26),
                LocalDate.of(2026, 4, 29),
                2);

        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 29));
    }

    @Test
    @DisplayName("should include exact grace boundary")
    void calculateNewDueDate_exactGraceBoundary_shouldUseOriginalDueDate() {
        LocalDate result = service.calculateNewDueDate(
                LocalDate.of(2026, 4, 27),
                LocalDate.of(2026, 4, 29),
                2);

        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 27));
    }

    @Test
    @DisplayName("should calculate from original due date when paid on due date")
    void calculateNewDueDate_sameDay_shouldUseOriginalDueDate() {
        LocalDate result = service.calculateNewDueDate(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 4, 29),
                2);

        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 29));
    }
}
