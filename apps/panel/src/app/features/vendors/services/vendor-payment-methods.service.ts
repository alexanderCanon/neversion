import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, from, map, catchError, of } from 'rxjs';
import { VendorsApiService, VendorProfileResponse } from '@alexandercanon/api-client-angular';
import { BankAccount } from '@neversion/models';
import { SupabaseService } from '../../../core/services/supabase.service';
import { runtimeConfig } from '../../../core/config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class VendorPaymentMethodsService {
  private readonly vendorsApi = inject(VendorsApiService);
  private readonly supabaseService = inject(SupabaseService);
  private readonly http = inject(HttpClient);

  getBankAccounts(): Observable<BankAccount[]> {
    return this.vendorsApi.meVendor().pipe(
      map((vendor: VendorProfileResponse) => {
        if (!vendor.bankDetails) return [];
        try {
          const parsed = typeof vendor.bankDetails === 'string'
            ? JSON.parse(vendor.bankDetails)
            : vendor.bankDetails;
          return Array.isArray(parsed) ? (parsed as BankAccount[]) : [];
        } catch {
          return [];
        }
      }),
      catchError(() => {
        return from(
          this.supabaseService.client
            .from('v_store_vendors')
            .select('bank_details')
            .maybeSingle()
        ).pipe(
          map(({ data }) => {
            if (!data?.bank_details) return [];
            const details = typeof data.bank_details === 'string'
              ? JSON.parse(data.bank_details)
              : data.bank_details;
            return Array.isArray(details) ? (details as BankAccount[]) : [];
          }),
          catchError(() => of([]))
        );
      })
    );
  }

  saveBankAccounts(accounts: BankAccount[]): Observable<void> {
    const jsonPayload = JSON.stringify(accounts);
    const endpoint = `${runtimeConfig.apiUrl}/api/v1/vendors/bank-details`;

    return this.http.put(endpoint, { bankDetails: jsonPayload }).pipe(
      map(() => void 0),
      catchError((httpErr) => {
        return from(
          (async () => {
            const { data: { user } } = await this.supabaseService.client.auth.getUser();
            if (!user) {
              throw new Error('Usuario no autenticado');
            }
            const { error } = await this.supabaseService.client
              .from('vendors')
              .update({ bank_details: accounts })
              .eq('user_id', user.id);

            if (error) {
              const { error: error2 } = await this.supabaseService.client
                .from('vendors')
                .update({ bank_details: accounts });
              if (error2 && httpErr) {
                throw httpErr;
              }
            }
          })()
        );
      })
    );
  }
}
