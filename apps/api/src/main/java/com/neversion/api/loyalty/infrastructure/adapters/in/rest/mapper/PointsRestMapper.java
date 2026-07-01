package com.neversion.api.loyalty.infrastructure.adapters.in.rest.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.neversion.api.loyalty.application.port.in.dto.PointsMovementsPage;
import com.neversion.api.loyalty.application.port.in.dto.PointsSummary;
import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto.PointsMovementResponse;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto.PointsSummaryResponse;

@Component
public class PointsRestMapper {

    public PointsSummaryResponse toResponse(PointsSummary summary) {
        return new PointsSummaryResponse(summary.available(), summary.pending(), summary.total());
    }

    public PointsMovementResponse toResponse(PointsLedgerEntry entry) {
        return new PointsMovementResponse(
                entry.getUuid(),
                entry.getEntryType(),
                entry.getStatus(),
                entry.getPoints(),
                entry.getNotes(),
                entry.getCreatedAt());
    }

    public List<PointsMovementResponse> toResponseList(PointsMovementsPage page) {
        return page.movements().stream().map(this::toResponse).toList();
    }
}
