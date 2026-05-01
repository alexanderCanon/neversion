import { HttpInterceptorFn } from '@angular/common/http';
import { runtimeConfig } from '../config/runtime-config';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // We only want to attach the token to our own API
  if (!req.url.startsWith(runtimeConfig.apiUrl)) {
    return next(req);
  }

  // Retrieve token synchronously to avoid Supabase auth session locking issues on every HTTP request
  let token = null;

  try {
      // Supabase stores the session under a key like 'sb-<project-id>-auth-token'
      const authKey = Object.keys(localStorage).find(key => key.startsWith('sb-') && key.endsWith('-auth-token'));
      if (authKey) {
          const storedSession = localStorage.getItem(authKey);
          if (storedSession) {
              const sessionObj = JSON.parse(storedSession);
              token = sessionObj.access_token;
          }
      }
  } catch (error) {
      console.warn('Could not parse Supabase session from localStorage:', error);
  }
  
  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedRequest);
  }
  
  return next(req);
};
