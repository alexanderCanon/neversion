package com.neversion.api.dashboard.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.neversion.api.dashboard.application.port.in.GetProfilesByAccountUseCase;
import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.ProfileResult;

@Service
public class GetProfilesByAccountService implements GetProfilesByAccountUseCase {

    private final DashboardQueryPort dashboardQueryPort;

    public GetProfilesByAccountService(DashboardQueryPort dashboardQueryPort) {
        this.dashboardQueryPort = dashboardQueryPort;
    }

    @Override
    public List<ProfileResult> getByAccountId(UUID accountId) {
        return dashboardQueryPort.findProfilesByAccountId(accountId);
    }
}
