import { Component, EventEmitter, Output, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { BatchCreateManualSubscriptionRequest, BatchItem } from '@alexandercanon/api-client-angular';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { AccountsService } from '../../../accounts/services/accounts.service';
import { ClientsService } from '../../../clients/services/clients.service';
import { ServicesDataService } from '../../../services/services/services-data.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ProfileResponse, AccountResponse, ClientResponse, ServiceResponse } from '@neversion/models';

interface ServiceLineContext {
  accounts: AccountResponse[];
  profiles: ProfileResponse[];
  showOverride: boolean;
  availableCount: number;
}

@Component({
  selector: 'app-batch-subscription-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './batch-subscription-form.component.html',
  styleUrl: './batch-subscription-form.component.scss'
})
export class BatchSubscriptionFormComponent implements OnInit {
  @Output() batchCreated = new EventEmitter<void>();

  batchForm!: FormGroup;
  isSubmitting = false;
  isLoadingData = false;
  isBrowser: boolean;
  isModalOpen = false;

  services: ServiceResponse[] = [];
  clients: ClientResponse[] = [];
  filteredClients: ClientResponse[] = [];
  showClientDropdown = false;

  lineContexts: ServiceLineContext[] = [];

  batchResult: { success: number; failed: number; total: number } | null = null;

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
    this.batchForm = this.fb.group({
      clientId: ['', Validators.required],
      clientSearch: ['', Validators.required],
      items: this.fb.array([]),
      discountApplied: [0, [Validators.min(0)]],
      paymentDueDate: ['', Validators.required],
      sendNotification: [true],
      notes: ['']
    });

    this.batchForm.get('clientSearch')?.valueChanges.subscribe(value => {
      if (typeof value === 'string') {
        this.filterClients(value);
      }
    });

    this.batchForm.get('clientId')?.valueChanges.subscribe(clientId => {
      const control = this.batchForm.get('sendNotification');
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

    this.addItem();
  }

  get items(): FormArray<FormGroup> {
    return this.batchForm.get('items') as FormArray<FormGroup>;
  }

  addItem(): void {
    const line = this.fb.group({
      serviceId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      priceSold: [0, [Validators.required, Validators.min(0)]],
      useAutoAssign: [true],
      accountId: [''],
      profileId: ['']
    });

    const index = this.items.length;
    this.lineContexts.push({ accounts: [], profiles: [], showOverride: false, availableCount: 0 });

    line.get('serviceId')?.valueChanges.subscribe(serviceId => {
      this.onServiceChange(index, serviceId ?? '');
    });

    line.get('accountId')?.valueChanges.subscribe(accountId => {
      this.onAccountChange(index, accountId ?? '');
    });

    this.items.push(line);
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
    this.lineContexts.splice(index, 1);
  }

  toggleOverride(index: number): void {
    const ctx = this.lineContexts[index];
    ctx.showOverride = !ctx.showOverride;
    const line = this.items.at(index);
    if (!ctx.showOverride) {
      line.patchValue({ accountId: '', profileId: '' });
      ctx.profiles = [];
    }
  }

  private onServiceChange(index: number, serviceId: string): void {
    const ctx = this.lineContexts[index];
    ctx.accounts = [];
    ctx.profiles = [];
    ctx.availableCount = 0;

    const line = this.items.at(index);
    line.patchValue({ accountId: '', profileId: '' }, { emitEvent: false });

    if (!serviceId) return;

    this.accountsService.getAccounts({ serviceId }).subscribe({
      next: (accounts) => {
        ctx.accounts = accounts.filter(a => a.status !== 'EXPIRED');
        ctx.availableCount = ctx.accounts.reduce((sum, a) => sum + (a.availableProfiles || 0), 0);
      }
    });
  }

  private onAccountChange(index: number, accountId: string): void {
    const ctx = this.lineContexts[index];
    ctx.profiles = [];

    const line = this.items.at(index);
    line.patchValue({ profileId: '' }, { emitEvent: false });

    if (!accountId) return;

    this.accountsService.getAccountDetail(accountId).subscribe({
      next: (detail) => {
        ctx.profiles = (detail.profiles ?? []).map(profile => ({
          id: profile.id || '',
          accountId,
          name: profile.name || '',
          pin: profile.pin,
          notes: profile.notes,
          isOwner: profile.isOwner ?? false,
          status: profile.status as ProfileResponse['status'],
          createdAt: '',
        })).filter(profile => profile.status === 'AVAILABLE');
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
    this.batchForm.patchValue({
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
      this.filterClients(this.batchForm.get('clientSearch')?.value || '');
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
      }
    });
  }

  openModal(): void {
    if (this.isBrowser) {
      this.loadDropdownData();
      this.batchResult = null;
      this.isModalOpen = true;
      document.body.style.overflow = 'hidden';
    }
  }

  closeModal(): void {
    this.isModalOpen = false;
    if (this.isBrowser) {
      document.body.style.overflow = '';
    }
    this.resetForm();
  }

  onOverlayClick(event: Event): void {
    if (event.target === event.currentTarget) {
      this.closeModal();
    }
  }

  resetForm(): void {
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }
    this.lineContexts = [];
    this.batchForm.reset({
      sendNotification: true,
      discountApplied: 0,
      paymentDueDate: '',
      notes: ''
    });
    this.addItem();
    this.isSubmitting = false;
    this.batchResult = null;
  }

  get minDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  get selectedClientHasEmail(): boolean {
    const clientId = this.batchForm.get('clientId')?.value;
    if (!clientId) return true;
    const client = this.clients.find(c => c.id === clientId);
    return !!client?.email;
  }

  get totalSubscriptions(): number {
    return this.items.controls.reduce((sum, line) => sum + (line.get('quantity')?.value || 0), 0);
  }

  get totalAmount(): number {
    const discount = this.batchForm.get('discountApplied')?.value || 0;
    const subtotal = this.items.controls.reduce((sum, line) => {
      const qty = line.get('quantity')?.value || 0;
      const price = line.get('priceSold')?.value || 0;
      return sum + (qty * price);
    }, 0);
    return Math.max(0, subtotal - discount);
  }

  get hasDuplicateServices(): boolean {
    const serviceIds = this.items.controls
      .map(line => line.get('serviceId')?.value)
      .filter(v => v);
    return serviceIds.length !== new Set(serviceIds).size;
  }

  onSubmit(): void {
    if (this.batchForm.invalid || this.hasDuplicateServices) {
      Object.keys(this.batchForm.controls).forEach((key) => {
        this.batchForm.get(key)?.markAsTouched();
      });
      this.items.controls.forEach(line => {
        Object.keys(line.controls).forEach(k => line.get(k)?.markAsTouched());
      });
      return;
    }

    const formValue = this.batchForm.getRawValue();
    const items: BatchItem[] = formValue.items.map((line: { serviceId: string; quantity: number; priceSold: number; useAutoAssign: boolean; profileId: string }) => ({
      serviceId: line.serviceId,
      quantity: line.quantity,
      priceSold: line.priceSold,
      profileId: line.useAutoAssign ? undefined : (line.profileId || undefined)
    }));

    const request: BatchCreateManualSubscriptionRequest = {
      clientId: formValue.clientId,
      items,
      discountApplied: formValue.discountApplied || 0,
      paymentDueDate: formValue.paymentDueDate,
      sendNotification: formValue.sendNotification,
      notes: formValue.notes || undefined
    };

    this.isSubmitting = true;
    this.batchResult = null;

    this.subscriptionsService.createBatchSubscriptions(request).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.batchResult = {
          success: response.successCount ?? 0,
          failed: response.failedCount ?? 0,
          total: response.totalRequested ?? 0
        };

        if (this.batchResult.failed === 0) {
          this.toastService.success(`${this.batchResult.success} suscripciones creadas exitosamente.`);
          this.batchCreated.emit();
          setTimeout(() => this.closeModal(), 1500);
        } else if (this.batchResult.success > 0) {
          this.toastService.warning(`${this.batchResult.success} creadas, ${this.batchResult.failed} fallidas. Revisa el detalle.`);
          this.batchCreated.emit();
        } else {
          this.toastService.error('Todas las suscripciones fallaron. Revisa el detalle.');
        }
      },
      error: () => {
        this.isSubmitting = false;
        this.toastService.error('Error al crear las suscripciones.');
      }
    });
  }
}
