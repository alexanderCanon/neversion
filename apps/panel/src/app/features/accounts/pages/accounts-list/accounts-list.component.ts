import { Component, OnInit, inject, signal, computed, ViewChild, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountsService } from '../../services/accounts.service';
import { AccountResponse, AccountStatus, ProfileResponse, ProfileStatus, ServiceResponse } from '@neversion/models';
import { AccountFormComponent } from '../../components/account-form/account-form.component';
import { ProfileListComponent } from '../../components/profile-list/profile-list.component';
import { ToastService } from '../../../../core/services/toast.service';
import { AccountDetailResponse, ProfileSummaryResponse } from '@neversion/api-client';
import { AuthService } from '../../../../core/services/auth.service';
import { ServicesDataService } from '../../../services/services/services-data.service';

import { ActivatedRoute, Router } from '@angular/router';

interface AccountServiceGroup {
  key: string;
  name: string;
  accounts: AccountResponse[];
  totalProfiles: number;
  occupiedProfiles: number;
  availableProfiles: number;
}

@Component({
  selector: 'app-accounts-list',
  standalone: true,
  imports: [CommonModule, FormsModule, AccountFormComponent, ProfileListComponent],
  templateUrl: './accounts-list.component.html',
  styleUrl: './accounts-list.component.scss'
  })
export class AccountsListComponent implements OnInit {
  @ViewChild('accountForm') accountForm!: AccountFormComponent;

