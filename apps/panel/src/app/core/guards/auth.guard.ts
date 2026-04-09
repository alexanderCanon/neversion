import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SupabaseService } from '../services/supabase.service';

/**
 * Auth Guard protects private routes. If no session exists, the user is 
 * redirected to the login page, remembering the URL they attempted to visit 
 * as the 'returnUrl' query parameter.
 */
export const authGuard: CanActivateFn = async (route, state) => {
  const router = inject(Router);
  const supabaseService = inject(SupabaseService);

  const { data } = await supabaseService.client.auth.getSession();

  // If a session exists, allow access to the protected route
  if (data.session) return true;

  // Otherwise, redirect to login, preserving the intended destination
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url }
  });
};
