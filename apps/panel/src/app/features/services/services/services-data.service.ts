import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, finalize } from 'rxjs';
import { ServiceRequest, ServiceResponse } from '@neversion/models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicesDataService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  private readonly _services = signal<ServiceResponse[]>([]);
  readonly services = this._services.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getServices(): Observable<ServiceResponse[]> {
    this._isLoading.set(true);
    return this.http.get<ServiceResponse[]>(`${this.baseUrl}/services`).pipe(
      tap((services) => this._services.set(services)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getServiceById(id: string): Observable<ServiceResponse> {
    return this.http.get<ServiceResponse>(`${this.baseUrl}/services/${id}`);
  }

  createService(service: ServiceRequest): Observable<ServiceResponse> {
    return this.http.post<ServiceResponse>(`${this.baseUrl}/services`, service).pipe(
      tap((newService) => {
        this._services.update((current) => [...current, newService]);
      })
    );
  }

  updateService(id: string, service: ServiceRequest): Observable<ServiceResponse> {
    return this.http.put<ServiceResponse>(`${this.baseUrl}/services/${id}`, service).pipe(
        tap((updatedService) => {
            this._services.update(current => 
                current.map(s => s.id === id ? updatedService : s)
            );
        })
    );
  }

  deleteService(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/services/${id}`).pipe(
      tap(() => {
        this._services.update((current) => current.filter((s) => s.id !== id));
      })
    );
  }

  refreshServices(): Observable<ServiceResponse[]> {
    return this.getServices();
  }
}
