import { Component, EventEmitter, Output, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of, throwError } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { BatchCreateManualSubscriptionRequest, BatchItem } from '@alexandercanon/api-client-angular';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { AccountsService } from '../../../accounts/services/accounts.service';
import { ProfileService } from '../../../accounts/services/profile.service';
import { ClientsService } from '../../../clients/services/clients.service';
import { ServicesDataService } from '../../../services/services/services-data.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ProfileResponse, AccountResponse, ClientResponse, ServiceResponse } from '@neversion/models';

interface ServiceLineContext {
  accounts: AccountResponse[];
  profiles: ProfileResponse[];
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

  // WhatsApp xN flow: quick slot count selector (1-5) and inline client creation.
  readonly minSlots = 1;
  readonly maxSlots = 5;
  readonly quickSlotOptions = [2, 3, 4, 5];
  showNewClientForm = false;
  isCreatingClient = false;
  newClientForm!: FormGroup;

  private readonly fb = inject(FormBuilder);
  private readonly subscriptionsService = inject(SubscriptionsService);
  private readonly servicesService = inject(ServicesDataService);
  private readonly accountsService = inject(AccountsService);
  private readonly profileService = inject(ProfileService);
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

    this.newClientForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      phone: ['', [Validators.required, Validators.maxLength(50)]],
      email: ['', [Validators.email, Validators.maxLength(255)]]
    });

    this.addItem();
  }

  get items(): FormArray<FormGroup> {
    return this.batchForm.get('items') as FormArray<FormGroup>;
  }

  addItem(): void {
    if (this.items && this.items.length >= this.maxSlots) {
      return;
    }
    const line = this.fb.group({
      serviceId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      priceSold: [0, [Validators.required, Validators.min(0)]],
      useAutoAssign: [true],
      accountId: [''],
      profileId: [''],
      profileName: ['', [Validators.maxLength(100)]],
      profilePin: ['', [Validators.maxLength(20)]]
    });

    const index = this.items.length;
    this.lineContexts.push({ accounts: [], profiles: [], availableCount: 0 });

    line.get('serviceId')?.valueChanges.subscribe(serviceId => {
      this.onServiceChange(index, serviceId ?? '');
    });

    line.get('accountId')?.valueChanges.subscribe(accountId => {
      this.onAccountChange(index, accountId ?? '');
    });

    // Single source of truth: the manual panel follows the switch.
    // Switching back to auto clears the manual selection (name/PIN are kept).
    line.get('useAutoAssign')?.valueChanges.subscribe(useAuto => {
      if (useAuto) {
        line.patchValue({ accountId: '', profileId: '' }, { emitEvent: false });
        this.lineContexts[index].profiles = [];
      }
    });

    this.items.push(line);
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
    this.lineContexts.splice(index, 1);
  }

  setLineCount(count: number): void {
    const target = Math.min(Math.max(count, this.minSlots), this.maxSlots);
    while (this.items.length < target) {
      this.addItem();
    }
    while (this.items.length > target) {
      this.removeItem(this.items.length - 1);
    }
  }

  toggleNewClientForm(): void {
    this.showNewClientForm = !this.showNewClientForm;
    if (this.showNewClientForm) {
      const searchTerm = this.batchForm.get('clientSearch')?.value || '';
      if (searchTerm && !this.newClientForm.get('name')?.value) {
        this.newClientForm.patchValue({ name: searchTerm });
      }
    }
  }

  createAndSelectClient(): void {
    if (this.newClientForm.invalid) {
      Object.keys(this.newClientForm.controls).forEach((key) => {
        this.newClientForm.get(key)?.markAsTouched();
      });
      return;
    }
    this.isCreatingClient = true;
    const formValue = this.newClientForm.value;
    this.clientsService.createClient({
      name: formValue.name,
      phone: formValue.phone,
      email: formValue.email || undefined
    }).subscribe({
      next: (created) => {
        this.isCreatingClient = false;
        this.clients = [created, ...this.clients];
        this.selectClient(created);
        this.showNewClientForm = false;
        this.newClientForm.reset();
        this.toastService.success('Cliente registrado correctamente.');
      },
      error: () => {
        this.isCreatingClient = false;
        this.toastService.error('No se pudo registrar el cliente.');
      }
    });
  }

  isManual(index: number): boolean {
    return !this.items.at(index).get('useAutoAssign')?.value;
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
        ctx.profiles = this.mapDetailProfiles(detail, accountId);
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
    this.showNewClientForm = false;
    this.isCreatingClient = false;
    this.newClientForm?.reset();
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

    this.isSubmitting = true;
    this.batchResult = null;

    // Manual lines must target a profile; auto lines resolve at submit time.
    const missingTarget = this.items.controls.some(line =>
      !line.get('useAutoAssign')?.value && !line.get('profileId')?.value);
    if (missingTarget) {
      this.isSubmitting = false;
      this.toastService.error('En modo manual debes elegir cuenta y Perfil en cada línea.');
      return;
    }

    // Step 1: resolve auto lines carrying a custom name/PIN into concrete profiles.
    // Step 2: rename customized profiles.
    // Step 3: create the batch. Any failure aborts before creating subscriptions.
    this.resolveAutoTargets().pipe(
      switchMap(() => this.renameCustomizedProfiles()),
      switchMap(() => {
        const raw = this.batchForm.getRawValue();
        const batchItems: BatchItem[] = raw.items.map(
          (line: { serviceId: string; quantity: number; priceSold: number; useAutoAssign: boolean; profileId: string }) => ({
            serviceId: line.serviceId,
            quantity: line.quantity,
            priceSold: line.priceSold,
            profileId: line.useAutoAssign ? undefined : (line.profileId || undefined)
          }));
        const batchRequest: BatchCreateManualSubscriptionRequest = {
          clientId: raw.clientId,
          items: batchItems,
          discountApplied: raw.discountApplied || 0,
          paymentDueDate: raw.paymentDueDate,
          sendNotification: raw.sendNotification,
          notes: raw.notes || undefined
        };
        return this.subscriptionsService.createBatchSubscriptions(batchRequest);
      })
    ).subscribe({
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
          this.accountsService.refreshAccounts().subscribe();
          setTimeout(() => this.closeModal(), 1500);
        } else if (this.batchResult.success > 0) {
          this.toastService.warning(`${this.batchResult.success} creadas, ${this.batchResult.failed} fallidas. Revisa el detalle.`);
          this.batchCreated.emit();
          this.accountsService.refreshAccounts().subscribe();
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

  private resolveAutoTargets() {
    const resolutions = this.items.controls.map((line, index) => {
      const useAuto = line.get('useAutoAssign')?.value;
      const profileName = (line.get('profileName')?.value || '').trim();
      const profilePin = (line.get('profilePin')?.value || '').trim();
      if (!useAuto || (!profileName && !profilePin)) {
        return of(true);
      }
      const ctx = this.lineContexts[index];
      const account = ctx.accounts.find(a => (a.availableProfiles || 0) > 0);
      if (!account) {
        this.toastService.error('No hay perfiles disponibles para una línea con nombre personalizado.');
        return throwError(() => new Error('no-availability-for-rename'));
      }
      return this.accountsService.getAccountDetail(account.id).pipe(
        map(detail => {
          const target = (detail.profiles ?? []).find(p => p.status === 'AVAILABLE');
          if (!target || !target.id) {
            this.toastService.error('No hay perfiles disponibles para una línea con nombre personalizado.');
            throw new Error('no-availability-for-rename');
          }
          line.patchValue(
            { accountId: account.id, profileId: target.id, useAutoAssign: false },
            { emitEvent: false });
          this.lineContexts[index].profiles = this.mapDetailProfiles(detail, account.id);
          return true;
        }),
        catchError((err) => {
          if (err instanceof Error && err.message === 'no-availability-for-rename') {
            throw err;
          }
          this.toastService.error('No se pudo resolver un Perfil disponible.');
          throw new Error('resolve-failed');
        })
      );
    });

    return forkJoin(resolutions).pipe(map(() => true));
  }

  private mapDetailProfiles(
    detail: { profiles?: { id?: string; name?: string; pin?: string; notes?: string; isOwner?: boolean; status?: string }[] },
    accountId: string
  ): ProfileResponse[] {
    return (detail.profiles ?? []).map(profile => ({
      id: profile.id || '',
      accountId,
      name: profile.name || '',
      pin: profile.pin,
      notes: profile.notes,
      isOwner: profile.isOwner ?? false,
      status: profile.status as ProfileResponse['status'],
      createdAt: ''
    })).filter(profile => profile.status === 'AVAILABLE');
  }

  private renameCustomizedProfiles() {
    const renames = this.items.controls
      .map((line, index) => {
        const profileId = line.get('profileId')?.value;
        const profileName = (line.get('profileName')?.value || '').trim();
        const profilePin = (line.get('profilePin')?.value || '').trim();
        const accountId = line.get('accountId')?.value;
        if (!profileId || (!profileName && !profilePin)) {
          return null;
        }
        const current = this.lineContexts[index]?.profiles.find(p => p.id === profileId);
        return this.profileService.updateProfile(profileId, {
          accountId,
          name: profileName || current?.name || 'Perfil',
          pin: profilePin || current?.pin || '',
          notes: current?.notes ?? '',
          isOwner: current?.isOwner ?? false
        }).pipe(
          map(() => true),
          catchError(() => {
            this.toastService.error('No se pudo nombrar un Perfil. Se canceló la asignación.');
            throw new Error('profile-rename-failed');
          })
        );
      })
      .filter(op => op !== null);

    if (renames.length === 0) {
      return of(true);
    }
    return forkJoin(renames).pipe(map(() => true));
  }
}
