import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileRequest, ProfileResponse } from '../../models/profile.model';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileService } from '../../services/profile.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-profile-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-list.component.html',
  styleUrls: [],
})
export class ProfileListComponent implements OnChanges {
  @Input() accountId!: string;
  @Input() profiles: ProfileResponse[] = [];

  readonly profileForm: FormGroup;
  selectedProfileId: string | null = null;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private toastService: ToastService
  ) {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      pin: ['', [Validators.maxLength(10)]],
      isOwner: [false]
    });
  }

  isLoading = false;

  ngOnChanges(changes: SimpleChanges): void {
  }

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
      const bootstrap = (window as any).bootstrap;
      if (bootstrap) {
        new bootstrap.Modal(modalEl).show();
      }
    }
  }

  closeEditModal(): void {
    const modalEl = document.getElementById('editProfileModal');
    if (modalEl) {
      const bootstrap = (window as any).bootstrap;
      if (bootstrap) {
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
