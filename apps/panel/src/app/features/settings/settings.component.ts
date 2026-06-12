import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { SupabaseService } from '../../core/services/supabase.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly supabaseService = inject(SupabaseService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  // Active Menu / Section
  activeSection = signal<'profile' | 'security'>('profile');

  // Auth context signals
  readonly storeName = signal<string>('Mi Tienda');
  readonly userEmail = signal<string>('');
  readonly userRole = signal<string>('Distribuidor');

  // Security Form
  passwordForm!: FormGroup;
  isSubmitting = signal<boolean>(false);

  ngOnInit(): void {
    // Load current user profile from context
    const context = this.authService.currentContext();
    const user = this.authService.currentUser();
    
    if (context) {
      this.storeName.set(context.storeName || 'Mi Tienda');
      this.userRole.set(context.role === 'super_admin' ? 'Super Administrador' : 'Vendedor / Distribuidor');
    }
    
    if (user) {
      this.userEmail.set(user.email || '');
    }

    // Initialize password form
    this.passwordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  // Custom password matching validator
  private passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  // Change active section
  selectSection(section: 'profile' | 'security'): void {
    this.activeSection.set(section);
  }

  // Update password in Supabase
  onUpdatePassword(): void {
    if (this.passwordForm.invalid) return;

    this.isSubmitting.set(true);
    const newPassword = this.passwordForm.get('newPassword')?.value;

    this.supabaseService.client.auth.updateUser({ password: newPassword })
      .then(({ data, error }) => {
        this.isSubmitting.set(false);
        if (error) {
          this.toastService.error(`Error al actualizar contraseña: ${error.message}`);
        } else {
          this.toastService.success('Contraseña actualizada correctamente');
          this.passwordForm.reset();
        }
      })
      .catch((err) => {
        this.isSubmitting.set(false);
        this.toastService.error('Ocurrió un error inesperado al actualizar la contraseña');
        console.error(err);
      });
  }
}
