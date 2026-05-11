import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = async (_, state) => {
  const platformId = inject(PLATFORM_ID);
  const authService = inject(AuthService);
  const router = inject(Router);

  // SSR: localStorage is unavailable — let the client handle auth checks.
  // The server renders the page shell; the client-side guard fires on hydration.
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  // Wait until the async Supabase session restore finishes before evaluating.
  // Without this, the guard would always see currentUser$ as null on first load.
  await firstValueFrom(authService.isRestoring$.pipe(filter(restoring => !restoring)));

  if (authService.isLoggedIn()) {
    return true;
  }

  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
