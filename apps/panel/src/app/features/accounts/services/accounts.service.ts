import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, finalize } from 'rxjs';
import { AccountRequest, AccountResponse, AccountsFilter } from '@neversion/models';
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
    return this.http.get<AccountResponse[]>(`${this.baseUrl}/accounts`, { params }).pipe(
      tap((accounts) => this._accounts.set(accounts)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getAccountById(id: string): Observable<AccountResponse> {
    return this.http.get<AccountResponse>(`${this.baseUrl}/accounts/${id}`);
  }

  createAccount(account: AccountRequest): Observable<AccountResponse> {
    return this.http.post<AccountResponse>(`${this.baseUrl}/accounts`, account).pipe(
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
}
