import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ServicesApiService, ServiceResponse } from '@neversion/api-client';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class PlatformService {
  private readonly servicesApi = inject(ServicesApiService);

  /**
   * Fetch only ACTIVE services for the storefront (US-021)
   */
  public getPlatforms(): Observable<ServiceResponse[]> {
    return this.servicesApi.listActiveService(runtimeConfig.storeVendorUuid).pipe(
      catchError(err => {
        console.error('Error fetching storefront services:', err);
        return throwError(() => err);
      })
    );
  }

  public getPlatformById(id: string): Observable<ServiceResponse> {
    return this.servicesApi.getByIdService(id).pipe(
      catchError(err => {
        console.error('Error fetching service details:', err);
        return throwError(() => err);
      })
    );
  }

}
