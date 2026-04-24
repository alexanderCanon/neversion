import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LoginHeaderComponent } from './components/login-header/login-header.component';
import { LoginFormComponent } from './components/login-form/login-form.component';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, RouterLink, LoginHeaderComponent, LoginFormComponent],
    templateUrl: './login.component.html',
    styleUrl: './login.component.scss'
})
export class LoginComponent {
    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);

    // Local signal to display mapped error messages
    errorMessage = signal<string | null>(null);

    onSubmit(credentials: {email: string, password: string}): void {
        const { email, password } = credentials;

        // Reset the previous error before submitting
        this.errorMessage.set(null);

        this.authService.signIn(email, password).subscribe({
            next: (result) => {
                if (result.success && result.user) {
                    const role = result.user.role;
                    
                    if (role === 'cliente') {
                        // User story US-014: If a client tries to log in here, redirect to store or deny.
                        // For now, we sign out and show error.
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
