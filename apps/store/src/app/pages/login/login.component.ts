import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, RegisterFormData } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  isLoginMode: boolean = true;
  showPassword: boolean = false;
  loginForm!: FormGroup;
  registerForm!: FormGroup;
  
  isLoading$ = this.authService.isLoading$;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForms();
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
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

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
          this.isLoginMode = true; // Redirect to login
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
