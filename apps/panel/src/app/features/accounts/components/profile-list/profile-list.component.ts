import { Component, Input, Output, EventEmitter, ElementRef, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileRequest, ProfileResponse, ProfileStatus } from '@neversion/models';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
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
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './profile-list.component.html',
  styleUrl: './profile-list.component.scss'
  })
export class ProfileListComponent {
  @ViewChild('editProfileModal') editProfileModal!: ElementRef<HTMLElement>;

  @Input() accountId!: string;
  @Input() accountEmail = '';
  @Input() accountPassword = '';
  @Input() accountRenewalDate = '';
  @Input() profiles: ProfileResponse[] = [];
  @Input() canGenerateProfiles = true;
  @Input() maxProfiles = 0;
  @Input() currentProfileCount = 0;
  @Input() isSpotify = false;
  @Output() profilesChanged = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly toastService = inject(ToastService);

  readonly profileForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    pin: ['', [Validators.maxLength(20)]],
    notes: [''],
    isOwner: [false]
  });

  selectedProfileId: string | null = null;
  isSubmitting = false;
  isLoading = false;
  generateCount = 1;

  getStatusClass(profile: ProfileResponse): string {
    switch (profile.status) {
      case ProfileStatus.AVAILABLE: return 'bg-success-subtle text-success border border-success-subtle';
      case ProfileStatus.ACTIVE:    return 'bg-primary-subtle text-primary border border-primary-subtle';
      case ProfileStatus.RESERVED:  return 'bg-warning-subtle text-warning-emphasis border border-warning-subtle';
      case ProfileStatus.BLOCKED:   return 'bg-danger-subtle text-danger border border-danger-subtle';
      default: return 'bg-light text-secondary border';
    }
  }

  getStatusLabel(profile: ProfileResponse): string {
    const labels: Record<string, string> = {
      AVAILABLE: 'Disponible',
      ACTIVE:    'Activo',
      RESERVED:  'Reservado',
      OCCUPIED:  'Ocupado',
      BLOCKED:   'Bloqueado',
      EXPIRED:   'Expirado'
    };
    let label = labels[profile.status] || profile.status;
    if (profile.isOwner) label += ' (Dueño)';
    return label;
  }

  get isLimitReached(): boolean {
    return this.maxProfiles > 0 && this.currentProfileCount >= this.maxProfiles;
  }

  get wouldExceedLimit(): boolean {
    return this.maxProfiles > 0 && (this.currentProfileCount + this.generateCount) > this.maxProfiles;
  }

  get hasOtherOwner(): boolean {
    return this.profiles.some(p => p.isOwner && p.id !== this.selectedProfileId);
  }

  onGenerateProfiles(): void {
      if (!this.canGenerateProfiles) return;
      if (this.generateCount < 1) return;

      if (this.wouldExceedLimit) {
        this.toastService.error(
          `No se pueden generar más perfiles: se excedería el límite permitido (${this.maxProfiles}) para este servicio.`
        );
        return;
      }

      this.isSubmitting = true;
      this.profileService.generateProfiles(this.accountId, this.generateCount).subscribe({
          next: () => {
              this.toastService.success(`Se han generado ${this.generateCount} perfiles.`);
              this.profilesChanged.emit();
              this.isSubmitting = false;
          },
          error: () => this.isSubmitting = false
      });
  }

  toggleBlocked(profile: ProfileResponse): void {
      const newStatus = profile.status === ProfileStatus.BLOCKED ? ProfileStatus.AVAILABLE : ProfileStatus.BLOCKED;
      this.profileService.changeStatus(profile.id, { status: newStatus }).subscribe({
          next: () => {
              this.toastService.success('Estado del perfil actualizado.');
              this.profilesChanged.emit();
          }
      });
  }

  copyAccess(profile: ProfileResponse): void {
    const access = [
      '📺📺',
      `📧 ${this.accountEmail || ''}`,
      `🔑 ${this.accountPassword || ''}`,
      `👤 ${profile.name || 'Perfil'}`,
      `🔒 ${profile.pin || ''}`,
      `📆 ${this.formatDate(this.accountRenewalDate)}`,
      '⚠️ No hacer cambios en la cuenta, cualquier duda e inconveniente contactar con el vendedor.'
    ].join('\n');

    this.copyToClipboard(access)
      .then(() => this.toastService.success('Accesos copiados'))
      .catch(() => this.toastService.error('No se pudieron copiar los accesos'));
  }

  openEditModal(profile: ProfileResponse): void {
    this.selectedProfileId = profile.id;
    this.profileForm.patchValue({
      name: profile.name,
      pin: profile.pin,
      notes: profile.notes ?? '',
      isOwner: profile.isOwner
    });

    const modalEl = this.editProfileModal?.nativeElement;
    if (modalEl) {
      if (typeof bootstrap !== 'undefined') {
        new bootstrap.Modal(modalEl).show();
      } else {
        modalEl.classList.add('show');
        modalEl.style.display = 'block';
        document.body.classList.add('modal-open');
        const backdrop = document.createElement('div');
        backdrop.classList.add('modal-backdrop', 'fade', 'show');
        document.body.appendChild(backdrop);
      }
    }
  }

  closeEditModal(): void {
    const modalEl = this.editProfileModal?.nativeElement;
    if (modalEl) {
      if (typeof bootstrap !== 'undefined') {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
      } else {
        modalEl.classList.remove('show');
        modalEl.style.display = 'none';
        document.body.classList.remove('modal-open');
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) backdrop.remove();
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
      next: () => {
        this.toastService.success('Perfil actualizado exitosamente');
        this.closeEditModal();
        this.profilesChanged.emit();
      },
      error: () => this.isSubmitting = false,
      complete: () => this.isSubmitting = false
    });
  }

  private formatDate(value: string): string {
    if (!value) {
      return '';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat('es-GT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date);
  }

  private copyToClipboard(value: string): Promise<void> {
    if (navigator.clipboard?.writeText) {
      return navigator.clipboard.writeText(value);
    }

    const textarea = document.createElement('textarea');
    textarea.value = value;
    textarea.setAttribute('readonly', '');
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    const copied = document.execCommand('copy');
    document.body.removeChild(textarea);

    return copied ? Promise.resolve() : Promise.reject();
  }
}
