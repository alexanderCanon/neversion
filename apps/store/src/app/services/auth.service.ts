import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, from, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { SupabaseService } from './supabase.service';
import { AuthResponse, User as SupabaseUser } from '@supabase/supabase-js';
import { User, AuthResult, UserRole } from '@neversion/models';
import { AuthApiService, RegisterClientRequest } from '@alexandercanon/api-client-angular';
import { runtimeConfig } from '../config/runtime-config';

export interface RegisterFormData {
  name: string;
  lastname: string;
  email: string;
  password: string;
  phone: string;
  checkNewsletter?: boolean;
  checkCookies?: boolean;
}

/** Minimal data captured from the Supabase session for an OAuth user who
 *  has not yet completed the WhatsApp onboarding step. */
export interface PendingOAuthUser {
  supabaseUid: string;
  email: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  public isLoading$ = this.isLoadingSubject.asObservable();

  // True while the initial Supabase session check is in-flight.
  // Guards must wait for this to be false before evaluating auth state.
  private isRestoringSubject = new BehaviorSubject<boolean>(true);
  public isRestoring$ = this.isRestoringSubject.asObservable();

  // True when a Google-authenticated user has no local DB profile yet.
  private needsOnboardingSubject = new BehaviorSubject<boolean>(false);
  public needsOnboarding$ = this.needsOnboardingSubject.asObservable();

  // Holds the raw Supabase user data while waiting for the onboarding form.
  private pendingOAuthUserSubject = new BehaviorSubject<PendingOAuthUser | null>(null);
  public pendingOAuthUser$ = this.pendingOAuthUserSubject.asObservable();

  constructor(
    private supabaseService: SupabaseService,
    private authApiService: AuthApiService
  ) {
    this.restoreSession();
    this.listenToAuthChanges();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public get pendingOAuthUserValue(): PendingOAuthUser | null {
    return this.pendingOAuthUserSubject.value;
  }

  public isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null;
  }

  /** Initiates Google OAuth sign-in via Supabase. Redirects back to /login. */
  loginWithGoogle(): void {
    this.supabaseService.client.auth.signInWithOAuth({
      provider: 'google',
      options: {
        redirectTo: `${window.location.origin}/login`
      }
    });
  }

  login(email: string, password: string): Observable<AuthResult> {
    this.isLoadingSubject.next(true);
    const promise = this.supabaseService.client.auth.signInWithPassword({ email, password });

    return from(promise).pipe(
      map(response => this.handleAuthResponse(response as unknown as AuthResponse)),
      catchError(err => this.handleError(err)),
      tap(() => this.isLoadingSubject.next(false))
    );
  }

  register(userData: RegisterFormData): Observable<AuthResult> {
    this.isLoadingSubject.next(true);

    const apiRequest: RegisterClientRequest = {
      email: userData.email,
      password: userData.password,
      name: `${userData.name} ${userData.lastname}`,
      phone: userData.phone,
      vendorUuid: runtimeConfig.storeVendorUuid
    };

    return this.authApiService.registerClientAuth(apiRequest).pipe(
      map(() => {
        // Backend successfully registered the client.
        // It does not log them in. The client must sign in separately.
        return {
          success: true,
          user: null, // No user session established yet
          error: null,
        };
      }),
      catchError(err => this.handleError(err)),
      tap(() => this.isLoadingSubject.next(false))
    );
  }

  /**
   * Completes the onboarding for a Google OAuth user who has no local DB profile.
   * Calls registerClient() with the pending user's data + the phone number.
   * On success, re-checks /me to fully establish the session.
   */
  completeOnboarding(phone: string): Observable<AuthResult> {
    const pending = this.pendingOAuthUserSubject.value;
    if (!pending) {
      return of({ success: false, user: null, error: 'No hay sesión de Google pendiente.' });
    }

    this.isLoadingSubject.next(true);

    const apiRequest: RegisterClientRequest = {
      email: pending.email,
      name: pending.name,
      phone,
      vendorUuid: runtimeConfig.storeVendorUuid,
      externalId: pending.supabaseUid
    };

    return this.authApiService.registerClientAuth(apiRequest).pipe(
      // switchMap lets us wait for the async token refresh before proceeding.
      switchMap(() =>
        from(this.supabaseService.client.auth.refreshSession()).pipe(
          map(() => {
            // Token refreshed — the new JWT now carries app_metadata.role = "client".
            // Re-check /me to fully establish the local session.
            this.checkBackendUser(pending.supabaseUid, pending.email, pending.name);
            return { success: true, user: null, error: null } as AuthResult;
          })
        )
      ),
      catchError(err => {
        this.isLoadingSubject.next(false);
        return this.handleError(err);
      })
    );
  }

