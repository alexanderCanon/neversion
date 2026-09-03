import { Component, EventEmitter, Output, ViewChild, ElementRef, OnInit, inject, signal, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccountRequest, SaleMode, ServiceResponse, AccountResponse } from '@neversion/models';
import { AccountsService } from '../../services/accounts.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ServicesDataService } from '../../../services/services/services-data.service';

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
  selector: 'app-account-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './account-form.component.html',
  styleUrl: './account-form.component.scss'
  })
export class AccountFormComponent implements OnInit {
  @ViewChild('accountModal') modalElement!: ElementRef;
  @Output() accountCreated = new EventEmitter<AccountRequest>();

  private _preselectedService: ServiceOption | null = null;

  @Input() set preselectedService(service: ServiceResponse | null) {
    if (service) {
      this._preselectedService = {
        id: service.id,
        displayName: service.name,
        maxProfiles: service.maxProfiles || 1
      };
      this.applyPreselectedService();
    } else {
      this._preselectedService = null;
      if (this.accountForm) {
        this.accountForm.get('serviceId')?.enable();
      }
    }
  }

  accountForm!: FormGroup;
  isSubmitting = false;
  isBrowser: boolean;
  isEditMode = false;
  accountId: string | null = null;

  readonly saleModes = Object.values(SaleMode);

  private readonly fb = inject(FormBuilder);
  private readonly accountsService = inject(AccountsService);
  private readonly toastService = inject(ToastService);
  private readonly servicesDataService = inject(ServicesDataService);

  readonly serviceOptions = signal<ServiceOption[]>([]);

  /** BR-02 ceiling: max profiles allowed by the selected service. */
  get serviceCeiling(): number | null {
    const serviceId = this.accountForm?.get('serviceId')?.value;
    const service = this.serviceOptions().find(s => s.id === serviceId);
    return service ? service.maxProfiles : null;
  }

  constructor() {
    this.isBrowser = true;
  }

  ngOnInit(): void {
    this.initForm();
    this.loadServices();
  }

  private loadServices(): void {
    this.servicesDataService.getServices().subscribe({
      next: (services) => {
        const options: ServiceOption[] = services.map(s => ({
          id: s.id,
          displayName: s.name,
          maxProfiles: s.maxProfiles || 1
        }));
        this.serviceOptions.set(options);
        this.applyCeiling();
      },
      error: (err) => console.error('Failed to load services', err)
    });
  }

  private initForm(): void {
    const today = new Date().toISOString().split('T')[0];
    this.accountForm = this.fb.group({
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
      notes: ['']
    });

    // Listen to saleMode changes to enable/disable maxProfiles
    this.accountForm.get('saleMode')?.valueChanges.subscribe(mode => {
      const maxProfilesCtrl = this.accountForm.get('maxProfiles');
      if (mode === SaleMode.FULL_ACCOUNT) {
        maxProfilesCtrl?.setValue(1);
        maxProfilesCtrl?.disable();
      } else {
        maxProfilesCtrl?.enable();
        const serviceId = this.accountForm.get('serviceId')?.value;
        const service = this.serviceOptions().find(s => s.id === serviceId);
        maxProfilesCtrl?.setValue(service ? service.maxProfiles : 1);
        this.applyCeiling();
      }
    });

    this.accountForm.get('serviceId')?.valueChanges.subscribe(() => this.applyCeiling());

    this.applyPreselectedService();
  }

  /** BR-02 ceiling: clamp maxProfiles to the selected service maximum (create mode only). */
  private applyCeiling(): void {
    const ctrl = this.accountForm.get('maxProfiles');
    if (!ctrl || this.isEditMode) return;
    const ceiling = this.serviceCeiling;
    const validators = [Validators.required, Validators.min(1)];
    if (ceiling != null) {
      validators.push(Validators.max(ceiling));
    }
    ctrl.setValidators(validators);
    if (ceiling != null && ctrl.value != null && Number(ctrl.value) > ceiling) {
      ctrl.setValue(ceiling);
    }
    ctrl.updateValueAndValidity({ emitEvent: false });
  }

