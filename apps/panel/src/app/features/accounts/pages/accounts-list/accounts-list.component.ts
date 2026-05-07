import { Component, OnInit, inject, signal, computed, ViewChild, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountsService } from '../../services/accounts.service';
import { AccountResponse, AccountStatus, ProfileResponse, ProfileStatus } from '@neversion/models';
import { AccountFormComponent } from '../../components/account-form/account-form.component';
import { ProfileListComponent } from '../../components/profile-list/profile-list.component';
import { ToastService } from '../../../../core/services/toast.service';
import { AccountDetailResponse, ProfileSummaryResponse } from '@neversion/api-client';
import { AuthService } from '../../../../core/services/auth.service';

import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-accounts-list',
  standalone: true,
  imports: [CommonModule, FormsModule, AccountFormComponent, ProfileListComponent],
  templateUrl: './accounts-list.component.html',
  styleUrls: [],
})
export class AccountsListComponent implements OnInit {
  @ViewChild('accountForm') accountForm!: AccountFormComponent;

  private readonly accountsService = inject(AccountsService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private lastLoadedVendorUuid: string | null = null;

  readonly accounts = this.accountsService.accounts;
  readonly isLoading = this.accountsService.isLoading;
  readonly vendorUuid = computed(() => this.authService.currentVendorUuid());
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
          a.plan?.toLowerCase().includes(term)
      );
    }

    return result;
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

    this.accountsService.getAccounts().subscribe({
      error: () => {
        this.loadError.set(true);
      },
    });
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
        const { [accountId]: _removed, ...remainingDetails } = current;
        return remainingDetails;
      });
      this.loadAccountDetail(accountId);
      this.loadAccounts();
  }

  retryLoadAccounts(): void {
    this.loadAccounts();
  }

  deactivateAccount(account: AccountResponse): void {
    if (confirm(`¿Seguro que deseas desactivar la cuenta: ${account.email}?`)) {
      this.accountsService.deactivateAccount(account.id).subscribe({
        next: () => {
          this.toastService.success(`Cuenta ${account.email} desactivada`);
        },
      });
    }
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'AVAILABLE': return 'bg-success';
      case 'PARTIAL':   return 'bg-warning text-dark';
      case 'FULL':      return 'bg-danger';
      case 'EXPIRED':   return 'bg-secondary';
      default: return 'bg-info';
    }
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
}