  logout(): Observable<void> {
    return from(this.supabaseService.client.auth.signOut()).pipe(
      tap(() => {
        this.currentUserSubject.next(null);
        this.needsOnboardingSubject.next(false);
        this.pendingOAuthUserSubject.next(null);
      }),
      map(() => void 0),
      catchError(() => {
        // Even if signOut fails remotely, clear local session
        this.currentUserSubject.next(null);
        this.needsOnboardingSubject.next(false);
        this.pendingOAuthUserSubject.next(null);
        return of(void 0);
      })
    );
  }

  private handleAuthResponse(response: AuthResponse): AuthResult {
    const { data, error } = response;
    if (error) {
      return { success: false, user: null, error: error.message };
    }

    const mappedUser: User = {
      id: data.user?.id || '',
      email: data.user?.email || '',
      // app_metadata is server-controlled — cannot be tampered by the client
      role: (data.user?.app_metadata?.['role'] as UserRole) || 'client',
      name: data.user?.user_metadata?.['name'],
      lastname: data.user?.user_metadata?.['lastname'],
      phone: data.user?.user_metadata?.['phone']
    };

    this.currentUserSubject.next(mappedUser);
    return { success: true, user: mappedUser, error: null };
  }

  private handleError(err: unknown): Observable<AuthResult> {
    const message = (err as Error).message || 'Error de autenticación';
    return of({ success: false, user: null, error: message });
  }

  /**
   * Calls GET /api/v1/auth/me to determine if the Supabase user already has
   * a local DB profile.
   *
   * - 200 → session fully established, redirect to panel.
   * - 404 → first-time Google user, show the WhatsApp onboarding form.
   */
  private checkBackendUser(uid: string, email: string, name: string): void {
    this.authApiService.meAuth().subscribe({
      next: (meResponse) => {
        // User exists in the DB — build the full local User object.
        // /me does not return name; it comes from Supabase user_metadata.
        const mappedUser: User = {
          id: uid,
          email,
          role: (meResponse.role as UserRole) || 'client',
          name,
        };
        this.currentUserSubject.next(mappedUser);
        this.needsOnboardingSubject.next(false);
        this.pendingOAuthUserSubject.next(null);
        this.isLoadingSubject.next(false);
        this.isRestoringSubject.next(false);
      },
      error: (err) => {
        // HTTP 404 means no local profile yet → trigger the onboarding form.
        if (err?.status === 404) {
          this.currentUserSubject.next(null);
          this.pendingOAuthUserSubject.next({ supabaseUid: uid, email, name });
          this.needsOnboardingSubject.next(true);
        } else {
          // Any other error (5xx, network) — clear state gracefully.
          this.currentUserSubject.next(null);
          this.needsOnboardingSubject.next(false);
          this.pendingOAuthUserSubject.next(null);
        }
        this.isLoadingSubject.next(false);
        this.isRestoringSubject.next(false);
      }
    });
  }

  private async restoreSession() {
    try {
      const { data: { session } } = await this.supabaseService.client.auth.getSession();
      if (session?.user) {
        const { id, email, user_metadata } = session.user;
        const name: string = user_metadata?.['full_name'] ?? user_metadata?.['name'] ?? email ?? '';
        this.checkBackendUser(id, email ?? '', name);
      } else {
        this.isRestoringSubject.next(false);
      }
    } catch {
      this.isRestoringSubject.next(false);
    }
  }

  private listenToAuthChanges() {
    this.supabaseService.client.auth.onAuthStateChange((event: string, session: { user: SupabaseUser } | null) => {
      // SIGNED_IN fires on every page load when a session exists — avoid
      // double-calling checkBackendUser since restoreSession() already handles it.
      // We only act on explicit NEW sign-ins (TOKEN_REFRESHED is benign, skip it).
      if (event === 'SIGNED_IN' && session?.user) {
        const { id, email, user_metadata } = session.user;
        const name: string = user_metadata?.['full_name'] ?? user_metadata?.['name'] ?? email ?? '';
        this.checkBackendUser(id, email ?? '', name);
      } else if (!session) {
        this.currentUserSubject.next(null);
        this.needsOnboardingSubject.next(false);
        this.pendingOAuthUserSubject.next(null);
      }
    });
  }
}
