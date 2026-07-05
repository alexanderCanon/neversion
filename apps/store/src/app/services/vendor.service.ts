import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { VendorsPublicApiService, VendorPublicResponse } from '@neversion/api-client';
import { runtimeConfig } from '../config/runtime-config';

export interface DiscountTier {
  count: number;
  discountPct: number;
}

export interface DiscountConfig {
  minItems: number;
  maxItems: number;
  roundTo: number;
  tiers: DiscountTier[];
}

@Injectable({
  providedIn: 'root'
})
export class VendorService {
  private readonly vendorsPublicApi = inject(VendorsPublicApiService);

  private vendorSubject = new BehaviorSubject<VendorPublicResponse | null>(null);
  vendor$ = this.vendorSubject.asObservable();

  private discountCfgSubject = new BehaviorSubject<DiscountConfig | null>(null);
  discountCfg$ = this.discountCfgSubject.asObservable();

  private loaded = false;

  /** Loads vendor public info (including discountCfg) once at app init. */
  loadVendor(): Observable<VendorPublicResponse | null> {
    if (this.loaded) {
      return of(this.vendorSubject.value);
    }

    return this.vendorsPublicApi.getByUuidVendorPublic(runtimeConfig.storeVendorUuid).pipe(
      tap(vendor => {
        this.vendorSubject.next(vendor);
        this.discountCfgSubject.next(this.parseDiscountCfg(vendor?.discountCfg));
        this.loaded = true;
      }),
      catchError(err => {
        console.error('Error loading vendor info:', err);
        this.loaded = true;
        return of(null);
      })
    );
  }

  getVendor(): VendorPublicResponse | null {
    return this.vendorSubject.value;
  }

  getDiscountConfig(): DiscountConfig | null {
    return this.discountCfgSubject.value;
  }

  private parseDiscountCfg(json?: string): DiscountConfig | null {
    if (!json || !json.trim()) return null;

    try {
      const raw = JSON.parse(json);
      const tiers: DiscountTier[] = (raw.tiers || []).map((t: any) => ({
        count: t.count ?? t.from,
        discountPct: t.discount_pct ?? 0
      }));

      return {
        minItems: raw.min_items ?? 2,
        maxItems: raw.max_items ?? 4,
        roundTo: raw.round_to ?? 5,
        tiers
      };
    } catch {
      console.error('Failed to parse discount_cfg:', json);
      return null;
    }
  }
}
