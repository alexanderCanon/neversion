import { Component, EventEmitter, Output, ViewChild, ElementRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SaleMode, ClientResponse } from '@neversion/models';
import { AccountWithSubscriptionRequest } from '@alexandercanon/api-client-angular';
import { AccountsService } from '../../services/accounts.service';
import { ClientsService } from '../../../clients/services/clients.service';
import { ServicesDataService } from '../../../services/services/services-data.service';
import { ToastService } from '../../../../core/services/toast.service';

interface ServiceOption {
  id: string;
  displayName: string;
  maxProfiles: number;
}

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
  selector: 'app-account-with-subscription-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './account-with-subscription-form.component.html',
  styleUrl: './account-with-subscription-form.component.scss'
})
export class AccountWithSubscriptionFormComponent implements OnInit {
  @ViewChild('unifiedModal') modalElement!: ElementRef;
  @Output() created = new EventEmitter<void>();

  unifiedForm!: FormGroup;
  isSubmitting = false;
  isBrowser = true;

  readonly saleModes = Object.values(SaleMode);
  readonly serviceOptions = signal<ServiceOption[]>([]);
  readonly clientOptions = signal<ClientResponse[]>([]);

  private readonly fb = inject(FormBuilder);
  private readonly accountsService = inject(AccountsService);
  private readonly clientsService = inject(ClientsService);
  private readonly servicesDataService = inject(ServicesDataService);
  private readonly toastService = inject(ToastService);

  constructor() {
    this.isBrowser = true;
  }

  ngOnInit(): void {
    this.initForm();
    this.loadServices();
    this.loadClients();
  }

  private loadServices(): void {
    this.servicesDataService.getServices().subscribe({
      next: (services) => {
        this.serviceOptions.set(services.map(s => ({
          id: s.id,
          displayName: s.name,
          maxProfiles: s.maxProfiles || 1
        })));
      },
      error: (err) => console.error('Failed to load services', err)
    });
  }

  private loadClients(): void {
    this.clientsService.getClients().subscribe({
      next: (clients) => this.clientOptions.set(clients),
      error: (err) => console.error('Failed to load clients', err)
    });
  }

  private initForm(): void {
    const today = new Date().toISOString().split('T')[0];
    this.unifiedForm = this.fb.group({
      // Account fields
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(1)]],
      serviceId: [null, [Validators.required]],
      maxProfiles: [1, [Validators.required, Validators.min(1)]],
      saleMode: [SaleMode.BY_PROFILE, Validators.required],
      renewalDate: ['', [Validators.required]],
      cost: [0, [Validators.required, Validators.min(0)]],
      source: [''],
      purchasedAt: [today],
      plan: [''],
      accountNotes: [''],
      // Subscription fields
      clientUuid: [null, [Validators.required]],
      paymentDueDate: ['', [Validators.required]],
      priceSold: [0, [Validators.required, Validators.min(0)]],
      discountApplied: [0, [Validators.min(0)]],
      subscriptionNotes: [''],
      sendNotification: [false]
    });

    this.unifiedForm.get('saleMode')?.valueChanges.subscribe(mode => {
      const maxProfilesCtrl = this.unifiedForm.get('maxProfiles');
      if (mode === SaleMode.FULL_ACCOUNT) {
        maxProfilesCtrl?.setValue(1);
        maxProfilesCtrl?.disable();
      } else {
        maxProfilesCtrl?.enable();
        const serviceId = this.unifiedForm.get('serviceId')?.value;
        const service = this.serviceOptions().find(s => s.id === serviceId);
        maxProfilesCtrl?.setValue(service ? service.maxProfiles : 1);
      }
    });
  }

  onServiceChange(serviceId: string): void {
    const service = this.serviceOptions().find(s => s.id === serviceId);
    if (service && this.unifiedForm.get('saleMode')?.value === SaleMode.BY_PROFILE) {
      this.unifiedForm.patchValue({ maxProfiles: service.maxProfiles });
    }
  }

  openModal(): void {
    if (this.isBrowser) {
      this.resetForm();
      const modalEl = this.modalElement?.nativeElement;
      if (modalEl) {
        if (typeof bootstrap !== 'undefined') {
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
      this.resetForm();
    }
  }

  onSubmit(): void {
    if (this.unifiedForm.valid) {
      this.isSubmitting = true;
      const formValue = this.unifiedForm.getRawValue();

      const request: AccountWithSubscriptionRequest = {
        email: formValue.email,
        password: formValue.password,
        serviceUuid: formValue.serviceId,
        saleMode: formValue.saleMode,
        renewalDate: formValue.renewalDate,
        plan: formValue.plan || undefined,
        cost: Number(formValue.cost),
        source: formValue.source || undefined,
        purchasedAt: formValue.purchasedAt || undefined,
        accountNotes: formValue.accountNotes || undefined,
        maxProfiles: formValue.saleMode === SaleMode.FULL_ACCOUNT ? 1 : (Number(formValue.maxProfiles) || undefined),
        clientUuid: formValue.clientUuid,
        paymentDueDate: formValue.paymentDueDate,
        priceSold: Number(formValue.priceSold),
        discountApplied: Number(formValue.discountApplied) || undefined,
        subscriptionNotes: formValue.subscriptionNotes || undefined,
        sendNotification: formValue.sendNotification
      };

      this.accountsService.createAccountWithSubscription(request).subscribe({
        next: () => {
          this.toastService.success('Cuenta y suscripción creadas exitosamente.');
          this.created.emit();
          this.closeModal();
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      Object.keys(this.unifiedForm.controls).forEach((key) => {
        this.unifiedForm.get(key)?.markAsTouched();
      });
    }
  }

  resetForm(): void {
    const today = new Date().toISOString().split('T')[0];
    this.unifiedForm.reset({
      saleMode: SaleMode.BY_PROFILE,
      cost: 0,
      maxProfiles: 1,
      purchasedAt: today,
      priceSold: 0,
      discountApplied: 0,
      sendNotification: false
    });
    this.unifiedForm.get('maxProfiles')?.enable();
    this.isSubmitting = false;
  }
}
