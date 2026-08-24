import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { SupabaseService } from './supabase.service';
import { User as SupaUser, Session, AuthResponse } from '@supabase/supabase-js';
import { Observable, from, of } from 'rxjs';
import { map, catchError, finalize } from 'rxjs/operators';
import { User, AuthResult, UserRole, RegisterVendorRequest } from '@neversion/models';
import { AuthApiService, RegisterVendorRequest as ApiVendorRequest } from '@alexandercanon/api-client-angular';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly supabaseService = inject(SupabaseService);
    private readonly authApiService = inject(AuthApiService);
    private readonly router = inject(Router);

    // ── Reactive State (Signals) ──────────────────────────────────
    private readonly _currentUser = signal<SupaUser | null>(null);
    private readonly _currentSession = signal<Session | null>(null);
    private readonly _isLoading = signal<boolean>(false);
    private readonly _errorMessage = signal<string | null>(null);

    // ── Public Read-only Signals ──────────────────────────────────
    readonly currentUser = this._currentUser.asReadonly();
    readonly currentSession = this._currentSession.asReadonly();
    readonly isLoading = this._isLoading.asReadonly();
    readonly errorMessage = this._errorMessage.asReadonly();
    readonly isAuthenticated = computed(() => this._currentUser() !== null);
    readonly isSuperAdmin = computed(() => this.userRole() === 'super_admin');
    
    /**
     * Role resolution. 
     */
    readonly userRole = computed<UserRole | null>(() => {
        const user = this._currentUser();
        if (!user) return null;
        
        const appRole = user.app_metadata?.['role'] as UserRole;
        if (appRole) return appRole;

        const metaRole = user.user_metadata?.['role'] as UserRole;
        if (metaRole) return metaRole;

        return null;
    });

    constructor() {
        // Constructor left empty to avoid illegal inject() calls or side effects during bootstrap
    }

    // ── Initialization ────────────────────────────────────────────

    /**
     * Called by APP_INITIALIZER in app.config.ts
     */
    async initialize(): Promise<void> {
        this.listenToAuthChanges();
        await this.restoreSession();
    }

    /**
     * Probes Supabase Auth using a dummy password to verify if an email exists.
     * Returns true if email exists (returns "Invalid credentials"), false if not (returns "User not found").
     */
    checkEmailExists(email: string): Observable<boolean> {
        this._isLoading.set(true);
        this._errorMessage.set(null);

        const promise = this.supabaseService.client.auth.signInWithPassword({
            email,
            password: 'check-existence-only-dummy-password-probe-123456',
        });

        return from(promise).pipe(
            map(({ error }) => {
                this._isLoading.set(false);
                if (error) {
                    if (error.message.includes('User not found')) {
                        return false;
                    }
                    if (error.message.includes('Invalid login credentials')) {
                        return true;
                    }
                    throw new Error(error.message);
                }
                return true;
            }),
            catchError((err: unknown) => {
                this._isLoading.set(false);
                const message = err instanceof Error ? err.message : 'Error al verificar el correo';
                this._errorMessage.set(message);
                throw err;
            })
        );
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
                    error: message,
                });
            }),
            finalize(() => this._isLoading.set(false)),
        );
    }

    // ── Sign Up (Vendor) ─────────────────────────────────────────
    signUpVendor(request: RegisterVendorRequest): Observable<AuthResult> {
        this._isLoading.set(true);
        this._errorMessage.set(null);

        const apiRequest: ApiVendorRequest = {
            email: request.email,
            password: request.password,
            storeName: request.storeName,
        };

        return this.authApiService.registerVendorAuth(apiRequest).pipe(
            map(() => {
                return {
                    success: true,
                    user: null,
                    error: null,
                };
            }),
            catchError((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Error inesperado al registrar vendedor';
                this._errorMessage.set(message);
                return of<AuthResult>({
                    success: false,
                    user: null,
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

    // ── Private Helpers ───────────────────────────────────────────

    private handleAuthResponse(response: AuthResponse): AuthResult {
        const { data, error } = response;

        if (error) {
            this._errorMessage.set(error.message);
            return {
                success: false,
                user: null,
                error: error.message,
            };
        }

        this._currentUser.set(data.user);
        this._currentSession.set(data.session);

        return {
            success: true,
            user: this.buildAuthenticatedUser(),
            error: null,
        };
    }

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

    private listenToAuthChanges(): void {
        this.supabaseService.client.auth.onAuthStateChange((event, session) => {
            this._currentUser.set(session?.user ?? null);
            this._currentSession.set(session ?? null);

            if (event === 'SIGNED_OUT') {
                this.router.navigate(['/login'], { replaceUrl: true });
            }
        });
    }

    private buildAuthenticatedUser(): User | null {
        const user = this._currentUser();

        if (!user) {
            return null;
        }

        return {
            id: user.id,
            email: user.email ?? '',
            role: this.userRole() ?? 'client',
            name: user.user_metadata?.['name'],
            lastname: user.user_metadata?.['lastname'],
            phone: user.user_metadata?.['phone'],
        };
    }
}
