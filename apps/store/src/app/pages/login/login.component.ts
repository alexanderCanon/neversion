import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService, RegisterFormData, PendingOAuthUser } from '../../services/auth.service';

@Component({
  standalone: false,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
  isLoginMode: boolean = true;
  showPassword: boolean = false;
  loginForm!: FormGroup;
  registerForm!: FormGroup;
  onboardingForm!: FormGroup;

  isLoading$ = this.authService.isLoading$;
  needsOnboarding$ = this.authService.needsOnboarding$;
  pendingOAuthUser$ = this.authService.pendingOAuthUser$;

  errorMessage: string | null = null;
  successMessage: string | null = null;

  private subs = new Subscription();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForms();

    // Redirect immediately if a full session is already active.
    this.subs.add(
      this.authService.currentUser$.subscribe(user => {
        if (user) {
          this.handleRedirect(user.role);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  private initForms(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      checkNewsletter: [false],
      checkCookies: [false, Validators.requiredTrue]
    });

    // Onboarding form: only captures the missing WhatsApp phone number.
    this.onboardingForm = this.fb.group({
      phone: ['', [Validators.required, Validators.pattern(/^[23457]\d{7}$/)]],
      checkCookies: [false, Validators.requiredTrue]
    });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  /** Returns the pending Google user snapshot (read-only helper for the template). */
  get pendingUser(): PendingOAuthUser | null {
    return this.authService.pendingOAuthUserValue;
  }

  // ── Social Login ──────────────────────────────────────────────────────────

  onSocialLogin(): void {
    this.errorMessage = null;
    this.successMessage = null;
    this.authService.loginWithGoogle();
  }

  // ── Onboarding ────────────────────────────────────────────────────────────

  onOnboardingSubmit(): void {
    if (this.onboardingForm.invalid) return;

    this.errorMessage = null;
    const { phone } = this.onboardingForm.value;

    this.authService.completeOnboarding(`+502${phone}`).subscribe({
      next: (result) => {
        if (!result.success) {
          this.errorMessage = result.error || 'Error al completar el registro.';
        }
        // On success, the currentUser$ subscription above handles the redirect.
      }
    });
  }

  onCancelOnboarding(): void {
    this.authService.logout().subscribe(() => {
      this.errorMessage = null;
      this.successMessage = null;
      this.onboardingForm.reset();
    });
  }

  // ── Standard Login / Register ─────────────────────────────────────────────

  onLoginSubmit(): void {
    if (this.loginForm.invalid) return;

    this.errorMessage = null;
    this.successMessage = null;
    const { email, password } = this.loginForm.value;

    this.authService.login(email, password).subscribe({
      next: (result) => {
        if (result.success && result.user) {
          this.handleRedirect(result.user.role);
        } else {
          this.errorMessage = result.error || 'Credenciales inválidas.';
        }
      }
    });
  }

  onRegisterSubmit(): void {
    if (this.registerForm.invalid) return;

    this.errorMessage = null;
    this.successMessage = null;
    this.authService.register(this.registerForm.value as RegisterFormData).subscribe({
      next: (result) => {
        if (result.success) {
          this.successMessage = '¡Registro exitoso! Por favor inicia sesión con tus nuevos accesos.';
          this.isLoginMode = true;
          this.registerForm.reset();
        } else {
          this.errorMessage = result.error || 'Error al registrarse.';
        }
      }
    });
  }

  private handleRedirect(role: string): void {
    if (role === 'client') {
      this.successMessage = '¡Bienvenido de nuevo!';
      setTimeout(() => {
        this.router.navigate(['/customer-panel']);
      }, 1500);
    } else {
      alert('Bienvenido. Redirigiendo al Panel de Administración...');
      window.location.href = '/panel';
    }
  }
}
