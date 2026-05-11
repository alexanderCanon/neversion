import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Fallback: Synchronous check of localStorage since Supabase restoreSession is async
  if (typeof localStorage !== 'undefined') {
    const authKey = Object.keys(localStorage).find((key) => key.startsWith('sb-') && key.endsWith('-auth-token'));
    if (authKey && localStorage.getItem(authKey)) {
      return true;
    }
  }

  // Redirect to login with returnUrl
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
