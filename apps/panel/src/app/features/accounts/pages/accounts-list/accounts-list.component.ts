import { Component, OnInit, inject, signal, computed, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountsService } from '../../services/accounts.service';
import { AccountResponse, SaleMode } from '../../models/account.model';
import { AccountFormComponent } from '../../components/account-form/account-form.component';
import { ProfileListComponent } from '../../components/profile-list/profile-list.component';
import { ToastService } from '../../../../core/services/toast.service';

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
  private readonly route = inject(ActivatedRoute);

  readonly accounts = this.accountsService.accounts;
  readonly isLoading = this.accountsService.isLoading;

  searchTerm = signal('');
  filterType = signal<'ALL' | 'AVAILABLE' | 'OCCUPIED'>('ALL');

  readonly filteredAccounts = computed(() => {
    let result = this.accounts();
    const type = this.filterType();

    if (type === 'AVAILABLE') {
        result = result.filter(a => a.status === 'AVAILABLE');
    } else if (type === 'OCCUPIED') {
        result = result.filter(a => a.status === 'ASSIGNED'); // Or full
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
  currentPage = signal(1);
  pageSize = 5;

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
            this.filterType.set(params['filter'] as any);
        }
        this.loadAccounts();
    });
  }

  loadAccounts(): void {
    this.accountsService.getAccounts().subscribe();
  }

  toggleAccount(accountId: string): void {
    if (this.expandedAccounts.has(accountId)) {
      this.expandedAccounts.delete(accountId);
    } else {
      this.expandedAccounts.add(accountId);
    }
  }

  isExpanded(accountId: string): boolean {
    return this.expandedAccounts.has(accountId);
  }

  onAccountCreated(): void {
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
      case 'AVAILABLE':
        return 'bg-success';
      case 'ASSIGNED':
        return 'bg-secondary';
      case 'EXPIRED':
        return 'bg-danger';
      default:
        return 'bg-info';
    }
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