  private applyPreselectedService(): void {
    if (this._preselectedService && this.accountForm) {
      this.accountForm.patchValue({
        serviceId: this._preselectedService.id,
        maxProfiles: this._preselectedService.maxProfiles
      });
      this.accountForm.get('serviceId')?.disable();
      this.applyCeiling();
    }
  }

  onServiceChange(serviceId: string): void {
    const service = this.serviceOptions().find(s => s.id === serviceId);
    if (service && this.accountForm.get('saleMode')?.value === SaleMode.BY_PROFILE) {
      this.accountForm.patchValue({ maxProfiles: service.maxProfiles });
    }
    this.applyCeiling();
  }

  openModal(account?: AccountResponse): void {
    if (this.isBrowser) {
      if (account) {
        this.isEditMode = true;
        this.accountId = account.id;
        
        this.accountForm.patchValue({
          email: account.email,
          password: account.password || '',
          serviceId: account.serviceUuid,
          maxProfiles: account.maxProfiles,
          saleMode: account.saleMode,
          renewalDate: account.renewalDate ? account.renewalDate.split('T')[0] : '',
          cost: account.cost,
          source: account.source || '',
          purchasedAt: account.purchasedAt ? account.purchasedAt.split('T')[0] : '',
          plan: account.plan || '',
          notes: account.notes || ''
        });

        // En modo edición no se puede cambiar el servicio
        this.accountForm.get('serviceId')?.disable();
        // Legacy over-ceiling accounts stay editable: no max validator here (backend guards increases).
        this.accountForm.get('maxProfiles')?.setValidators([Validators.required, Validators.min(1)]);
        this.accountForm.get('maxProfiles')?.updateValueAndValidity({ emitEvent: false });
      } else {
        this.isEditMode = false;
        this.accountId = null;
        this.resetForm();
        if (!this._preselectedService) {
          this.accountForm.get('serviceId')?.enable();
        }
      }

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
    if (this.accountForm.valid) {
      this.isSubmitting = true;
      const formValue = this.accountForm.getRawValue();

      const accountRequest: AccountRequest = {
        email: formValue.email,
        password: formValue.password,
        serviceId: formValue.serviceId,
        saleMode: formValue.saleMode as SaleMode,
        renewalDate: formValue.renewalDate,
        cost: Number(formValue.cost),
        source: formValue.source || undefined,
        purchasedAt: formValue.purchasedAt || undefined,
        plan: formValue.plan || undefined,
        notes: formValue.notes || undefined,
        maxProfiles: formValue.saleMode === SaleMode.FULL_ACCOUNT ? 1 : (Number(formValue.maxProfiles) || undefined)
      };

      if (this.isEditMode) {
        this.accountsService.updateAccount(this.accountId!, accountRequest).subscribe({
          next: () => {
            this.toastService.success('Cuenta actualizada exitosamente.');
            this.accountCreated.emit(accountRequest);
            this.closeModal();
          },
          error: () => {
            this.isSubmitting = false;
          },
        });
      } else {
        this.accountsService.createAccount(accountRequest).subscribe({
          next: () => {
            this.toastService.success('Cuenta ingresada existosamente.');
            this.accountCreated.emit(accountRequest);
            this.closeModal();
          },
          error: () => {
            this.isSubmitting = false;
          },
        });
      }
    } else {
      Object.keys(this.accountForm.controls).forEach((key) => {
        this.accountForm.get(key)?.markAsTouched();
      });
    }
  }

  resetForm(): void {
    const today = new Date().toISOString().split('T')[0];
    this.accountForm.reset({
      saleMode: SaleMode.BY_PROFILE,
      cost: 0,
      maxProfiles: 1,
      purchasedAt: today
    });
    this.accountForm.get('maxProfiles')?.enable();
    this.applyPreselectedService();
    this.applyCeiling();
    this.isSubmitting = false;
  }
}
