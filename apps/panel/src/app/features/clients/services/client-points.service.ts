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
    return this.loyaltyApi.getSummary(clientUuid);
  }

  getMovements(clientUuid: string, page: number, size: number): Observable<PointsMovementsPageResponse> {
    return this.loyaltyApi.getMovements(clientUuid, page, size);
  }

  adjustPoints(clientUuid: string, request: AdjustPointsRequest): Observable<PointsMovementResponse> {
    return this.loyaltyApi.adjust(clientUuid, request);
  }
}
