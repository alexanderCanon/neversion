import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, finalize, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { 
  ReservationResponse as ApiReservationResponse, 
  ReservationRequest as CreateReservationRequest,
  ReservationDetailResponse as ApiReservationDetailResponse
} from '@neversion/api-client';
import { ReservationsFilter, ReservationResponse, ReservationDetailResponse } from '@neversion/models';

@Injectable({ providedIn: 'root' })
export class ReservationsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/reservations`;

  private readonly _reservations = signal<ReservationResponse[]>([]);
  readonly reservations = this._reservations.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getReservations(filter?: ReservationsFilter): Observable<ReservationResponse[]> {
    let params = new HttpParams();
    if (filter?.status) {
      params = params.set('status', filter.status);
    }

    this._isLoading.set(true);
    return this.http.get<ApiReservationResponse[]>(this.baseUrl, { params }).pipe(
      map(apiRes => apiRes.map(api => this.mapToModel(api))),
      tap((data) => this._reservations.set(data)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getReservationById(id: string): Observable<ReservationResponse> {
    return this.http.get<ApiReservationResponse>(`${this.baseUrl}/${id}`).pipe(
      map(api => this.mapToModel(api))
    );
  }

  createReservation(request: CreateReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ApiReservationResponse>(this.baseUrl, request).pipe(
      map(api => this.mapToModel(api)),
      tap((newReserv) => {
        this._reservations.update((current) => [newReserv, ...current]);
      })
    );
  }

  uploadReceipt(id: string, receiptUrl: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}/receipt`, { receiptUrl });
  }

  validateReservation(id: string, notes: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}/validate`, { notes });
  }

  cancelReservation(id: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}/cancel`, {});
  }

  attachClient(id: string, clientId: string): Observable<void> {
    const params = new HttpParams().set('clientId', clientId);
    return this.http.put<void>(`${this.baseUrl}/${id}/client`, {}, { params });
  }

  refreshReservations(): void {
    this.getReservations().subscribe();
  }

  private mapToModel(api: ApiReservationResponse): ReservationResponse {
    return {
      id: api.id || '',
      clientId: api.clientId || null,
      status: (api.status as any) || 'PENDING',
      discount: api.discount || 0,
      total: api.total || 0,
      receiptUrl: api.receiptUrl || null,
      expirationDate: api.expirationDate || '',
      createdAt: api.createdAt || '',
      details: (api.details || []).map(this.mapDetailToModel)
    };
  }

  private mapDetailToModel(api: ApiReservationDetailResponse): ReservationDetailResponse {
    return {
      id: api.id || '',
      inventoryId: api.inventoryId || 0,
      qty: api.qty || 0,
      unitPrice: api.unitPrice || 0,
      subtotal: api.subtotal || 0
    };
  }
}
