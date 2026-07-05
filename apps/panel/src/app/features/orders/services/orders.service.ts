import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  OrdersApiService,
  OrderResponse as ApiOrderResponse,
  OrderDetailResponse as ApiOrderDetailResponse
} from '@neversion/api-client';
import { OrderResponse, OrderStatus } from '@neversion/models';

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private readonly ordersApi = inject(OrdersApiService);

  getOrderById(id: string): Observable<ApiOrderDetailResponse> {
    return this.ordersApi.getByIdOrderGet(id);
  }

  getOrderByReservationId(reservationId: string): Observable<ApiOrderResponse> {
    return this.ordersApi.getByReservationIdOrderGet(
      Number(reservationId),
      'body',
      false,
    );
  }

  getOrdersByVendor(vendorUuid: string, status?: string): Observable<OrderResponse[]> {
    return this.ordersApi.listByVendorOrderGet(
      vendorUuid,
      undefined,
      status as 'VALIDATED' | 'COMPLETED' | 'REJECTED' | 'CANCELLED' | 'PENDING' | undefined,
      'body',
      false,
    ).pipe(
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
