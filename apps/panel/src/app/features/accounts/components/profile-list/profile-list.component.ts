import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileRequest, ProfileResponse } from '../../models/profile.model';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileService } from '../../services/profile.service';
import { ToastService } from '../../../../core/services/toast.service';

interface BootstrapModal {
  show(): void;
  hide(): void;
}

interface Bootstrap {
  Modal: {
    new (el: HTMLElement): BootstrapModal;
    getInstance(el: HTMLElement): BootstrapModal | null;
  };
}

declare const bootstrap: Bootstrap;

@Component({
  selector: 'app-profile-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-list.component.html',
  styleUrls: [],
})
export class ProfileListComponent {
  @Input() accountId!: string;
  @Input() profiles: ProfileResponse[] = [];

  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly toastService = inject(ToastService);

  readonly profileForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    pin: ['', [Validators.maxLength(10)]],
    isOwner: [false]
  });

  selectedProfileId: string | null = null;
  isSubmitting = false;
  isLoading = false;

  getStatusClass(profile: ProfileResponse): string {
    return profile.isOwner ? 'bg-primary' : 'bg-warning text-dark';
  }

  getStatusLabel(profile: ProfileResponse): string {
    return profile.isOwner ? 'Dueño' : 'Ocupado';
  }

  openEditModal(profile: ProfileResponse): void {
    this.selectedProfileId = profile.id;
    this.profileForm.patchValue({
      name: profile.name,
      pin: profile.pin,
      isOwner: profile.isOwner
    });

    const modalEl = document.getElementById('editProfileModal');
    if (modalEl) {
      if (typeof bootstrap !== 'undefined') {
        new bootstrap.Modal(modalEl).show();
      }
    }
  }

  closeEditModal(): void {
    const modalEl = document.getElementById('editProfileModal');
    if (modalEl) {
      if (typeof bootstrap !== 'undefined') {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
      }
    }
    this.selectedProfileId = null;
    this.profileForm.reset();
  }

  saveProfile(): void {
    if (this.profileForm.invalid || !this.selectedProfileId) return;

    this.isSubmitting = true;
    const request: ProfileRequest = {
      ...this.profileForm.value,
      accountId: this.accountId
    };

    this.profileService.updateProfile(this.selectedProfileId, request).subscribe({
      next: (updatedProfile) => {
        const index = this.profiles.findIndex(p => p.id === this.selectedProfileId);
        if (index !== -1) {
          this.profiles[index] = updatedProfile;
        }
        this.toastService.success('Perfil actualizado exitosamente');
        this.closeEditModal();
      },
      error: () => this.isSubmitting = false,
      complete: () => this.isSubmitting = false
    });
  }
}
