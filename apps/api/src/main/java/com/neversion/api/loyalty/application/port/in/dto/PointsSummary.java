package com.neversion.api.loyalty.application.port.in.dto;

/**
 * Application-layer DTO summarizing a client's points balance.
 */
public record PointsSummary(long available, long pending, long total) {

    public static PointsSummary of(long available, long pending) {
        return new PointsSummary(available, pending, available + pending);
    }
}
