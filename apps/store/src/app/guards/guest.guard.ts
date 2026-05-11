import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const guestGuard: CanActivateFn = async () => {
  const platformId = inject(PLATFORM_ID);
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  await firstValueFrom(authService.isRestoring$.pipe(filter(restoring => !restoring)));

  if (!authService.isLoggedIn()) {
    return true;
  }

  return router.createUrlTree(['/']);
};
