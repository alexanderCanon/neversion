import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, of, map, catchError } from 'rxjs';
import {
  SubscriptionsApiService,
  SubscriptionResponse,
  SubscriptionDetailResponse,
  CreateManualSubscriptionRequest,
  BatchCreateManualSubscriptionRequest,
  BatchCreateSubscriptionsResponse,
  DetectExpiredSubscriptionsResponse
} from '@neversion/api-client';
import { SubscriptionsFilter } from '@neversion/models';
import { AuthService } from '../../../core/services/auth.service';

@Injectable({ providedIn: 'root' })
export class SubscriptionsService {
  private readonly subscriptionsApi = inject(SubscriptionsApiService);
  private readonly authService = inject(AuthService);

  private readonly _subscriptions = signal<SubscriptionResponse[]>([]);
  readonly subscriptions = this._subscriptions.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getSubscriptions(filter?: SubscriptionsFilter): Observable<SubscriptionResponse[]> {
    const vendorUuid = this.authService.currentVendorUuid();
    if (!vendorUuid) return of([]);

    this._isLoading.set(true);
    return this.subscriptionsApi.listByVendor(
      vendorUuid,
      filter?.serviceId,
      filter?.status as 'ACTIVE' | 'PENDING' | 'SUSPENDED' | 'CANCELLED',
      'body',
      false,
    ).pipe(
      map((response) => this.normalizeSubscriptionsResponse(response)),
      tap((subscriptions) => this._subscriptions.set(subscriptions)),
      catchError((error) => {
        this._subscriptions.set([]);
        throw error;
      }),
      finalize(() => this._isLoading.set(false))
    );
  }

  getSubscriptionDetail(id: string): Observable<SubscriptionDetailResponse> {
    return this.subscriptionsApi.getById4(id);
  }

  createManualSubscription(request: CreateManualSubscriptionRequest): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.assign(request).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  createBatchSubscriptions(request: BatchCreateManualSubscriptionRequest): Observable<BatchCreateSubscriptionsResponse> {
    return this.subscriptionsApi.batchCreate(request).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  renewSubscription(id: string): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.renew(id);
  }

  cancelSubscription(id: string): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.cancel(id);
  }

  suspendSubscription(id: string): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.suspend(id);
  }

  detectExpiredSubscriptions(): Observable<DetectExpiredSubscriptionsResponse> {
    return this.subscriptionsApi.detectExpired();
  }

  refreshSubscriptions(): Observable<SubscriptionResponse[]> {
    return this.getSubscriptions();
  }

  private normalizeSubscriptionsResponse(response: unknown): SubscriptionResponse[] {
    if (Array.isArray(response)) {
      return response;
    }

    if (response && typeof response === 'object' && Array.isArray((response as { content?: unknown }).content)) {
      return (response as { content: SubscriptionResponse[] }).content;
    }

    return [];
  }
}
