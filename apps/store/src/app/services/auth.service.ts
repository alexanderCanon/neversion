import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, from, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { SupabaseService } from './supabase.service';
import { AuthResponse } from '@supabase/supabase-js';
import { User, AuthResult, UserRole } from '@neversion/models';
import { AuthApiService, RegisterClientRequest } from '@neversion/api-client';
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

  public isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null;
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

    return this.authApiService.registerClient(apiRequest).pipe(
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

  logout(): Observable<void> {
    return from(this.supabaseService.client.auth.signOut()).pipe(
      tap(() => this.currentUserSubject.next(null)),
      map(() => void 0),
      catchError(() => {
        // Even if signOut fails remotely, clear local session
        this.currentUserSubject.next(null);
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

  private handleError(err: any): Observable<AuthResult> {
    const message = err.message || 'Error de autenticación';
    return of({ success: false, user: null, error: message });
  }

  private async restoreSession() {
    try {
      const { data: { session } } = await this.supabaseService.client.auth.getSession();
      if (session) {
        this.handleAuthResponse({ data: session, error: null } as any);
      }
    } finally {
      this.isRestoringSubject.next(false);
    }
  }

  private listenToAuthChanges() {
    this.supabaseService.client.auth.onAuthStateChange((event: any, session: any) => {
      if (session) {
        this.handleAuthResponse({ data: session, error: null } as any);
      } else {
        this.currentUserSubject.next(null);
      }
    });
  }
}
