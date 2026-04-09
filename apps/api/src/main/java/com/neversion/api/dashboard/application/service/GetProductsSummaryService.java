package com.neversion.api.dashboard.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neversion.api.dashboard.application.port.in.GetProductsSummaryUseCase;
import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.ProductSummaryResult;
import com.neversion.api.shared.domain.model.enums.CategoryType;

@Service
public class GetProductsSummaryService implements GetProductsSummaryUseCase {

    private final DashboardQueryPort dashboardQueryPort;

    public GetProductsSummaryService(DashboardQueryPort dashboardQueryPort) {
        this.dashboardQueryPort = dashboardQueryPort;
    }

    @Override
    public List<ProductSummaryResult> getByCategory(CategoryType category) {
        return dashboardQueryPort.findProductsByCategory(category.name().toLowerCase());
    }
}
