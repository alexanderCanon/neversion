package com.neversion.api.reservation.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neversion.api.reservation.domain.model.ReservationDetail;

/**
 * Domain service for reservation pricing logic.
 * <p>
 * Implements BR-03 (Combo Discount):
 * - 1 item: 0% discount (list price).
 * - 2+ items: 2% automatic discount on the subtotal.
 * </p>
 */
@Service
public class ReservationPricingService {

    private static final BigDecimal COMBO_DISCOUNT_RATE = new BigDecimal("0.02");
    private static final int COMBO_THRESHOLD = 2;

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
     * Calculates the combo discount amount (BR-03).
     *
     * @param grossTotal total before discount
     * @param itemCount  number of distinct line items in the cart
     * @return discount amount (0 if fewer than COMBO_THRESHOLD items)
     */
    public BigDecimal calculateComboDiscount(BigDecimal grossTotal, int itemCount) {
        if (itemCount >= COMBO_THRESHOLD) {
            return grossTotal.multiply(COMBO_DISCOUNT_RATE)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
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
