import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, finalize } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { 
  ReservationResponse, 
  CreateReservationRequest, 
  ReservationsFilter 
} from '../models/reservation.model';

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
    return this.http.get<ReservationResponse[]>(this.baseUrl, { params }).pipe(
      tap((data) => this._reservations.set(data)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getReservationById(id: string): Observable<ReservationResponse> {
    return this.http.get<ReservationResponse>(`${this.baseUrl}/${id}`);
  }

  createReservation(request: CreateReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(this.baseUrl, request).pipe(
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
}
