import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { SupabaseService } from '../../core/services/supabase.service';
import { ToastService } from '../../core/services/toast.service';
import { VendorsApiService, VendorProfileResponse } from '@alexandercanon/api-client-angular';

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
  private readonly vendorsApi = inject(VendorsApiService);
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
    const user = this.authService.currentUser();
    const role = this.authService.userRole();
    
    this.userRole.set(role === 'super_admin' ? 'Super Administrador' : 'Vendedor / Distribuidor');
    
    if (user) {
      this.userEmail.set(user.email || '');
    }

    if (role === 'vendor') {
      this.vendorsApi.meVendor().subscribe({
        next: (profile: VendorProfileResponse) => {
          if (profile.storeName) {
            this.storeName.set(profile.storeName);
          }
        },
        error: (err: unknown) => {
          console.error('Error loading vendor profile in settings:', err);
        }
      });
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
  selectSection(section: 'profile' | 'security') {
    this.activeSection.set(section);
  }

  setSection(section: 'profile' | 'security') {
    this.activeSection.set(section);
  }

  // Update password method
  onUpdatePassword() {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const newPassword = this.passwordForm.get('newPassword')?.value;

    this.supabaseService.client.auth.updateUser({ password: newPassword })
      .then(({ error }) => {
        this.isSubmitting.set(false);
        if (error) {
          this.toastService.error(error.message || 'Error al actualizar contraseña');
        } else {
          this.toastService.success('Contraseña actualizada correctamente');
          this.passwordForm.reset();
        }
      })
      .catch((err: unknown) => {
        this.isSubmitting.set(false);
        const message = err instanceof Error ? err.message : 'Error inesperado';
        this.toastService.error(message);
      });
  }
}
