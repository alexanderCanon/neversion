import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ErrorLoggerService } from '../services/error-logger.service';
import { ToastService } from '../services/toast.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const errorLogger = inject(ErrorLoggerService);
  const toastService = inject(ToastService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        const backendMessage = error.error?.message;

        errorLogger.log(error, {
          url: req.url,
          method: req.method,
        });

        switch (error.status) {
          case 400:
            toastService.warning(
              backendMessage || 'Bad request. Please check your input.'
            );
            break;

          case 401:
            toastService.warning('Session expired. Please log in again.');
            clearSupabaseSession();
            router.navigate(['/login'], { replaceUrl: true });
            break;

          case 403:
            toastService.error('Access Denied. Admin role required.');
            break;

          case 404:
            toastService.warning(
              backendMessage || 'Resource not found.'
            );
            break;

          case 409:
            toastService.warning(
              backendMessage || 'Conflict: The operation could not be completed.'
            );
            break;

          case 500:
            toastService.error(
              'A critical server error occurred. Please try again later.'
            );
            break;

          case 0:
            toastService.error(
              'Unable to connect to server. Please check your internet connection.'
            );
            break;

          case 502:
          case 503:
          case 504:
            toastService.error(
              'Server is temporarily unavailable. Please try again later.'
            );
            break;

          default:
            toastService.error(
              backendMessage || `Error: ${error.status} ${error.statusText}`
            );
        }
      } else {
        // Non-HTTP errors (e.g., client-side or network)
        const message =
          error instanceof Error ? error.message : 'An unexpected error occurred';
        toastService.error(message);
        errorLogger.log(error, {
          url: req.url,
          method: req.method,
        });
      }

      return throwError(() => error);
    })
  );
};

/**
 * Clears the Supabase auth session from localStorage
 * so the auth guard redirects to login on the next navigation.
 */
function clearSupabaseSession(): void {
  try {
    const authKey = Object.keys(localStorage).find(
      (key) => key.startsWith('sb-') && key.endsWith('-auth-token')
    );
    if (authKey) {
      localStorage.removeItem(authKey);
    }
  } catch {
    // Silently ignore — guard will handle redirect anyway
  }
}
