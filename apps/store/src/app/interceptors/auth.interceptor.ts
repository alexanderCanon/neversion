import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, from, mergeMap, catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { runtimeConfig } from '../config/runtime-config';
import { SupabaseService } from '../services/supabase.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private supabaseService: SupabaseService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const isPublicRoute = req.url.includes('/services/store/');

    if (!req.url.startsWith(runtimeConfig.apiUrl) || isPublicRoute) {
      return next.handle(req);
    }

    // getSession() auto-refreshes expired tokens — safer than reading localStorage directly.
    // Cast the Promise to a typed shape to avoid Observable<unknown> inference from from().
    const session$ = from(
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      this.supabaseService.client.auth.getSession()
        .then((r: any) => (r.data?.session?.access_token ?? null) as string | null)
    ) as Observable<string | null>;

    return session$.pipe(
      mergeMap((token: string | null) => {
        const authReq = token
          ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
          : req;
        return next.handle(authReq);
      }),
      catchError((err: HttpErrorResponse) => {
        if (err.status === 401) {
          // Token rejected by the backend — clear session and send to login
          this.supabaseService.client.auth.signOut().then(() => {
            this.router.navigate(['/login']);
          });
        }
        return throwError(() => err);
      })
    );
  }
}
