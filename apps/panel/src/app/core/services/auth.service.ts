import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { SupabaseService } from './supabase.service';
import { User as SupaUser, Session, AuthResponse } from '@supabase/supabase-js';
import { Observable, firstValueFrom, from, of } from 'rxjs';
import { map, catchError, finalize, switchMap } from 'rxjs/operators';
import { User, AuthResult, UserRole, RegisterVendorRequest } from '@neversion/models';
import { AuthApiService, RegisterVendorRequest as ApiVendorRequest } from '@neversion/api-client';
import { runtimeConfig } from '../config/runtime-config';

interface AuthContextResponse {
    userUuid: string;
    externalId: string;
    role: UserRole;
    vendorUuid: string | null;
    storeName: string | null;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly http = inject(HttpClient);
    private readonly supabaseService = inject(SupabaseService);
    private readonly authApiService = inject(AuthApiService);
    private readonly router = inject(Router);

    // ── Reactive State (Signals) ──────────────────────────────────
    private readonly _currentUser = signal<SupaUser | null>(null);
    private readonly _currentSession = signal<Session | null>(null);
    private readonly _currentContext = signal<AuthContextResponse | null>(null);
    private readonly _isLoading = signal<boolean>(false);
    private readonly _errorMessage = signal<string | null>(null);
    private readonly _contextLoadFailed = signal<boolean>(false);

    // ── Public Read-only Signals ──────────────────────────────────
    readonly currentUser = this._currentUser.asReadonly();
    readonly currentSession = this._currentSession.asReadonly();
    readonly currentContext = this._currentContext.asReadonly();
    readonly isLoading = this._isLoading.asReadonly();
    readonly errorMessage = this._errorMessage.asReadonly();
    readonly contextLoadFailed = this._contextLoadFailed.asReadonly();
    readonly isAuthenticated = computed(() => this._currentUser() !== null);
    readonly currentVendorUuid = computed(() => this._currentContext()?.vendorUuid ?? null);
    readonly isSuperAdmin = computed(() => this.userRole() === 'super_admin');
    
    /**
     * Role resolution. 
     */
    readonly userRole = computed<UserRole | null>(() => {
        const contextRole = this._currentContext()?.role;
        if (contextRole) return contextRole;

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
        await this.loadCurrentContextIfAuthenticated();
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
                    // Supabase returns "User not found" or "invalid_credentials" error code/message.
                    // If error message contains "User not found", account does not exist.
                    if (error.message.includes('User not found')) {
                        return false;
                    }
                    // If error is "Invalid login credentials", account exists (since the dummy password failed).
                    if (error.message.includes('Invalid login credentials')) {
                        return true;
                    }
                    // If some other error happens (e.g. rate limit), throw it.
                    throw new Error(error.message);
                }
                // If it succeeds (impossible with dummy pass), it exists.
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
            switchMap((result: AuthResult) => {
                if (!result.success) {
                    return of(result);
                }

                this._contextLoadFailed.set(false);
                return from(this.loadCurrentContext()).pipe(
                    map(() => ({
                        success: true,
                        user: this.buildAuthenticatedUser(),
                        error: null,
                    })),
                    catchError((err: unknown) => {
                        const message = err instanceof Error
                            ? err.message
                            : 'No se pudo resolver el contexto del usuario autenticado';
                        this._contextLoadFailed.set(true);
                        this._errorMessage.set(message);
                        return of<AuthResult>({
                            success: false,
                            user: null,
                            error: message,
                        });
                    }),
                );
            }),
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

        return this.authApiService.registerVendor(apiRequest).pipe(
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
                this._currentContext.set(null);
                this._contextLoadFailed.set(false);
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
            } else {
                this._currentContext.set(null);
                this._contextLoadFailed.set(false);
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
                this._currentContext.set(null);
                this._contextLoadFailed.set(false);
                this.router.navigate(['/login'], { replaceUrl: true });
            }
        });
    }

    private async loadCurrentContextIfAuthenticated(): Promise<void> {
        if (!this._currentSession()) {
            this._currentContext.set(null);
            this._contextLoadFailed.set(false);
            return;
        }

        try {
            this._contextLoadFailed.set(false);
            await this.loadCurrentContext();
        } catch (err) {
            const message = err instanceof Error
                ? err.message
                : 'No se pudo conectar con el servidor';
            this._currentContext.set(null);
            this._contextLoadFailed.set(true);
            this._errorMessage.set(message);
            console.error('Error loading authenticated context:', err);
        }
    }

    async retryCurrentContext(): Promise<void> {
        await this.loadCurrentContextIfAuthenticated();
    }

    private async loadCurrentContext(): Promise<void> {
        const context = await firstValueFrom(
            this.http.get<AuthContextResponse>(`${runtimeConfig.apiUrl}/api/v1/auth/me`)
        );
        this._currentContext.set(context);
        this._contextLoadFailed.set(false);
    }

    private buildAuthenticatedUser(): User | null {
        const user = this._currentUser();
        const context = this._currentContext();

        if (!user) {
            return null;
        }

        return {
            id: context?.userUuid ?? user.id,
            email: user.email ?? '',
            role: context?.role ?? this.userRole() ?? 'client',
            name: user.user_metadata?.['name'],
            lastname: user.user_metadata?.['lastname'],
            phone: user.user_metadata?.['phone'],
        };
    }
}
