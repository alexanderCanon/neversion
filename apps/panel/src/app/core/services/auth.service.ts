import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { SupabaseService } from './supabase.service';
import { User as SupaUser, Session, AuthResponse } from '@supabase/supabase-js';
import { Observable, from, of, switchMap } from 'rxjs';
import { map, catchError, finalize } from 'rxjs/operators';
import { User, AuthResult, UserRole, RegisterVendorRequest } from '@neversion/models';
import { AuthApiService, RegisterVendorRequest as ApiVendorRequest } from '@neversion/api-client';

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
    
    /**
     * Role resolution. 
     * In a real scenario, this would come from user metadata or a database query.
     */
    readonly userRole = computed<UserRole | null>(() => {
        const user = this._currentUser();
        if (!user) return null;
        
        // Priority to app_metadata (set securely by backend)
        const appRole = user.app_metadata?.['role'] as UserRole;
        if (appRole) return appRole;

        // Fallback to user_metadata (if set manually from Supabase Dashboard)
        const metaRole = user.user_metadata?.['role'] as UserRole;
        if (metaRole) return metaRole;

        // Fallback Mock logic: admin emails are super_admin, others are vendor
        return user.email?.includes('admin') ? 'super_admin' : 'vendor';
    });

    constructor() {
        this.listenToAuthChanges();
        this.restoreSession();
    }

    // ── Initialization ────────────────────────────────────────────

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
            // name, lastname and phone are not supported in backend DTO yet, 
            // but we can pass them if backend allows, but API currently only takes storeName, email, password.
        };

        return this.authApiService.registerVendor(apiRequest).pipe(
            map((response) => {
                // Backend successfully registered the vendor.
                // It does not log them in. The vendor must sign in separately.
                return {
                    success: true,
                    user: null, // No user session established yet
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

        const mappedUser: User = {
            id: data.user?.id || '',
            email: data.user?.email || '',
            role: (data.user?.user_metadata?.['role'] as UserRole) || 'vendor',
            name: data.user?.user_metadata?.['name'],
            lastname: data.user?.user_metadata?.['lastname'],
            phone: data.user?.user_metadata?.['phone']
        };

        return {
            success: true,
            user: mappedUser,
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
}
