import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, from, of, switchMap } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { SupabaseService } from './supabase.service';
import { AuthResponse } from '@supabase/auth-js';
import { User, AuthResult, UserRole } from '@neversion/models';
import { AuthApiService, RegisterClientRequest } from '@neversion/api-client';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  public isLoading$ = this.isLoadingSubject.asObservable();

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

  register(userData: any): Observable<AuthResult> {
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

  logout(): void {
    this.supabaseService.client.auth.signOut().then(() => {
      this.currentUserSubject.next(null);
    });
  }

  private handleAuthResponse(response: AuthResponse): AuthResult {
    const { data, error } = response;
    if (error) {
      return { success: false, user: null, error: error.message };
    }

    const mappedUser: User = {
      id: data.user?.id || '',
      email: data.user?.email || '',
      role: (data.user?.user_metadata?.['role'] as UserRole) || 'client',
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
    const { data: { session } } = await this.supabaseService.client.auth.getSession();
    if (session) {
      this.handleAuthResponse({ data: session, error: null } as any);
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
