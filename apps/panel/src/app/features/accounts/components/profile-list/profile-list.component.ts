import { Component, Input, Output, EventEmitter, ElementRef, ViewChild, inject, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileRequest, ProfileResponse, ProfileStatus, ClientResponse } from '@neversion/models';
import { copyToClipboard } from '@neversion/utils';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { forkJoin, of, switchMap, catchError } from 'rxjs';
import { ProfileService } from '../../services/profile.service';
import { ClientsService } from '../../../clients/services/clients.service';
import { ToastService } from '../../../../core/services/toast.service';
import { AccountsApiService, CreateManualSubscriptionRequest } from '@neversion/api-client';
import { Router } from '@angular/router';

import { SubscriptionsService } from '../../../subscriptions/services/subscriptions.service';


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
export class ProfileListComponent implements OnInit, OnChanges {
  @ViewChild('editProfileModal') editProfileModal!: ElementRef<HTMLElement>;
  @ViewChild('datePickerModal') datePickerModal!: ElementRef<HTMLElement>;
  @ViewChild('assignModal') assignModal!: ElementRef<HTMLElement>;

  @Input() accountId!: string;
  @Input() accountEmail = '';
  @Input() accountPassword = '';
  @Input() accountRenewalDate = '';
  @Input() serviceUuid = '';
  @Input() profiles: ProfileResponse[] = [];
  @Input() canGenerateProfiles = true;
  @Input() maxProfiles = 0;
  @Input() currentProfileCount = 0;
  @Input() isSpotify = false;
  @Input() serviceName = '';
  @Output() profilesChanged = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly clientsService = inject(ClientsService);
  private readonly subscriptionsService = inject(SubscriptionsService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);
  private readonly accountsApi = inject(AccountsApiService);

  profileClients: Record<string, { id: string; name: string; phone?: string }> = {};


