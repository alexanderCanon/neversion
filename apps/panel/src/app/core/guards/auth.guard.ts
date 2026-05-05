import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SupabaseService } from '../services/supabase.service';

/**
 * Auth Guard protects private routes. If no session exists, the user is
 * redirected to the login page, preserving the requested URL.
 */
export const authGuard: CanActivateFn = async (_route, state) => {
  const router = inject(Router);
  const supabaseService = inject(SupabaseService);

  const { data } = await supabaseService.client.auth.getSession();

  if (data.session) {
    return true;
  }

  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};
