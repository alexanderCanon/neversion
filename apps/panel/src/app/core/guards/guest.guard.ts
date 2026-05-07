import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * GuestGuard prevents authenticated users from accessing guest-only routes like /login.
 */
export const guestGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);

  if (!authService.currentSession()) {
    return true;
  }

  return router.createUrlTree(['/']);
};
