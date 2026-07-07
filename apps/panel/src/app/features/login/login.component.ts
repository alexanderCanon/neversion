import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LoginHeaderComponent } from './components/login-header/login-header.component';
import { LoginFormComponent } from './components/login-form/login-form.component';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, LoginHeaderComponent, LoginFormComponent],
    templateUrl: './login.component.html',
    styleUrl: './login.component.scss'
})
export class LoginComponent {
    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);

    // Flow steps: email validation, password input, or partner info redirection
    step = signal<'email' | 'password' | 'no-account'>('email');
    selectedEmail = signal<string>('');

    // Local signal to display mapped error messages
    errorMessage = signal<string | null>(null);

    onEmailSubmit(email: string): void {
        this.errorMessage.set(null);

        this.authService.checkEmailExists(email).subscribe({
            next: (exists) => {
                this.selectedEmail.set(email);
                if (exists) {
                    this.step.set('password');
                } else {
                    this.step.set('no-account');
                }
            },
            error: (err: unknown) => {
                const rawError = err instanceof Error ? err.message : 'Error inesperado';
                this.errorMessage.set(this.mapAuthError(rawError));
            }
        });
    }

    onLoginSubmit(password: string): void {
        const email = this.selectedEmail();
        this.errorMessage.set(null);

        this.authService.signIn(email, password).subscribe({
            next: (result) => {
                if (result.success && result.user) {
                    const role = result.user.role;
                    
                    if (role === 'client') {
                        // User story US-014: If a client tries to log in here, redirect to store or deny.
                        this.authService.signOut().subscribe();
                        this.errorMessage.set('Este panel es exclusivo para administradores y vendedores.');
                        return;
                    }

                    // Success for super_admin or vendedor
                    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/dashboard';
                    this.router.navigate([returnUrl], { replaceUrl: true });
                } else {
                    this.errorMessage.set(this.mapAuthError(result.error!));
                }
            },
            error: () => {
                this.errorMessage.set('Ocurrió un error inesperado al iniciar sesión.');
            }
        });
    }

    onGoBack(): void {
        this.step.set('email');
        this.errorMessage.set(null);
    }

    get isLoading() {
        return this.authService.isLoading();
    }

    private mapAuthError(raw: string): string {
        if (raw.includes('Invalid login credentials')) return 'Correo o contraseña incorrectos';
        if (raw.includes('Email not confirmed'))        return 'Confirma tu correo antes de ingresar';
        if (raw.includes('Too many requests'))          return 'Demasiados intentos. Espera unos minutos';
        if (raw.includes('User not found'))             return 'No existe una cuenta con ese correo';
        return 'Error de conexión. Intenta de nuevo';
    }
}
