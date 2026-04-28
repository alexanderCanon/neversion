import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { OrderResponse as ApiOrderResponse, OrderDetailResponse as ApiOrderDetailResponse } from '@neversion/api-client';
import { OrderResponse, OrderStatus } from '@neversion/models';

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/orders`;

  getOrderById(id: string): Observable<ApiOrderDetailResponse> {
    return this.http.get<ApiOrderDetailResponse>(`${this.baseUrl}/${id}`);
  }

  getOrderByReservationId(reservationId: string): Observable<ApiOrderResponse> {
    return this.http.get<ApiOrderResponse>(`${this.baseUrl}/by-reservation/${reservationId}`);
  }

  getOrdersByVendor(vendorUuid: string, status?: string): Observable<OrderResponse[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<ApiOrderResponse[]>(`${this.baseUrl}/vendor/${vendorUuid}`, { params }).pipe(
      map(apiRes => apiRes.map(api => this.mapToModel(api)))
    );
  }

  private mapToModel(api: ApiOrderResponse): OrderResponse {
    return {
      id: api.id || '',
      reservationId: api.reservationId || '',
      status: (api.status as unknown as OrderStatus) || 'PENDING',
      notes: api.notes || '',
      total: api.total || 0,
      paymentMethod: api.paymentMethod || '',
      createdAt: api.createdAt || ''
    };
  }
}
