import { HttpInterceptorFn } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';
import { runtimeConfig } from '../config/runtime-config';
import { getSupabaseAccessToken } from '../utils/supabase-session-storage';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // We only want to attach the token to our own API
  if (!req.url.startsWith(runtimeConfig.apiUrl)) {
    return next(req);
  }

  const platformId = inject(PLATFORM_ID);
  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  try {
    const token = getSupabaseAccessToken(localStorage);
    if (token) {
      return next(req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      }));
    }
  } catch (error) {
    console.warn('Could not parse Supabase session from localStorage:', error);
  }

  return next(req);
};
