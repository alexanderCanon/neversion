import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, finalize, map } from 'rxjs';
import { ServiceRequest as ApiServiceRequest, ServiceResponse as ApiServiceResponse } from '@neversion/api-client';
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
    return this.http.get<ApiServiceResponse[]>(`${this.baseUrl}/services`).pipe(
      map(apiServices => apiServices.map(api => this.mapToModel(api))),
      tap((services) => this._services.set(services)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getServiceById(id: string): Observable<ServiceResponse> {
    return this.http.get<ApiServiceResponse>(`${this.baseUrl}/services/${id}`).pipe(
      map(api => this.mapToModel(api))
    );
  }

  createService(service: ServiceRequest): Observable<ServiceResponse> {
    const apiRequest: ApiServiceRequest = {
      name: service.name,
      maxProfiles: service.maxProfiles,
      details: JSON.stringify(service.details)
    };

    return this.http.post<ApiServiceResponse>(`${this.baseUrl}/services`, apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newService) => {
        this._services.update((current) => [...current, newService]);
      })
    );
  }

  updateService(id: string, service: ServiceRequest): Observable<ServiceResponse> {
    const apiRequest: ApiServiceRequest = {
      name: service.name,
      maxProfiles: service.maxProfiles,
      details: JSON.stringify(service.details)
    };

    return this.http.put<ApiServiceResponse>(`${this.baseUrl}/services/${id}`, apiRequest).pipe(
        map(api => this.mapToModel(api)),
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

  private mapToModel(api: ApiServiceResponse): ServiceResponse {
    let details = { category: 'OTHERS', description: '', imageUrl: '' };
    try {
      if (api.details) {
        details = JSON.parse(api.details);
      } else if (api.category) {
        details.category = api.category;
      }
    } catch {
      if (api.category) details.category = api.category;
    }

    return {
      id: api.id || '',
      name: api.name || '',
      maxProfiles: api.maxProfiles || 0,
      details: details,
      createdAt: api.createdAt || ''
    };
  }
}
