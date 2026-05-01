import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { RegisterVendorRequest } from '@neversion/models';

@Component({
  selector: 'app-vendor-registration',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="container mt-4">
      <div class="card shadow-sm">
        <div class="card-header bg-primary text-white">
          <h4 class="mb-0">Registrar Nuevo Vendedor</h4>
        </div>
        <div class="card-body">
          <form [formGroup]="vendorForm" (ngSubmit)="onSubmit()">
            <div class="row">
              <!-- Datos Personales -->
              <div class="col-md-6 mb-3">
                <label for="name" class="form-label">Nombre</label>
                <input id="name" type="text" class="form-control" formControlName="name" placeholder="Ej: Juan">
              </div>
              <div class="col-md-6 mb-3">
                <label for="lastname" class="form-label">Apellidos</label>
                <input id="lastname" type="text" class="form-control" formControlName="lastname" placeholder="Ej: Pérez">
              </div>

              <!-- Contacto -->
              <div class="col-md-6 mb-3">
                <label for="email" class="form-label">Email</label>
                <input id="email" type="email" class="form-control" formControlName="email" placeholder="vendedor@ejemplo.com">
              </div>
              <div class="col-md-6 mb-3">
                <label for="phone" class="form-label">Teléfono</label>
                <input id="phone" type="text" class="form-control" formControlName="phone" placeholder="Ej: 123456789">
              </div>
              <div class="col-md-6 mb-3">
                <label for="password" class="form-label">Contraseña</label>
                <input id="password" type="password" class="form-control" formControlName="password" placeholder="Mínimo 6 caracteres">
              </div>

              <!-- Tienda -->
              <div class="col-12 mb-4">
                <label for="storeName" class="form-label">Nombre de la Tienda / Negocio</label>
                <input id="storeName" type="text" class="form-control" formControlName="storeName" placeholder="Ej: Mi Tienda Online">
              </div>
            </div>

            @if (errorMessage()) {
              <div class="alert alert-danger">
                {{ errorMessage() }}
              </div>
            }

            <div class="d-flex justify-content-end gap-2">
              <button type="button" class="btn btn-outline-secondary" (click)="onCancel()">Cancelar</button>
              <button type="submit" class="btn btn-primary" [disabled]="vendorForm.invalid || isLoading()">
                @if (isLoading()) {
                  <span class="spinner-border spinner-border-sm me-1"></span>
                }
                Registrar Vendedor
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
  `]
})
export class VendorRegistrationComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isLoading = this.authService.isLoading;
  readonly errorMessage = this.authService.errorMessage;

  vendorForm: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    lastname: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    phone: ['', [Validators.required]],
    storeName: ['', [Validators.required, Validators.minLength(3)]]
  });

  onSubmit(): void {
    if (this.vendorForm.invalid) return;

    const request: RegisterVendorRequest = this.vendorForm.value;
    
    this.authService.signUpVendor(request).subscribe({
      next: (result) => {
        if (result.success) {
          alert('Vendedor registrado exitosamente. Se ha enviado un correo de bienvenida.');
          this.router.navigate(['/vendors']);
        }
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/vendors']);
  }
}
