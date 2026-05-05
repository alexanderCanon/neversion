package com.neversion.api.user.application.port.in;

import com.neversion.api.user.domain.model.CurrentUserContextResult;

public interface GetCurrentUserContextUseCase {

    CurrentUserContextResult get(String callerExternalId);
}
