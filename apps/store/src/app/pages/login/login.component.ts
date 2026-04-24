import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
  isLoading: boolean = false;
  errorMessage: string | null = null;

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

    this.isLoading = true;
    this.errorMessage = null;

    const { email, password } = this.loginForm.value;
    this.authService.login(email, password).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Credenciales inválidas. Por favor intente de nuevo.';
      }
    });
  }

  onRegisterSubmit(): void {
    if (this.registerForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = null;

    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Hubo un error al registrarse. Por favor intente de nuevo.';
      }
    });
  }
}
