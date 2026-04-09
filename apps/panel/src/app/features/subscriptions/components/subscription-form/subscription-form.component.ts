import { Component, EventEmitter, Output, ViewChild, ElementRef, OnInit, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateSubscriptionRequest } from '../../models/subscription.model';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { AccountsService } from '../../../accounts/services/accounts.service';
import { ClientsService } from '../../../clients/services/clients.service';
import { ToastService } from '../../../../core/services/toast.service';
import { AccountResponse } from '../../../accounts/models/account.model';
import { ClientResponse } from '../../../clients/models/client.model';
import { ProfileResponse } from '../../../accounts/models/profile.model';

@Component({
  selector: 'app-subscription-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './subscription-form.component.html',
  styleUrls: [],
})
export class SubscriptionFormComponent implements OnInit {
  @ViewChild('subscriptionModal') modalElement!: ElementRef;
  @Output() subscriptionCreated = new EventEmitter<CreateSubscriptionRequest>();

  subscriptionForm!: FormGroup;
  isSubmitting = false;
  isLoadingData = false;
  isBrowser: boolean;

  accounts: AccountResponse[] = [];
  clients: ClientResponse[] = [];
  filteredClients: ClientResponse[] = [];
  profiles: ProfileResponse[] = [];
  
  showClientDropdown = false;

  private readonly fb = inject(FormBuilder);
  private readonly subscriptionsService = inject(SubscriptionsService);
  private readonly accountsService = inject(AccountsService);
  private readonly clientsService = inject(ClientsService);
  private readonly toastService = inject(ToastService);

  constructor() {
    this.isBrowser = true;
  }

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    const defaultDate = new Date().toISOString().split('T')[0];
    this.subscriptionForm = this.fb.group({
      clientId: ['', Validators.required],
      clientSearch: ['', Validators.required],
      accountId: ['', Validators.required],
      profileId: ['', Validators.required],
      purchaseDate: [defaultDate, Validators.required],
      paymentDueDate: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0.01)]],
      notes: ['']
    });

    this.subscriptionForm.get('clientSearch')?.valueChanges.subscribe(value => {
      if (typeof value === 'string') {
        this.filterClients(value);
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
    
    // We fetch all non-expired accounts to list them
    this.accountsService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts.filter(a => a.status !== 'EXPIRED');
        this.isLoadingData = false;
      },
      error: () => {
        this.isLoadingData = false;
      },
    });

    this.clientsService.getClients().subscribe({
      next: (clients) => {
        this.clients = clients;
        this.filteredClients = clients;
      },
    });
  }

  onAccountChange(accountId: string): void {
    if (accountId) {
      const selectedAccount = this.accounts.find(a => a.id === accountId);
      if (selectedAccount && selectedAccount.profiles) {
        // Find available profiles or all profiles if we want to list them
        this.profiles = selectedAccount.profiles;
      } else {
        this.profiles = [];
      }
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
        const bootstrap = (window as any).bootstrap;
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
        const bootstrap = (window as any).bootstrap;
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
      const formValue = this.subscriptionForm.value;

      const subscriptionRequest: CreateSubscriptionRequest = {
        profileId: formValue.profileId,
        clientId: formValue.clientId,
        paymentDueDate: formValue.paymentDueDate,
        price: formValue.price,
        notes: formValue.notes || undefined,
      };

      this.subscriptionsService.createSubscription(subscriptionRequest).subscribe({
        next: () => {
          this.toastService.success('Suscripción creada exitosamente.');
          this.subscriptionCreated.emit(subscriptionRequest);
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
      purchaseDate: new Date().toISOString().split('T')[0]
    });
    this.profiles = [];
    this.isSubmitting = false;
  }

  get selectedAccount(): AccountResponse | undefined {
    const id = this.subscriptionForm.get('accountId')?.value;
    return this.accounts.find(a => a.id === id);
  }

  get minDate(): string {
    return new Date().toISOString().split('T')[0];
  }
}
