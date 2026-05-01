import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { runtimeConfig } from '../config/runtime-config';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!req.url.startsWith(runtimeConfig.apiUrl)) {
      return next.handle(req);
    }

    const token = this.getSupabaseToken();
    if (!token) {
      return next.handle(req);
    }

    return next.handle(req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    }));
  }

  private getSupabaseToken(): string | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    try {
      const authKey = Object.keys(localStorage)
        .find((key) => key.startsWith('sb-') && key.endsWith('-auth-token'));
      if (!authKey) {
        return null;
      }

      const storedSession = localStorage.getItem(authKey);
      if (!storedSession) {
        return null;
      }

      const session = JSON.parse(storedSession) as { access_token?: string };
      return session.access_token ?? null;
    } catch (error) {
      console.warn('Could not parse Supabase session from localStorage:', error);
      return null;
    }
  }
}
