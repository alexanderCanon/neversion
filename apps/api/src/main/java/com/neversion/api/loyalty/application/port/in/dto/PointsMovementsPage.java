package com.neversion.api.loyalty.application.port.in.dto;

import java.util.List;

import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;

/**
 * Application-layer DTO for a paginated slice of a client's points ledger.
 */
public record PointsMovementsPage(List<PointsLedgerEntry> movements, long totalElements) {
}
