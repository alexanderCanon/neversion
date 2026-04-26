import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, from, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { SupabaseService } from './supabase.service';
import { User as SupaUser, AuthResponse } from '@supabase/supabase-js';
import { User, AuthResult, UserRole } from '@neversion/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  public isLoading$ = this.isLoadingSubject.asObservable();

  constructor(private supabaseService: SupabaseService) {
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
    
    // For US-013, we need to send name, lastname and phone in metadata
    // and also the role 'cliente'.
    const promise = this.supabaseService.client.auth.signUp({
      email: userData.email,
      password: userData.password,
      options: {
        data: {
          name: userData.name,
          lastname: userData.lastname,
          phone: userData.phone,
          role: 'cliente' as UserRole
        }
      }
    });

    return from(promise).pipe(
      map(response => this.handleAuthResponse(response as unknown as AuthResponse)),
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
      role: (data.user?.user_metadata?.['role'] as UserRole) || 'cliente',
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
