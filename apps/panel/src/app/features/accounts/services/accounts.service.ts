import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, finalize, map } from 'rxjs';
import { AccountRequest as ApiAccountRequest, AccountResponse as ApiAccountResponse } from '@neversion/api-client';
import { AccountsFilter, AccountRequest, AccountResponse } from '@neversion/models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AccountsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  private readonly _accounts = signal<AccountResponse[]>([]);
  readonly accounts = this._accounts.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getAccounts(filter?: AccountsFilter): Observable<AccountResponse[]> {
    let params = new HttpParams();
    if (filter?.serviceId) params = params.set('serviceId', filter.serviceId.toString());
    if (filter?.saleMode) params = params.set('saleMode', filter.saleMode);
    if (filter?.isActive !== undefined) params = params.set('isActive', String(filter.isActive));

    this._isLoading.set(true);
    return this.http.get<ApiAccountResponse[]>(`${this.baseUrl}/accounts`, { params }).pipe(
      map(apiAccounts => apiAccounts.map(api => this.mapToModel(api))),
      tap((accounts) => this._accounts.set(accounts)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getAccountById(id: string): Observable<AccountResponse> {
    return this.http.get<ApiAccountResponse>(`${this.baseUrl}/accounts/${id}`).pipe(
      map(api => this.mapToModel(api))
    );
  }

  createAccount(account: AccountRequest): Observable<AccountResponse> {
    const apiRequest: ApiAccountRequest = {
      email: account.email,
      pass: account.password,
      serviceId: account.serviceId,
      saleMode: account.saleMode as unknown as ApiAccountRequest.SaleModeEnum,
      renewalDate: account.renewalDate,
      notes: account.notes
    };

    return this.http.post<ApiAccountResponse>(`${this.baseUrl}/accounts`, apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newAccount) => {
        this._accounts.update((current) => [...current, newAccount]);
      })
    );
  }

  deactivateAccount(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/accounts/${id}`).pipe(
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
      password: api.pass || '',
      serviceId: api.serviceId || 0,
      saleMode: api.saleMode as unknown as AccountResponse['saleMode'],
      renewalDate: api.renewalDate || '',
      notes: api.notes || '',
      plan: api.plan || '',
      activeProfiles: 0,
      maxProfiles: 0,
      profiles: [],
      createdAt: api.createdAt || ''
    };
  }
}
