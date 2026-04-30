import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ClientsApiService, ClientResponse, UpdateClientProfileRequest } from '@neversion/api-client';

@Component({
  selector: 'app-customer-profile',
  templateUrl: './profile.component.html',
  styleUrls: []
})
export class ProfileComponent implements OnInit {
  profile: ClientResponse | null = null;
  form: FormGroup;
  isLoading = true;
  isSaving = false;
  error: string | null = null;
  successMessage: string | null = null;

  constructor(
    private clientsApi: ClientsApiService,
    private formBuilder: FormBuilder
  ) {
    this.form = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      phone: ['', [Validators.maxLength(50)]]
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.error = null;

    this.clientsApi.getMyProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.form.patchValue({
          name: profile.name ?? '',
          phone: profile.phone ?? ''
        });
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching client profile:', err);
        this.error = 'Ocurrió un error al cargar tu perfil.';
        this.isLoading = false;
      }
    });
  }

  saveProfile(): void {
    this.successMessage = null;
    this.error = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    const request: UpdateClientProfileRequest = {
      name: this.form.value.name,
      phone: this.form.value.phone
    };

    this.clientsApi.updateMyProfile(request).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.form.patchValue({
          name: profile.name ?? '',
          phone: profile.phone ?? ''
        });
        this.successMessage = 'Perfil actualizado correctamente.';
        this.isSaving = false;
      },
      error: (err) => {
        console.error('Error updating client profile:', err);
        this.error = 'No se pudo actualizar tu perfil. Intenta de nuevo.';
        this.isSaving = false;
      }
    });
  }

  isInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.invalid && (control.dirty || control.touched);
  }
}
