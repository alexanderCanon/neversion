import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const guestGuard: CanActivateFn = async () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  await firstValueFrom(authService.isRestoring$.pipe(filter(restoring => !restoring)));

  if (!authService.isLoggedIn()) {
    return true;
  }

  return router.createUrlTree(['/']);
};
