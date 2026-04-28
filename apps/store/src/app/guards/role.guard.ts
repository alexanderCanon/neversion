import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles = route.data['allowedRoles'] as string[];
  const user = authService.currentUserValue;

  if (user && allowedRoles.includes(user.role)) {
    return true;
  }

  // Unauthorized: redirect to home
  return router.createUrlTree(['/']);
};
