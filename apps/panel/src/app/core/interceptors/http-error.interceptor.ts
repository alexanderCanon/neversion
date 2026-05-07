import {
  HttpInterceptorFn,
  HttpErrorResponse
} from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ErrorLoggerService } from '../services/error-logger.service';
import { ToastService } from '../services/toast.service';
import { clearSupabaseSession } from '../utils/supabase-session-storage';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const errorLogger = inject(ErrorLoggerService);
  const toastService = inject(ToastService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

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
              backendMessage || 'Solicitud inválida. Revisa los datos ingresados.'
            );
            break;

          case 401:
            toastService.warning('Tu sesión expiró. Inicia sesión nuevamente.');
            clearBrowserSupabaseSession(platformId);
            router.navigate(['/login'], { replaceUrl: true });
            break;

          case 403:
            toastService.error('Acceso denegado. No tienes permisos para esta acción.');
            break;

          case 404:
            toastService.warning(
              backendMessage || 'Recurso no encontrado.'
            );
            break;

          case 409:
            toastService.warning(
              backendMessage || 'Conflicto: no se pudo completar la operación.'
            );
            break;

          case 500:
            toastService.error(
              'Ocurrió un error crítico en el servidor. Intenta de nuevo más tarde.'
            );
            break;

          case 0:
            toastService.error(
              'No se pudo conectar con el servidor. Revisa tu conexión a internet.'
            );
            break;

          case 502:
          case 503:
          case 504:
            toastService.error(
              'El servidor no está disponible temporalmente. Intenta de nuevo más tarde.'
            );
            break;

          default:
            toastService.error(
              backendMessage || `Error: ${error.status} ${error.statusText}`
            );
        }
      } else {
        const message =
          error instanceof Error ? error.message : 'Ocurrió un error inesperado.';
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

function clearBrowserSupabaseSession(platformId: object): void {
  if (!isPlatformBrowser(platformId)) {
    return;
  }

  try {
    clearSupabaseSession(localStorage);
  } catch {
    // The guard will redirect on the next navigation if storage is unavailable.
  }
}
