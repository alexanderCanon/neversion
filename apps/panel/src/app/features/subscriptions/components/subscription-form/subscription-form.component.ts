import { Component, EventEmitter, Output, ViewChild, ElementRef, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateManualSubscriptionRequest } from '@neversion/api-client';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { AccountsService } from '../../../accounts/services/accounts.service';
import { ClientsService } from '../../../clients/services/clients.service';
import { ServicesDataService } from '../../../services/services/services-data.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ProfileResponse, AccountResponse, ClientResponse, ServiceResponse } from '@neversion/models';

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
  selector: 'app-subscription-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './subscription-form.component.html',
  styleUrl: './subscription-form.component.scss'
  })
export class SubscriptionFormComponent implements OnInit {
  @ViewChild('subscriptionModal') modalElement!: ElementRef;
  @Output() subscriptionCreated = new EventEmitter<void>();

  subscriptionForm!: FormGroup;
  isSubmitting = false;
  isLoadingData = false;
  isBrowser: boolean;

  services: ServiceResponse[] = [];
  accounts: AccountResponse[] = [];
  clients: ClientResponse[] = [];
  filteredClients: ClientResponse[] = [];
  profiles: ProfileResponse[] = [];
  
  showClientDropdown = false;

  private readonly fb = inject(FormBuilder);
  private readonly subscriptionsService = inject(SubscriptionsService);
  private readonly servicesService = inject(ServicesDataService);
  private readonly accountsService = inject(AccountsService);
  private readonly clientsService = inject(ClientsService);
  private readonly toastService = inject(ToastService);
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.subscriptionForm = this.fb.group({
      clientId: ['', Validators.required],
      clientSearch: ['', Validators.required],
      serviceId: ['', Validators.required],
      accountId: ['', Validators.required],
      profileId: ['', Validators.required],
      priceSold: [0, [Validators.required, Validators.min(0)]],
      discountApplied: [0, [Validators.min(0)]],
      paymentDueDate: ['', Validators.required],
      sendNotification: [true],
      notes: ['']
    });

    this.subscriptionForm.get('clientSearch')?.valueChanges.subscribe(value => {
      if (typeof value === 'string') {
        this.filterClients(value);
      }
    });

    this.subscriptionForm.get('serviceId')?.valueChanges.subscribe(serviceId => {
      if (serviceId) {
        this.loadAccountsForService(serviceId);
      } else {
        this.accounts = [];
        this.subscriptionForm.patchValue({ accountId: '', profileId: '' });
      }
    });

    this.subscriptionForm.get('clientId')?.valueChanges.subscribe(clientId => {
      const control = this.subscriptionForm.get('sendNotification');
      if (clientId) {
        const client = this.clients.find(c => c.id === clientId);
        if (client && !client.email) {
          control?.setValue(false);
          control?.disable();
        } else {
          control?.enable();
        }
      } else {
        control?.enable();
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
    this.subscriptionForm.patchValue({
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
      this.filterClients(this.subscriptionForm.get('clientSearch')?.value || '');
      this.showClientDropdown = true;
    }
  }

  private loadDropdownData(): void {
    this.isLoadingData = true;
    
    this.servicesService.getServices({ isActive: true }).subscribe({
      next: (services) => {
        this.services = services;
        this.isLoadingData = false;
      },
      error: () => this.isLoadingData = false
    });

    this.clientsService.getClients().subscribe({
      next: (clients) => {
        this.clients = clients;
        this.filteredClients = clients;
      },
    });
  }

  private loadAccountsForService(serviceId: string): void {
    this.accounts = [];
    this.profiles = [];
    this.subscriptionForm.patchValue({ accountId: '', profileId: '' });

    this.accountsService.getAccounts({ serviceId }).subscribe({
      next: (accounts) => {
        this.accounts = accounts.filter(a => a.status !== 'EXPIRED');
      }
    });
  }

  onAccountChange(accountId: string): void {
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
          })).filter(profile => profile.status === 'AVAILABLE');
        },
        error: () => {
          this.profiles = [];
          this.toastService.error('No se pudieron cargar los perfiles de la cuenta.');
        },
      });
      this.subscriptionForm.patchValue({ profileId: '' });
    } else {
      this.profiles = [];
      this.subscriptionForm.patchValue({ profileId: '' });
    }
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
    if (this.subscriptionForm.valid) {
      this.isSubmitting = true;
      const formValue = this.subscriptionForm.getRawValue();

      const request: CreateManualSubscriptionRequest = {
        clientId: formValue.clientId,
        profileId: formValue.profileId,
        serviceId: formValue.serviceId,
        priceSold: formValue.priceSold,
        discountApplied: formValue.discountApplied || 0,
        paymentDueDate: formValue.paymentDueDate,
        sendNotification: formValue.sendNotification,
        notes: formValue.notes || undefined,
      };

      this.subscriptionsService.createManualSubscription(request).subscribe({
        next: () => {
          this.toastService.success('Suscripción creada exitosamente.');
          this.subscriptionCreated.emit();
          this.closeModal();
        },
        error: () => {
          this.isSubmitting = false;
        },
      });
    } else {
      Object.keys(this.subscriptionForm.controls).forEach((key) => {
        this.subscriptionForm.get(key)?.markAsTouched();
      });
    }
  }

  resetForm(): void {
    this.subscriptionForm.reset({
      sendNotification: true,
      priceSold: 0,
      discountApplied: 0
    });
    this.profiles = [];
    this.accounts = [];
    this.isSubmitting = false;
  }

  get minDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  get selectedClientHasEmail(): boolean {
    const clientId = this.subscriptionForm.get('clientId')?.value;
    if (!clientId) return true;
    const client = this.clients.find(c => c.id === clientId);
    return !!client?.email;
  }
}
