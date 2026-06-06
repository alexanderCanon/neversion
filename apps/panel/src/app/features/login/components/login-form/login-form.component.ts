import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login-form.component.html'
})
export class LoginFormComponent {
  private readonly fb = inject(FormBuilder);

  @Input() isLoading = false;
  @Input() errorMessage: string | null = null;
  @Input() step: 'email' | 'password' | 'no-account' = 'email';
  
  private _email = '';
  @Input()
  set email(val: string) {
    this._email = val;
    if (this.emailForm) {
      this.emailForm.patchValue({ email: val }, { emitEvent: false });
    }
  }
  get email(): string {
    return this._email;
  }

  @Output() submitEmail = new EventEmitter<string>();
  @Output() submitLogin = new EventEmitter<string>();
  @Output() goBack = new EventEmitter<void>();

  emailForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  passwordForm: FormGroup = this.fb.group({
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  onSubmitEmail(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }
    this.submitEmail.emit(this.emailForm.value.email);
  }

  onSubmitPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.submitLogin.emit(this.passwordForm.value.password);
  }

  onGoBackClick(): void {
    this.passwordForm.reset();
    this.goBack.emit();
  }
}
