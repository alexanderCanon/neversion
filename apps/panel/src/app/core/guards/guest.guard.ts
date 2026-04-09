import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SupabaseService } from '../services/supabase.service';

/**
 * Guest Guard prevents authenticated users from accessing public routes
 * like the login page. If an active session exists, the user is redirected
 * to the dashboard.
 */
export const guestGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const supabaseService = inject(SupabaseService);

  const { data } = await supabaseService.client.auth.getSession();

  // If no session exists, allow access to the guest route
  if (!data.session) return true;

  // replaceUrl: true avoids polluting browser history with the login page
  await router.navigate(['/dashboard'], { replaceUrl: true });
  return false;
};
