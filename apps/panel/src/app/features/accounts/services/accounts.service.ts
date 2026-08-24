import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map, shareReplay } from 'rxjs';
import { 
    AccountsApiService, 
    AccountRequest as ApiAccountRequest, 
    AccountResponse as ApiAccountResponse,
    AccountDetailResponse as ApiAccountDetailResponse,
    AccountWithSubscriptionRequest,
    CreateAccountWithSubscriptionResult
} from '@alexandercanon/api-client-angular';
import { AccountsFilter, AccountRequest, AccountResponse, SaleMode, AccountStatus } from '@neversion/models';

interface ApiAccountsPageResponse {
  content?: ApiAccountResponse[];
}

type AccountCredentialResponse = ApiAccountResponse & {
  password?: string;
  pass?: string;
};

@Injectable({ providedIn: 'root' })
export class AccountsService {
  private readonly accountsApi = inject(AccountsApiService);
  private readonly inFlightRequests = new Map<string, Observable<AccountResponse[]>>();

  private readonly _accounts = signal<AccountResponse[]>([]);
  readonly accounts = this._accounts.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  /**
   * List accounts for the current authenticated vendor (US-024)
   */
  getAccounts(filter?: AccountsFilter): Observable<AccountResponse[]> {
    const requestKey = `${filter?.serviceId ?? ''}:${filter?.status ?? ''}`;
    const cachedRequest = this.inFlightRequests.get(requestKey);
    if (cachedRequest) {
      return cachedRequest;
    }

    this._isLoading.set(true);
    const request$ = this.accountsApi.listAccountsAccount(
      filter?.serviceId,
      filter?.status as 'AVAILABLE' | 'PARTIAL' | 'FULL' | 'EXPIRED',
      'body',
      false,
    ).pipe(
      map((apiAccounts) => this.normalizeAccountsResponse(apiAccounts).map(api => this.mapToModel(api))),
      tap((accounts: AccountResponse[]) => this._accounts.set(accounts)),
      finalize(() => {
        this._isLoading.set(false);
        this.inFlightRequests.delete(requestKey);
      }),
      shareReplay(1)
    );

    this.inFlightRequests.set(requestKey, request$);
    return request$;
  }

  getAccountById(id: string): Observable<AccountResponse> {
    return this.accountsApi.getByIdAccount(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  /**
   * Detailed view with profiles (US-028)
   */
  getAccountDetail(id: string): Observable<ApiAccountDetailResponse> {
      return this.accountsApi.getDetailAccount(id).pipe(
        map((detail) => ({
          ...detail,
          profiles: detail.profiles ?? [],
        }))
      );
  }

  createAccount(account: AccountRequest): Observable<AccountResponse> {
    const apiRequest: ApiAccountRequest = {
      ...account,
      pass: account.password,
      saleMode: account.saleMode as unknown as ApiAccountRequest.SaleModeEnum
    };

    return this.accountsApi.createAccount(apiRequest).pipe(
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

    return this.accountsApi.updateAccount(id, apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((updatedAccount) => {
        this._accounts.update((current) => 
            current.map(a => a.id === id ? updatedAccount : a)
        );
      })
    );
  }

  deactivateAccount(id: string): Observable<void> {
    return this.accountsApi.deleteAccount(id).pipe(
      tap(() => {
        this._accounts.update((current) => current.filter((a) => a.id !== id));
      })
    );
  }

  refreshAccounts(): Observable<AccountResponse[]> {
    return this.getAccounts();
  }

  createAccountWithSubscription(request: AccountWithSubscriptionRequest): Observable<CreateAccountWithSubscriptionResult> {
    return this.accountsApi.createWithSubscriptionAccount(request, 'body', false);
  }

  private normalizeAccountsResponse(response: ApiAccountResponse[] | ApiAccountsPageResponse): ApiAccountResponse[] {
    if (Array.isArray(response)) {
      return response;
    }

    return response.content ?? [];
  }

  private mapToModel(api: ApiAccountResponse): AccountResponse {
    const credentials = api as AccountCredentialResponse;

    return {
      id: api.id || '',
      email: api.email || '',
      password: credentials.password ?? credentials.pass,
      serviceId: String(api.serviceId || ''),
      serviceUuid: api.serviceUuid || '',
      serviceName: api.serviceName || '',
      saleMode: api.saleMode as unknown as SaleMode,
      status: api.status as unknown as AccountStatus,
      renewalDate: api.renewalDate || '',
      cost: api.cost || 0,
      plan: api.plan || '',
      source: api.source || '',
      purchasedAt: api.purchasedAt || '',
      notes: api.notes || '',
      createdAt: api.createdAt || '',
      maxProfiles: (api as unknown as Record<string, number>)['maxProfiles'] || 0,
      totalProfiles: api.totalProfiles || 0,
      availableProfiles: api.availableProfiles || 0,
      occupiedProfiles: api.occupiedProfiles || 0,
      blockedProfiles: api.blockedProfiles || 0,
      profiles: []
    };
  }
}
