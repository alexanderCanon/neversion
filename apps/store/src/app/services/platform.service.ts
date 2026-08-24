import { Injectable, inject } from '@angular/core';
import { Observable, from, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ServiceResponse } from '@alexandercanon/api-client-angular';
import { SupabaseService } from './supabase.service';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class PlatformService {
  private readonly supabase = inject(SupabaseService);

  /**
   * Fetch only ACTIVE streaming services for the storefront via PostgREST view
   */
  public getPlatforms(): Observable<ServiceResponse[]> {
    const promise = this.supabase.client
      .from('v_store_services')
      .select('*')
      .eq('vendor_uuid', runtimeConfig.storeVendorUuid);

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        return (data || []).map(item => ({
          id: item.service_uuid,
          uuid: item.service_uuid,
          name: item.service_name,
          category: item.category,
          description: item.description,
          imageUrl: item.image_url,
          priceProfile: item.price_profile ? Number(item.price_profile) : undefined,
          priceFull: item.price_full ? Number(item.price_full) : undefined,
          durationDays: item.duration_days,
          maxProfiles: item.max_profiles
        })) as ServiceResponse[];
      }),
      catchError(err => {
        console.error('Error fetching storefront services from Supabase view:', err);
        return throwError(() => err);
      })
    );
  }

  public getPlatformById(id: string): Observable<ServiceResponse> {
    const promise = this.supabase.client
      .from('v_store_services')
      .select('*')
      .eq('service_uuid', id)
      .maybeSingle();

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        if (!data) throw new Error(`Service not found for id: ${id}`);
        return {
          id: data.service_uuid,
          uuid: data.service_uuid,
          name: data.service_name,
          category: data.category,
          description: data.description,
          imageUrl: data.image_url,
          priceProfile: data.price_profile ? Number(data.price_profile) : undefined,
          priceFull: data.price_full ? Number(data.price_full) : undefined,
          durationDays: data.duration_days,
          maxProfiles: data.max_profiles
        } as ServiceResponse;
      }),
      catchError(err => {
        console.error('Error fetching service details from Supabase view:', err);
        return throwError(() => err);
      })
    );
  }
}
