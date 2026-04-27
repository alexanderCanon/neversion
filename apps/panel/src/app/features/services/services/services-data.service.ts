import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, finalize, map, of } from 'rxjs';
import { 
    ServicesApiService, 
    ServiceRequest as ApiServiceRequest, 
    ServiceResponse as ApiServiceResponse 
} from '@neversion/api-client';
import { ServiceRequest, ServiceResponse, ServicesFilter } from '@neversion/models';
import { AuthService } from '../../../core/services/auth.service';

@Injectable({ providedIn: 'root' })
export class ServicesDataService {
  private readonly servicesApi = inject(ServicesApiService);
  private readonly authService = inject(AuthService);

  private readonly _services = signal<ServiceResponse[]>([]);
  readonly services = this._services.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  /**
   * List services for the current authenticated vendor (US-020)
   */
  getServices(filter?: ServicesFilter): Observable<ServiceResponse[]> {
    const vendorUuid = this.getVendorUuid();
    if (!vendorUuid) return of([]);

    this._isLoading.set(true);
    return this.servicesApi.listByVendor(vendorUuid, filter?.category as any, filter?.isActive).pipe(
      map(apiServices => apiServices.map(api => this.mapToModel(api))),
      tap((services) => this._services.set(services)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getServiceById(id: string): Observable<ServiceResponse> {
    return this.servicesApi.getById(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  createService(service: ServiceRequest): Observable<ServiceResponse> {
    const apiRequest: ApiServiceRequest = {
      ...service,
      category: service.category as any
    };

    return this.servicesApi.create(apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newService) => {
        this._services.update((current) => [...current, newService]);
      })
    );
  }

  updateService(id: string, service: ServiceRequest): Observable<ServiceResponse> {
    const apiRequest: ApiServiceRequest = {
      ...service,
      category: service.category as any
    };

    return this.servicesApi.update(id, apiRequest).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedService) => {
            this._services.update(current => 
                current.map(s => s.id === id ? updatedService : s)
            );
        })
    );
  }

  /**
   * Toggles the active status of a service (US-019)
   */
  toggleServiceStatus(id: string): Observable<ServiceResponse> {
    return this.servicesApi.toggleStatus(id).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedService) => {
            this._services.update(current => 
                current.map(s => s.id === id ? updatedService : s)
            );
        })
    );
  }

  deleteService(id: string): Observable<void> {
    return this.servicesApi._delete(id).pipe(
      tap(() => {
        this._services.update((current) => current.filter((a) => a.id !== id));
      })
    );
  }

  refreshServices(): Observable<ServiceResponse[]> {
    return this.getServices();
  }

  private getVendorUuid(): string | null {
    const user = this.authService.currentUser();
    return user ? user.id : null;
  }

  private mapToModel(api: ApiServiceResponse): ServiceResponse {
    return {
      id: api.id || '',
      name: api.name || '',
      category: api.category as any || 'STREAMING',
      priceProfile: api.priceProfile || 0,
      priceComplete: api.priceComplete || 0,
      durationDays: api.durationDays || 30,
      maxProfiles: api.maxProfiles || 0,
      isActive: api.isActive ?? true,
      description: api.description || '',
      imageUrl: api.imageUrl || '',
      details: api.details || '',
      createdAt: api.createdAt || ''
    };
  }
}