  readonly profileForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    pin: ['', [Validators.maxLength(20)]],
    notes: [''],
    isOwner: [false]
  });

  readonly assignForm: FormGroup = this.fb.group({
    clientId: ['', Validators.required],
    clientSearch: ['', Validators.required],
    priceSold: [0, [Validators.required, Validators.min(0)]],
    discountApplied: [0, [Validators.min(0)]],
    startDate: ['', Validators.required],
    paymentDueDate: ['', Validators.required],
    sendNotification: [true],
    notes: ['']
  });

  clients: ClientResponse[] = [];
  filteredClients: ClientResponse[] = [];
  showClientDropdown = false;
  selectedProfileForAssign: ProfileResponse | null = null;
  isAssigning = false;

  selectedProfileId: string | null = null;
  selectedProfileForAccess: ProfileResponse | null = null;
  selectedRenewalDate = '';
  isSubmitting = false;
  isLoading = false;
  isCleaning = false;
  generateCount = 1;

  onCleanProfiles(): void {
    if (this.profiles.length === 0) return;

    if (!confirm('¿Deseas limpiar todos los perfiles de esta cuenta?\n\n- Se restablecerán los nombres a "Perfil 1, Perfil 2...".\n- Se eliminarán todos los PINs y notas.\n- Se cancelarán las suscripciones activas vinculadas para liberar a los clientes.')) {
      return;
    }

    this.isCleaning = true;

    // 1. Fetch current subscriptions to find any active/suspended subscriptions on these profiles
    this.subscriptionsService.getSubscriptions().subscribe({
      next: (allSubs) => {
        const profileIds = new Set(this.profiles.map(p => p.id));
        const activeSubsToCancel = allSubs.filter(
          s => s.profileId && profileIds.has(s.profileId) && s.status !== 'CANCELLED'
        );

        const cancelObservables = activeSubsToCancel.map(s => 
          this.subscriptionsService.cancelSubscription(s.id!).pipe(catchError(() => of(null)))
        );

        const executeProfileResets = () => {
          const updateObservables = this.profiles.map((p, index) => {
            const defaultName = `Perfil ${index + 1}`;
            const updateReq: ProfileRequest = {
              accountId: this.accountId,
              name: defaultName,
              pin: '',
              notes: '',
              isOwner: p.isOwner
            };
            return this.profileService.updateProfile(p.id, updateReq).pipe(
              switchMap(() => this.profileService.changeStatus(p.id, { status: ProfileStatus.AVAILABLE })),
              catchError(() => of(null))
            );
          });

          forkJoin(updateObservables).subscribe({
            next: () => {
              this.isCleaning = false;
              this.toastService.success('Perfiles limpiados y clientes desvinculados exitosamente.');
              this.profilesChanged.emit();
              this.loadProfilesClientData();
            },
            error: (err) => {
              this.isCleaning = false;
              console.error('Error cleaning profiles:', err);
              this.toastService.error('Ocurrió un error al limpiar los perfiles.');
              this.profilesChanged.emit();
            }
          });
        };

        if (cancelObservables.length > 0) {
          forkJoin(cancelObservables).subscribe({
            next: () => executeProfileResets(),
            error: () => executeProfileResets()
          });
        } else {
          executeProfileResets();
        }
      },
      error: (err) => {
        this.isCleaning = false;
        console.error('Error fetching subscriptions for cleanup:', err);
        this.toastService.error('Error al consultar suscripciones para limpieza.');
      }
    });
  }

  ngOnInit(): void {
    this.assignForm.get('clientSearch')?.valueChanges.subscribe(value => {
      if (typeof value === 'string') {
        this.filterClients(value);
      }
    });
    this.loadProfilesClientData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['accountId'] && !changes['accountId'].isFirstChange()) {
      this.loadProfilesClientData();
    }
  }

  loadProfilesClientData(): void {
    if (!this.accountId) return;

    this.accountsApi.getDetailAccount(this.accountId).subscribe({
      next: () => {
        const clientsMap: Record<string, { id: string; name: string; phone?: string }> = {};
        this.profileClients = clientsMap;
      },
      error: (err: unknown) => {
        console.error('Error fetching account details:', err);
      }
    });
  }


  viewClient(clientId: string): void {
    if (clientId) {
      this.router.navigate(['/clients', clientId]);
    }
  }

  getClientId(profileId: string): string {
    return this.profileClients[profileId]?.id || '';
  }

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
              this.loadProfilesClientData();
          }
      });
  }

  copyAccess(profile: ProfileResponse): void {
    this.selectedProfileForAccess = profile;
    if (this.accountRenewalDate) {
      this.selectedRenewalDate = this.accountRenewalDate.split('T')[0];
    } else {
      const today = new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, '0');
      const day = String(today.getDate()).padStart(2, '0');
      this.selectedRenewalDate = `${year}-${month}-${day}`;
    }
    this.openDatePickerModal();
  }

  confirmCopyAccess(): void {
    if (!this.selectedProfileForAccess) return;

    const profile = this.selectedProfileForAccess;
    const access = [
      `📺 ${this.serviceName || 'Servicio'} 📺`,
      `📧 ${this.accountEmail || ''}`,
      `🔑 ${this.accountPassword || ''}`,
      `👤 ${profile.name || 'Perfil'}`,
      `🔒 ${profile.pin || ''}`,
      `📆 ${this.formatDate(this.selectedRenewalDate)}`,
      '⚠️ Dudas e inconvenientes, reportar inmediatamente, de lo contrario no será valido el soporte.',
    ].join('\n');

    copyToClipboard(access)
      .then(() => {
        this.toastService.success('Accesos copiados');
        this.closeDatePickerModal();
      })
      .catch(() => this.toastService.error('No se pudieron copiar los accesos'));
  }

  openDatePickerModal(): void {
    const modalEl = this.datePickerModal?.nativeElement;
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

  closeDatePickerModal(): void {
    const modalEl = this.datePickerModal?.nativeElement;
    const resetState = () => {
      this.selectedProfileForAccess = null;
      this.selectedRenewalDate = '';
    };

    if (modalEl) {
      if (typeof bootstrap !== 'undefined') {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) {
          modalEl.addEventListener('hidden.bs.modal', () => {
            resetState();
          }, { once: true });
          modal.hide();
          return;
        }
      } else {
        modalEl.classList.remove('show');
        modalEl.style.display = 'none';
        document.body.classList.remove('modal-open');
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) backdrop.remove();
      }
    }
    resetState();
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
    const resetFormState = () => {
      this.selectedProfileId = null;
      this.profileForm.reset();
    };

    if (modalEl) {
      if (typeof bootstrap !== 'undefined') {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) {
          modalEl.addEventListener('hidden.bs.modal', () => {
            resetFormState();
          }, { once: true });
          modal.hide();
          return;
        }
      } else {
        modalEl.classList.remove('show');
        modalEl.style.display = 'none';
        document.body.classList.remove('modal-open');
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) backdrop.remove();
      }
    }
    resetFormState();
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

    let dateStr = value;
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
      dateStr = value.replace(/-/g, '/');
    }

    const date = new Date(dateStr);
    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat('es-GT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date);
  }


  // --- Quick Assignment ---

  openAssignModal(profile: ProfileResponse): void {
    if (profile.status !== 'AVAILABLE') {
      this.toastService.error('Solo se pueden asignar perfiles disponibles.');
      return;
    }
    this.selectedProfileForAssign = profile;
    const defaultDueDate = this.accountRenewalDate ? this.accountRenewalDate.split('T')[0] : '';
    const todayStr = new Date().toISOString().split('T')[0];
    this.assignForm.reset({
      clientId: '',
      clientSearch: '',
      priceSold: 0,
      discountApplied: 0,
      startDate: todayStr,
      paymentDueDate: defaultDueDate,
      sendNotification: true,
      notes: ''
    });
    this.loadClients();
    this.showModal(this.assignModal);
  }

  closeAssignModal(): void {
    this.hideModal(this.assignModal, () => {
      this.selectedProfileForAssign = null;
      this.assignForm.reset({
        sendNotification: true,
        priceSold: 0,
        discountApplied: 0
      });
      this.isAssigning = false;
    });
  }

  private loadClients(): void {
    this.clientsService.getClients().subscribe({
      next: (clients) => {
        this.clients = clients;
        this.filteredClients = clients;
      }
    });
  }

  filterClients(searchTerm: string): void {
    if (!searchTerm) {
      this.filteredClients = this.clients;
      this.showClientDropdown = false;
      return;
    }
    this.showClientDropdown = true;
    const lowerTerm = searchTerm.toLowerCase();
    this.filteredClients = this.clients.filter(c =>
      c.name.toLowerCase().includes(lowerTerm) ||
      (c.phone && c.phone.includes(lowerTerm)) ||
      (c.email && c.email.toLowerCase().includes(lowerTerm))
    );
  }

  selectClient(client: ClientResponse): void {
    this.assignForm.patchValue({
      clientId: client.id,
      clientSearch: `${client.name} (${client.phone || client.email})`
    });
    this.showClientDropdown = false;
  }

  hideClientDropdown(): void {
    setTimeout(() => {
      this.showClientDropdown = false;
    }, 200);
  }

  showClientDropdownList(): void {
    if (this.clients.length > 0) {
      this.filterClients(this.assignForm.get('clientSearch')?.value || '');
      this.showClientDropdown = true;
    }
  }

  submitAssignment(): void {
    if (this.assignForm.invalid || !this.selectedProfileForAssign) {
      Object.keys(this.assignForm.controls).forEach((key) => {
        this.assignForm.get(key)?.markAsTouched();
      });
      return;
    }

    const formValue = this.assignForm.value;
    const request: CreateManualSubscriptionRequest = {
      clientId: formValue.clientId,
      profileId: this.selectedProfileForAssign.id,
      serviceId: this.serviceUuid,
      priceSold: formValue.priceSold,
      discountApplied: formValue.discountApplied || 0,
      startDate: formValue.startDate,
      paymentDueDate: formValue.paymentDueDate,
      sendNotification: formValue.sendNotification,
      notes: formValue.notes || undefined
    };

    this.isAssigning = true;
    this.subscriptionsService.createManualSubscription(request).subscribe({
      next: () => {
        this.toastService.success('Asignación y suscripción rápida creadas con éxito.');
        this.profilesChanged.emit();
        this.closeAssignModal();
        this.loadProfilesClientData();
      },
      error: () => {
        this.isAssigning = false;
        this.toastService.error('Error al crear la asignación y suscripción.');
      }
    });
  }

  get minDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  get selectedClientHasEmail(): boolean {
    const clientId = this.assignForm.get('clientId')?.value;
    if (!clientId) return true;
    const client = this.clients.find(c => c.id === clientId);
    return !!client?.email;
  }

  private showModal(modalRef: ElementRef<HTMLElement>): void {
    const modalEl = modalRef?.nativeElement;
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

  private hideModal(modalRef: ElementRef<HTMLElement>, resetFn: () => void): void {
    const modalEl = modalRef?.nativeElement;
    if (modalEl) {
      if (typeof bootstrap !== 'undefined') {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) {
          modalEl.addEventListener('hidden.bs.modal', () => {
            resetFn();
          }, { once: true });
          modal.hide();
          return;
        }
      } else {
        modalEl.classList.remove('show');
        modalEl.style.display = 'none';
        document.body.classList.remove('modal-open');
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) backdrop.remove();
      }
    }
    resetFn();
  }


}
