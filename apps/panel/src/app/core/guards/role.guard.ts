import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Role Guard restricts access to routes based on the user's role.
 * Expects 'allowedRoles' to be passed in the route data.
 */
export const roleGuard: CanActivateFn = (route) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const allowedRoles = route.data['allowedRoles'] as string[];
    const userRole = authService.userRole();

    if (userRole && allowedRoles.includes(userRole)) {
        return true;
    }

    const redirectPath = authService.isAuthenticated()
        ? getAuthenticatedFallback(userRole)
        : '/login';
    return router.createUrlTree([redirectPath]);
};

function getAuthenticatedFallback(userRole: string | null): string {
    return userRole === 'super_admin' ? '/vendors' : '/dashboard';
}
