import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  LoyaltyPointsApiService,
  PointsSummaryResponse,
  PointsMovementsPageResponse,
  PointsMovementResponse,
  AdjustPointsRequest
} from '@neversion/api-client';

@Injectable({ providedIn: 'root' })
export class ClientPointsService {
  private readonly loyaltyApi = inject(LoyaltyPointsApiService);

  getSummary(clientUuid: string): Observable<PointsSummaryResponse> {
    return this.loyaltyApi.getSummaryVendorClientPoints(clientUuid);
  }

  getMovements(clientUuid: string, page: number, size: number): Observable<PointsMovementsPageResponse> {
    return this.loyaltyApi.getMovementsVendorClientPoints(clientUuid, page, size);
  }

  adjustPoints(clientUuid: string, request: AdjustPointsRequest): Observable<PointsMovementResponse> {
    return this.loyaltyApi.adjustVendorClientPoints(clientUuid, request);
  }
}
