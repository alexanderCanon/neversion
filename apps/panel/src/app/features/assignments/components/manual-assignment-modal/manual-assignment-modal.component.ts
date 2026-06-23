import { Component, EventEmitter, Output, ViewChild, ElementRef, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AssignmentsService } from '../../services/assignments.service';
import { AccountsService } from '../../../accounts/services/accounts.service';
import { ClientsService } from '../../../clients/services/clients.service';
import { ServicesDataService } from '../../../services/services/services-data.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ClientResponse, AccountResponse, ProfileResponse, ServiceResponse } from '@neversion/models';
import { ManualAssignmentRequest } from '@neversion/api-client';

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

@Component({
  selector: 'app-manual-assignment-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './manual-assignment-modal.component.html',
  styleUrl: './manual-assignment-modal.component.scss',
})
export class ManualAssignmentModalComponent implements OnInit {
  @ViewChild('manualModal') modalElement!: ElementRef;
  @Output() assignmentCreated = new EventEmitter<void>();

  assignmentForm!: FormGroup;
  isSubmitting = false;
  isLoadingData = false;
  isBrowser: boolean;

  clients: ClientResponse[] = [];
  filteredClients: ClientResponse[] = [];
  services: ServiceResponse[] = [];
  accounts: AccountResponse[] = [];
  profiles: ProfileResponse[] = [];

  showClientDropdown = false;

  private readonly fb = inject(FormBuilder);
  private readonly assignmentsService = inject(AssignmentsService);
  private readonly accountsService = inject(AccountsService);
  private readonly clientsService = inject(ClientsService);
  private readonly servicesDataService = inject(ServicesDataService);
  private readonly toastService = inject(ToastService);
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    const defaultDate = new Date().toISOString().split('T')[0];
    this.assignmentForm = this.fb.group({
      clientId: ['', Validators.required],
      clientSearch: ['', Validators.required],
      serviceId: ['', Validators.required],
      accountId: ['', Validators.required],
      profileId: ['', Validators.required],
      startDate: [defaultDate, Validators.required],
      endDate: ['', Validators.required]
    });

    this.assignmentForm.get('clientSearch')?.valueChanges.subscribe(value => {
      if (typeof value === 'string') {
        this.filterClients(value);
      }
    });

    this.assignmentForm.get('serviceId')?.valueChanges.subscribe(serviceId => {
      if (serviceId) {
        this.loadAccountsForService(serviceId);
      } else {
        this.accounts = [];
        this.assignmentForm.patchValue({ accountId: '', profileId: '' });
      }
    });

    this.assignmentForm.get('accountId')?.valueChanges.subscribe(accountId => {
        if (accountId) {
          this.accountsService.getAccountDetail(accountId).subscribe({
            next: (detail) => {
              this.profiles = (detail.profiles ?? []).map(profile => ({
                id: profile.id || '',
                accountId,
                name: profile.name || '',
                pin: profile.pin,
                notes: profile.notes,
                isOwner: profile.isOwner ?? false,
                status: profile.status as ProfileResponse['status'],
                createdAt: '',
              })).filter(p => p.status === 'AVAILABLE');
            },
            error: () => {
              this.profiles = [];
              this.toastService.error('No se pudieron cargar los perfiles de la cuenta.');
            },
          });
          this.assignmentForm.patchValue({ profileId: '' });
        } else {
          this.profiles = [];
          this.assignmentForm.patchValue({ profileId: '' });
        }
    });
  }

  private filterClients(searchTerm: string): void {
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
    this.assignmentForm.patchValue({
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
      this.filterClients(this.assignmentForm.get('clientSearch')?.value || '');
      this.showClientDropdown = true;
    }
  }

  private loadDropdownData(): void {
    this.isLoadingData = true;

    this.clientsService.getClients().subscribe({
      next: (clients) => {
        this.clients = clients;
        this.filteredClients = clients;
      },
    });

    this.servicesDataService.getServices({ isActive: true }).subscribe({
      next: (services) => {
          this.services = services;
          this.isLoadingData = false;
      },
      error: () => {
          this.isLoadingData = false;
      }
    });
  }

  private loadAccountsForService(serviceId: string): void {
      this.accounts = [];
      this.profiles = [];
      this.assignmentForm.patchValue({ accountId: '', profileId: '' });
      this.accountsService.getAccounts({ serviceId }).subscribe({
          next: (accounts) => {
              this.accounts = accounts.filter(a => a.status !== 'EXPIRED');
          }
      });
  }

  openModal(): void {
    if (this.isBrowser) {
      this.loadDropdownData();
      const modalEl = this.modalElement?.nativeElement;
      if (modalEl) {
        const bootstrap = (window as unknown as { bootstrap: Bootstrap }).bootstrap;
        if (bootstrap) {
          const modal = new bootstrap.Modal(modalEl);
          modal.show();
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
  }

  closeModal(): void {
    if (this.isBrowser) {
      const modalEl = this.modalElement?.nativeElement;
      if (modalEl) {
        const bootstrap = (window as unknown as { bootstrap: Bootstrap }).bootstrap;
        if (bootstrap) {
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
      this.resetForm();
    }
  }

  onSubmit(): void {
    if (this.assignmentForm.valid) {
      this.isSubmitting = true;
      const formValue = this.assignmentForm.value;

      const request: ManualAssignmentRequest = {
        clientId: formValue.clientId,
        serviceId: formValue.serviceId,
        profileId: formValue.profileId,
        startDate: formValue.startDate,
        endDate: formValue.endDate
      };

      this.assignmentsService.manualAssignment(request).subscribe({
        next: () => {
          this.toastService.success('Asignación manual completada con éxito.');
          this.assignmentCreated.emit();
          this.closeModal();
        },
        error: () => {
          this.isSubmitting = false;
          this.toastService.error('Error al crear asignación manual.');
        },
      });
    } else {
      Object.keys(this.assignmentForm.controls).forEach((key) => {
        this.assignmentForm.get(key)?.markAsTouched();
      });
    }
  }

  resetForm(): void {
    this.assignmentForm.reset({
      startDate: new Date().toISOString().split('T')[0]
    });
    this.accounts = [];
    this.profiles = [];
    this.isSubmitting = false;
  }
}
