import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { SupabaseService } from './supabase.service';
import { User, Session, AuthResponse } from '@supabase/supabase-js';
import { Observable, from, of } from 'rxjs';
import { map, tap, catchError, finalize } from 'rxjs/operators';

export interface RegisterProfile {
    email: string;
    password: string;
    // metadata in auth.users (raw_user_meta_data)
    name: string;
    lastname: string;
    phone: string;
}

export interface LoginProfile {
    email: string;
    password: string;
}

export interface AuthResult {
    success: boolean;
    user: User | null;
    session: Session | null;
    error: string | null;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly supabaseService = inject(SupabaseService);
    private readonly router = inject(Router);

    // ── Reactive State (Signals) ──────────────────────────────────
    private readonly _currentUser = signal<User | null>(null);
    private readonly _currentSession = signal<Session | null>(null);
    private readonly _isLoading = signal<boolean>(false);
    private readonly _errorMessage = signal<string | null>(null);

    // ── Public Read-only Signals ──────────────────────────────────
    readonly currentUser = this._currentUser.asReadonly();
    readonly currentSession = this._currentSession.asReadonly();
    readonly isLoading = this._isLoading.asReadonly();
    readonly errorMessage = this._errorMessage.asReadonly();
    readonly isAuthenticated = computed(() => this._currentUser() !== null);

    constructor() {
        this.listenToAuthChanges();
        this.restoreSession();
    }

    // ── Initialization ────────────────────────────────────────────

    /**
     * Public method to block app initialization until the existing session (if any)
     * is fully restored from Supabase. Used by APP_INITIALIZER.
     */
    async initialize(): Promise<void> {
        await this.restoreSession();
    }

    // ── Sign In ───────────────────────────────────────────────────
    signIn(email: string, password: string): Observable<AuthResult> {
        this._isLoading.set(true);
        this._errorMessage.set(null);

        const promise = this.supabaseService.client.auth.signInWithPassword({
            email,
            password,
        });

        return from(promise).pipe(
            map((response: AuthResponse) => this.handleAuthResponse(response)),
            catchError((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Error inesperado al iniciar sesión';
                this._errorMessage.set(message);
                return of<AuthResult>({
                    success: false,
                    user: null,
                    session: null,
                    error: message,
                });
            }),
            finalize(() => this._isLoading.set(false)),
        );
    }

    // ── Sign Up (stub for future use) ────────────────────────────
    signUp(profile: RegisterProfile): Observable<AuthResult> {
        this._isLoading.set(true);
        this._errorMessage.set(null);

        const promise = this.supabaseService.client.auth.signUp({
            email: profile.email,
            password: profile.password,
            options: {
                data: {
                    name: profile.name,
                    lastname: profile.lastname,
                    phone: profile.phone,
                },
            },
        });

        return from(promise).pipe(
            map((response: AuthResponse) => this.handleAuthResponse(response)),
            catchError((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Error inesperado al registrarse';
                this._errorMessage.set(message);
                return of<AuthResult>({
                    success: false,
                    user: null,
                    session: null,
                    error: message,
                });
            }),
            finalize(() => this._isLoading.set(false)),
        );
    }

    // ── Sign Out ──────────────────────────────────────────────────
    signOut(): Observable<{ success: boolean; error: string | null }> {
        this._isLoading.set(true);
        this._errorMessage.set(null);

        const promise = this.supabaseService.client.auth.signOut();

        return from(promise).pipe(
            map(({ error }) => {
                if (error) {
                    this._errorMessage.set(error.message);
                    return { success: false, error: error.message };
                }
                this._currentUser.set(null);
                this._currentSession.set(null);
                return { success: true, error: null };
            }),
            catchError((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Error inesperado al cerrar sesión';
                this._errorMessage.set(message);
                return of({ success: false, error: message });
            }),
            finalize(() => this._isLoading.set(false)),
        );
    }

    // ── Get Current User ─────────────────────────────────────────
    getCurrentUser(): Observable<User | null> {
        return from(this.supabaseService.client.auth.getUser()).pipe(
            map(({ data }) => data.user),
            tap((user) => this._currentUser.set(user)),
            catchError(() => of(null)),
        );
    }

    // ── Reset Password ───────────────────────────────────────────
    resetPassword(email: string): Observable<{ success: boolean; error: string | null }> {
        this._isLoading.set(true);
        this._errorMessage.set(null);

        const promise = this.supabaseService.client.auth.resetPasswordForEmail(email);

        return from(promise).pipe(
            map(({ error }) => {
                if (error) {
                    this._errorMessage.set(error.message);
                    return { success: false, error: error.message };
                }
                return { success: true, error: null };
            }),
            catchError((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Error inesperado al enviar el correo';
                this._errorMessage.set(message);
                return of({ success: false, error: message });
            }),
            finalize(() => this._isLoading.set(false)),
        );
    }

    // ── Clear Error ───────────────────────────────────────────────
    clearError(): void {
        this._errorMessage.set(null);
    }

    // ── Private Helpers ───────────────────────────────────────────

    /**
     * Processes the Supabase AuthResponse, updates local state,
     * and returns a normalised AuthResult.
     */
    private handleAuthResponse(response: AuthResponse): AuthResult {
        const { data, error } = response;

        if (error) {
            this._errorMessage.set(error.message);
            return {
                success: false,
                user: null,
                session: null,
                error: error.message,
            };
        }

        this._currentUser.set(data.user);
        this._currentSession.set(data.session);
        return {
            success: true,
            user: data.user,
            session: data.session,
            error: null,
        };
    }

    /**
     * Restores the existing session (if any) when the service initialises.
     */
    private async restoreSession(): Promise<void> {
        try {
            const { data: { session }, error } = await this.supabaseService.client.auth.getSession();

            if (error) {
                console.error('Error restoring session:', error.message);
                return;
            }

            if (session) {
                this._currentUser.set(session.user);
                this._currentSession.set(session);
            }
        } catch (err) {
            console.error('Unexpected error restoring session:', err);
        }
    }

    /**
     * Subscribes to Supabase auth state changes (login, logout, token refresh)
     * and keeps the local signals in sync.
     */
    private listenToAuthChanges(): void {
        this.supabaseService.client.auth.onAuthStateChange((event, session) => {
            this._currentUser.set(session?.user ?? null);
            this._currentSession.set(session ?? null);

            // If the user logs out from another tab, redirect this tab to login page
            if (event === 'SIGNED_OUT') {
                this.router.navigate(['/login'], { replaceUrl: true });
            }
        });
    }
}
