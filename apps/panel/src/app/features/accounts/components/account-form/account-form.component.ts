import { Component, EventEmitter, Output, ViewChild, ElementRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccountRequest, SaleMode } from '@neversion/models';
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

  accountForm!: FormGroup;
  isSubmitting = false;
  isBrowser: boolean;

  readonly saleModes = Object.values(SaleMode);

  private readonly fb = inject(FormBuilder);
  private readonly accountsService = inject(AccountsService);
  private readonly toastService = inject(ToastService);
  private readonly servicesDataService = inject(ServicesDataService);

  readonly serviceOptions = signal<ServiceOption[]>([]);

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
      plan: [''],
      maxProfiles: [1, [Validators.required, Validators.min(1)]],
      saleMode: [SaleMode.BY_PROFILE, Validators.required],
      renewalDate: ['', [Validators.required]],
      cost: [0, [Validators.required, Validators.min(0)]],
      source: [''],
      purchasedAt: [today],
      notes: ['']
    });
  }

  onServiceChange(serviceId: string): void {
    const service = this.serviceOptions().find(s => s.id === serviceId);
    if (service) {
      this.accountForm.patchValue({ maxProfiles: service.maxProfiles });
    }
  }

  openModal(): void {
    if (this.isBrowser) {
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
      const formValue = this.accountForm.value;

      const accountRequest: AccountRequest = {
        email: formValue.email,
        password: formValue.password,
        serviceId: formValue.serviceId,
        plan: formValue.plan || undefined,
        saleMode: formValue.saleMode as SaleMode,
        renewalDate: formValue.renewalDate,
        cost: Number(formValue.cost),
        source: formValue.source || undefined,
        purchasedAt: formValue.purchasedAt || undefined,
        notes: formValue.notes || undefined,
        maxProfiles: Number(formValue.maxProfiles) || undefined
      };

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
    this.isSubmitting = false;
  }
}
