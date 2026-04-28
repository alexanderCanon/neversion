import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map, of } from 'rxjs';
import { 
    AccountsApiService, 
    AccountRequest as ApiAccountRequest, 
    AccountResponse as ApiAccountResponse,
    AccountDetailResponse as ApiAccountDetailResponse
} from '@neversion/api-client';
import { AccountsFilter, AccountRequest, AccountResponse, SaleMode, AccountStatus } from '@neversion/models';
import { AuthService } from '../../../core/services/auth.service';

@Injectable({ providedIn: 'root' })
export class AccountsService {
  private readonly accountsApi = inject(AccountsApiService);
  private readonly authService = inject(AuthService);

  private readonly _accounts = signal<AccountResponse[]>([]);
  readonly accounts = this._accounts.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  /**
   * List accounts for the current authenticated vendor (US-024)
   */
  getAccounts(filter?: AccountsFilter): Observable<AccountResponse[]> {
    const user = this.authService.currentUser();
    if (!user) return of([]);

    this._isLoading.set(true);
    // listByVendor3(vendorUuid, serviceUuid, status)
    return this.accountsApi.listByVendor3(user.id, filter?.serviceId, filter?.status as any).pipe(
      map((apiAccounts: ApiAccountResponse[]) => apiAccounts.map(api => this.mapToModel(api))),
      tap((accounts: AccountResponse[]) => this._accounts.set(accounts)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getAccountById(id: string): Observable<AccountResponse> {
    return this.accountsApi.getById3(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  /**
   * Detailed view with profiles (US-028)
   */
  getAccountDetail(id: string): Observable<ApiAccountDetailResponse> {
      return this.accountsApi.getDetail1(id);
  }

  createAccount(account: AccountRequest): Observable<AccountResponse> {
    const apiRequest: ApiAccountRequest = {
      ...account,
      pass: account.password,
      saleMode: account.saleMode as unknown as ApiAccountRequest.SaleModeEnum
    };

    return this.accountsApi.create3(apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newAccount) => {
        this._accounts.update((current) => [...current, newAccount]);
      })
    );
  }

  updateAccount(id: string, account: AccountRequest): Observable<AccountResponse> {
    const apiRequest: ApiAccountRequest = {
        ...account,
        pass: account.password,
        saleMode: account.saleMode as unknown as ApiAccountRequest.SaleModeEnum
    };

    return this.accountsApi.update3(id, apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((updatedAccount) => {
        this._accounts.update((current) => 
            current.map(a => a.id === id ? updatedAccount : a)
        );
      })
    );
  }

  deactivateAccount(id: string): Observable<void> {
    return this.accountsApi.delete3(id).pipe(
      tap(() => {
        this._accounts.update((current) => current.filter((a) => a.id !== id));
      })
    );
  }

  refreshAccounts(): Observable<AccountResponse[]> {
    return this.getAccounts();
  }

  private mapToModel(api: ApiAccountResponse): AccountResponse {
    return {
      id: api.id || '',
      email: api.email || '',
      // Password is intentionally omitted if not present in the response
      password: (api as any).pass || undefined, 
      serviceId: String(api.serviceId || ''),
      saleMode: api.saleMode as unknown as SaleMode,
      status: api.status as unknown as AccountStatus,
      renewalDate: api.renewalDate || '',
      cost: api.cost || 0,
      plan: api.plan || '',
      source: api.source || '',
      purchasedAt: api.purchasedAt || '',
      notes: api.notes || '',
      createdAt: api.createdAt || '',
      totalProfiles: api.totalProfiles || 0,
      availableProfiles: api.availableProfiles || 0,
      occupiedProfiles: api.occupiedProfiles || 0,
      blockedProfiles: api.blockedProfiles || 0,
      profiles: []
    };
  }
}
