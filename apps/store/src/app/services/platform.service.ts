import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ServicesApiService, ServiceResponse } from '@neversion/api-client';

@Injectable({
  providedIn: 'root'
})
export class PlatformService {
  private readonly servicesApi = inject(ServicesApiService);

  // In EPIC-01/02 we use a fixed vendor for the store catalog. 
  // Multi-tenancy would resolve this from the domain/URL.
  private readonly VENDOR_UUID = '00000000-0000-0000-0000-000000000000';

  /**
   * Fetch only ACTIVE services for the storefront (US-021)
   */
  public getPlatforms(): Observable<ServiceResponse[]> {
    return this.servicesApi.listActive(this.VENDOR_UUID).pipe(
      catchError(err => {
        console.error('Error fetching storefront services:', err);
        return throwError(() => err);
      })
    );
  }

  public getPlatformById(id: string): Observable<ServiceResponse> {
    return this.servicesApi.getById(id).pipe(
      catchError(err => {
        console.error('Error fetching service details:', err);
        return throwError(() => err);
      })
    );
  }

}
