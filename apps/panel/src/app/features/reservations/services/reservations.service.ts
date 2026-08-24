import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map } from 'rxjs';
import {
  ReservationsApiService,
  ReservationResponse as ApiReservationResponse, 
  ReservationRequest as ApiReservationRequest,
} from '@alexandercanon/api-client-angular';
import {
  CreateReservationRequest,
  ReservationsFilter,
  ReservationResponse,
  ReservationStatus
} from '@neversion/models';

@Injectable({ providedIn: 'root' })
export class ReservationsService {
  private readonly reservationsApi = inject(ReservationsApiService);

  private readonly _reservations = signal<ReservationResponse[]>([]);
  readonly reservations = this._reservations.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getReservations(filter?: ReservationsFilter): Observable<ReservationResponse[]> {
    this._isLoading.set(true);
    return this.reservationsApi.listReservationsReservation(
      filter?.status as 'PENDING' | 'UPLOADED' | 'VALIDATED' | 'REJECTED' | 'EXPIRED' | 'CANCELLED' | undefined,
      'body',
      false,
    ).pipe(
      map(apiRes => apiRes.map(api => this.mapToModel(api))),
      tap((data) => this._reservations.set(data)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getReservationById(id: string): Observable<ReservationResponse> {
    return this.reservationsApi.getReservationReservation(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  createReservation(request: CreateReservationRequest): Observable<ReservationResponse> {
    const apiRequest: ApiReservationRequest = {
      clientId: request.clientId ?? '',
      items: request.items.map((item) => ({
        serviceUuid: String(item.inventoryId),
        qty: item.qty,
      })),
    };

    return this.reservationsApi.createReservationReservation(apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newReserv) => {
        this._reservations.update((current) => [newReserv, ...current]);
      })
    );
  }

  uploadReceipt(id: string, receiptUrl: string): Observable<void> {
    return this.reservationsApi.uploadReceiptReservation(
      id,
      { receiptUrl },
      'body',
      false,
    ).pipe(map(() => undefined));
  }

  validateReservation(id: string, notes: string): Observable<void> {
    return this.reservationsApi.validateReservationReservation(
      id,
      { notes },
      'body',
      false,
    ).pipe(map(() => undefined));
  }

  cancelReservation(id: string): Observable<void> {
    return this.reservationsApi.cancelReservationReservation(
      id,
      'body',
      false,
    ).pipe(map(() => undefined));
  }

  attachClient(id: string, clientId: string): Observable<void> {
    return this.reservationsApi.attachClientReservation(
      id,
      clientId,
      'body',
      false,
    ).pipe(map(() => undefined));
  }

  refreshReservations(): void {
    this.getReservations().subscribe();
  }

  private mapToModel(api: ApiReservationResponse): ReservationResponse {
    return {
      id: api.id || '',
      clientId: api.clientId || null,
      status: (api.status as unknown as ReservationStatus) || 'PENDING',
      discount: api.discount || 0,
      total: api.total || 0,
      receiptUrl: api.receiptUrl || null,
      expirationDate: api.expirationDate || '',
      createdAt: api.createdAt || '',
      details: (api.details || []).map(apiDetail => ({
          id: apiDetail.id || '',
          inventoryId: apiDetail.serviceId || 0,
          qty: apiDetail.qty || 0,
          unitPrice: apiDetail.unitPrice || 0,
          subtotal: apiDetail.subtotal || 0
      }))
    };
  }
}
