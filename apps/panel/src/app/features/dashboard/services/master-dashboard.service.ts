import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ProductSummary, AccountGroup, ProfileItem } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class MasterDashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dashboard`;

  getProductsSummary(category = 'STREAMING'): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(this.baseUrl, { params: { category } });
  }

  getAccountsByProduct(productId: string): Observable<AccountGroup[]> {
    return this.http.get<AccountGroup[]>(`${this.baseUrl}/products/${productId}/accounts`);
  }

  getProfilesByAccount(accountId: string): Observable<ProfileItem[]> {
    return this.http.get<ProfileItem[]>(`${this.baseUrl}/accounts/${accountId}/profiles`);
  }
}
