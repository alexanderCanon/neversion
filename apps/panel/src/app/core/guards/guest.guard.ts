import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SupabaseService } from '../services/supabase.service';

/**
 * GuestGuard prevents authenticated users from accessing guest-only routes like /login.
 */
export const guestGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const supabaseService = inject(SupabaseService);

  const { data } = await supabaseService.client.auth.getSession();

  if (!data.session) {
    return true;
  }

  return router.createUrlTree(['/']);
};
