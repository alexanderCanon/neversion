import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { OrderResponse as ApiOrderResponse } from '@neversion/api-client';
import { OrderResponse } from '@neversion/models';

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/orders`;

  getOrderById(id: string): Observable<OrderResponse> {
    return this.http.get<ApiOrderResponse>(`${this.baseUrl}/${id}`).pipe(
      map(api => this.mapToModel(api))
    );
  }

  getOrderByReservationId(reservationId: string): Observable<OrderResponse> {
    return this.http.get<ApiOrderResponse>(`${this.baseUrl}/by-reservation/${reservationId}`).pipe(
      map(api => this.mapToModel(api))
    );
  }

  private mapToModel(api: ApiOrderResponse): OrderResponse {
    return {
      id: api.uuid || '',
      reservationId: api.reservationId || '',
      status: (api.status as any) || 'PENDING',
      notes: api.notes || '',
      createdAt: api.createdAt || ''
    };
  }
}
