import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map, of } from 'rxjs';
import { 
    ServicesApiService, 
    ServiceRequest as ApiServiceRequest, 
    ServiceResponse as ApiServiceResponse 
} from '@neversion/api-client';
import { ServiceRequest, ServiceResponse, ServicesFilter } from '@neversion/models';
import { AuthService } from '../../../core/services/auth.service';

interface ApiServicesPageResponse {
  content?: ApiServiceResponse[];
}

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
    const vendorUuid = this.authService.currentVendorUuid();
    if (!vendorUuid) return of([]);

    this._isLoading.set(true);
    return this.servicesApi.listByVendorService(
      vendorUuid,
      filter?.category as unknown as Parameters<typeof this.servicesApi.listByVendorService>[1],
      filter?.isActive,
      'body',
      false,
    ).pipe(
      map((apiServices) => this.normalizeServicesResponse(apiServices).map(api => this.mapToModel(api))),
      tap((services: ServiceResponse[]) => this._services.set(services)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getServiceById(id: string): Observable<ServiceResponse> {
    return this.servicesApi.getByIdService(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  createService(service: ServiceRequest): Observable<ServiceResponse> {
    const apiRequest: ApiServiceRequest = {
      ...service,
      category: service.category as unknown as ApiServiceRequest['category']
    };

    return this.servicesApi.createService(apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newService) => {
        this._services.update((current) => [...current, newService]);
      })
    );
  }

  updateService(id: string, service: ServiceRequest): Observable<ServiceResponse> {
    const apiRequest: ApiServiceRequest = {
      ...service,
      category: service.category as unknown as ApiServiceRequest['category']
    };

    return this.servicesApi.updateService(id, apiRequest).pipe(
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
    return this.servicesApi.toggleStatusService(id).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedService) => {
            this._services.update(current => 
                current.map(s => s.id === id ? updatedService : s)
            );
        })
    );
  }

  deleteService(id: string): Observable<void> {
    return this.servicesApi.deleteService(id).pipe(
      tap(() => {
        this._services.update((current) => current.filter((a) => a.id !== id));
      })
    );
  }

  refreshServices(): Observable<ServiceResponse[]> {
    return this.getServices();
  }

  private normalizeServicesResponse(response: ApiServiceResponse[] | ApiServicesPageResponse): ApiServiceResponse[] {
    if (Array.isArray(response)) {
      return response;
    }

    return response.content ?? [];
  }

  private mapToModel(api: ApiServiceResponse): ServiceResponse {
    return {
      id: api.id || '',
      name: api.name || '',
      category: (api.category as unknown as ServiceResponse['category']) || 'streaming',
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