  private readonly accountsService = inject(AccountsService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);
  private readonly servicesDataService = inject(ServicesDataService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private lastLoadedVendorUuid: string | null = null;

  readonly serviceUuidParam = signal<string | null>(null);
  readonly currentService = signal<ServiceResponse | null>(null);

  readonly accounts = this.accountsService.accounts;
  readonly isLoading = this.accountsService.isLoading;
  readonly vendorUuid = computed(() => this.authService.currentVendorUuid());
  readonly isSuperAdmin = this.authService.isSuperAdmin;
  readonly hasRequestedAccounts = signal(false);
  readonly loadError = signal(false);

  searchTerm = signal('');
  filterType = signal<AccountStatus | 'ALL'>('ALL');

  readonly filteredAccounts = computed(() => {
    let result = this.accounts();
    const type = this.filterType();

    if (type !== 'ALL') {
        result = result.filter(a => a.status === type);
    }

    const term = this.searchTerm().toLowerCase();
    if (term) {
      result = result.filter(
        (a) =>
          a.email?.toLowerCase().includes(term) ||
          a.plan?.toLowerCase().includes(term) ||
          a.serviceName?.toLowerCase().includes(term)
      );
    }

    return result;
  });

  readonly groupedAccounts = computed<AccountServiceGroup[]>(() => {
    const groups = new Map<string, AccountServiceGroup>();

    for (const account of this.filteredAccounts()) {
      const key = account.serviceUuid || account.serviceId || 'unknown';
      const group = groups.get(key) ?? {
        key,
        name: account.serviceName || this.unresolvedServiceLabel(account),
        accounts: [],
        totalProfiles: 0,
        occupiedProfiles: 0,
        availableProfiles: 0,
      };

      group.accounts.push(account);
      group.totalProfiles += account.totalProfiles || 0;
      group.occupiedProfiles += account.occupiedProfiles || 0;
      group.availableProfiles += account.availableProfiles || 0;
      groups.set(key, group);
    }

    return Array.from(groups.values())
      .sort((a, b) => a.name.localeCompare(b.name, 'es'));
  });

  expandedAccounts = new Set<string>();
  detailedData = signal<Record<string, AccountDetailResponse>>({});
  loadingDetails = new Set<string>();

  currentPage = signal(1);
  pageSize = 5;

  constructor() {
    effect(() => {
      const vendorUuid = this.vendorUuid();
      if (!vendorUuid || this.lastLoadedVendorUuid === vendorUuid) {
        return;
      }
      this.lastLoadedVendorUuid = vendorUuid;
      this.loadAccounts();
    }, { allowSignalWrites: true });
  }

  readonly paginatedAccounts = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredAccounts().slice(start, start + this.pageSize);
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.filteredAccounts().length / this.pageSize)
  );

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const serviceUuid = params['serviceUuid'];
      if (serviceUuid) {
        this.serviceUuidParam.set(serviceUuid);
        this.servicesDataService.getServiceById(serviceUuid).subscribe({
          next: (service) => this.currentService.set(service)
        });
      }
    });

    this.route.queryParams.subscribe(params => {
        if (params['filter']) {
            this.filterType.set(params['filter'] as AccountStatus);
        }
    });
  }

  loadAccounts(): void {
    const vendorUuid = this.vendorUuid();
    if (!vendorUuid) {
      return;
    }

    this.hasRequestedAccounts.set(true);
    this.loadError.set(false);

    const filter = this.serviceUuidParam()
      ? { serviceId: this.serviceUuidParam()! }
      : undefined;

    this.accountsService.getAccounts(filter).subscribe({
      error: () => {
        this.loadError.set(true);
      },
    });
  }

  goBackToSelector(): void {
    this.router.navigate(['/accounts']);
  }

  toggleAccount(accountId: string): void {
    if (this.expandedAccounts.has(accountId)) {
      this.expandedAccounts.delete(accountId);
    } else {
      this.expandedAccounts.add(accountId);
      this.loadAccountDetail(accountId);
    }
  }

  private loadAccountDetail(id: string): void {
      if (this.detailedData()[id]) return;
      
      this.loadingDetails.add(id);
      this.accountsService.getAccountDetail(id).subscribe({
          next: (detail) => {
              this.detailedData.update(prev => ({ ...prev, [id]: detail }));
              this.loadingDetails.delete(id);
          },
          error: () => this.loadingDetails.delete(id)
      });
  }

  isExpanded(accountId: string): boolean {
    return this.expandedAccounts.has(accountId);
  }

  onAccountCreated(): void {
    this.loadAccounts();
  }

  onProfilesChanged(accountId: string): void {
      this.detailedData.update((current) => {
        const remainingDetails = { ...current };
        delete remainingDetails[accountId];
        return remainingDetails;
      });
      this.loadAccountDetail(accountId);
      this.loadAccounts();
  }

  retryLoadAccounts(): void {
    this.loadAccounts();
  }

  deleteAccount(account: AccountResponse): void {
    if (confirm('¿Estás seguro? Se eliminará la cuenta y todos sus perfiles de forma permanente.')) {
      this.accountsService.deactivateAccount(account.id).subscribe({
        next: () => {
          this.toastService.success(`Cuenta ${account.email} desactivada`);
        },
      });
    }
  }

  copyAccountValue(value: string, label: string): void {
    if (!value) {
      this.toastService.error(`${label} no disponible`);
      return;
    }

    this.copyToClipboard(value)
      .then(() => this.toastService.success(`${label} copiado`))
      .catch(() => this.toastService.error(`No se pudo copiar ${label.toLowerCase()}`));
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'AVAILABLE': return 'status-available';
      case 'PARTIAL':   return 'status-partial';
      case 'FULL':      return 'status-full';
      case 'EXPIRED':   return 'status-expired';
      default: return 'status-default';
    }
  }

  getAccountStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      AVAILABLE: 'Disponible',
      PARTIAL: 'Parcial',
      FULL: 'Llena',
      EXPIRED: 'Vencida'
    };
    return labels[status?.toUpperCase()] || status;
  }

  getProfilesForAccount(id: string): ProfileResponse[] {
      const detail = this.detailedData()[id];
      return (detail?.profiles || []).map((profile: ProfileSummaryResponse) => ({
        id: profile.id || '',
        accountId: id,
        name: profile.name || '',
        pin: profile.pin,
        isOwner: Boolean(profile.isOwner),
        status: (profile.status as unknown as ProfileStatus) || ProfileStatus.AVAILABLE,
        createdAt: '',
      }));
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
    this.currentPage.set(1);
  }

  prevPage(): void {
    if (this.currentPage() > 1) {
      this.currentPage.update((p) => p - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages()) {
      this.currentPage.update((p) => p + 1);
    }
  }

  trackByAccountId(index: number, account: AccountResponse): string {
    return account.id;
  }

  private unresolvedServiceLabel(account: AccountResponse): string {
    return account.serviceId ? `Servicio no resuelto #${account.serviceId}` : 'Servicio no resuelto';
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
