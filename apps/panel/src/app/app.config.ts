import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { AuthService } from './core/services/auth.service';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { httpErrorInterceptor } from './core/interceptors/http-error.interceptor';
import { provideApi } from '@neversion/api-client';
import { runtimeConfig } from './core/config/runtime-config';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideApi(runtimeConfig.apiUrl),
    provideHttpClient(withInterceptors([authInterceptor, httpErrorInterceptor])),
    {
      provide: APP_INITIALIZER,
      useFactory: (authService: AuthService) => () => authService.initialize(),
      deps: [AuthService],
      multi: true
    }
  ]
};
