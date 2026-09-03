import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map, catchError } from 'rxjs';
import {
  SubscriptionsApiService,
  ProfilesApiService,
  SubscriptionResponse,
  SubscriptionDetailResponse,
  CreateManualSubscriptionRequest,
  BatchCreateManualSubscriptionRequest,
  BatchCreateSubscriptionsResponse,
  DetectExpiredSubscriptionsResponse,
  ProfileResponse,
  ChangeProfileStatusRequest
} from '@alexandercanon/api-client-angular';
import { SubscriptionsFilter } from '@neversion/models';

@Injectable({ providedIn: 'root' })
export class SubscriptionsService {
  private readonly subscriptionsApi = inject(SubscriptionsApiService);
  private readonly profilesApi = inject(ProfilesApiService);

  private readonly _subscriptions = signal<SubscriptionResponse[]>([]);
  readonly subscriptions = this._subscriptions.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getSubscriptions(filter?: SubscriptionsFilter): Observable<SubscriptionResponse[]> {
    this._isLoading.set(true);
    return this.subscriptionsApi.listSubscriptionsSubscription(
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
    return this.subscriptionsApi.getByIdSubscription(id);
  }

  createManualSubscription(request: CreateManualSubscriptionRequest): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.assignSubscription(request).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  createBatchSubscriptions(request: BatchCreateManualSubscriptionRequest): Observable<BatchCreateSubscriptionsResponse> {
    return this.subscriptionsApi.batchCreateSubscription(request).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  renewSubscription(id: string): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.renewSubscription(id).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  /**
   * Late renewal with an explicit due date (past grace).
   */
  renewSubscriptionToDate(id: string, newDueDate: string): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.renewSubscription(id, newDueDate).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  cancelSubscription(id: string): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.cancelSubscription(id).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  suspendSubscription(id: string): Observable<SubscriptionResponse> {
    return this.subscriptionsApi.suspendSubscription(id).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  changeProfileStatus(profileId: string, status: ChangeProfileStatusRequest.StatusEnum): Observable<ProfileResponse> {
    return this.profilesApi.changeStatusProfile(profileId, { status }).pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  detectExpiredSubscriptions(): Observable<DetectExpiredSubscriptionsResponse> {
    return this.subscriptionsApi.detectExpiredSubscription().pipe(
      tap(() => this.refreshSubscriptions().subscribe())
    );
  }

  refreshSubscriptions(): Observable<SubscriptionResponse[]> {
    return this.getSubscriptions();
  }

  private normalizeSubscriptionsResponse(response: SubscriptionResponse[] | { content?: SubscriptionResponse[] }): SubscriptionResponse[] {
    if (Array.isArray(response)) {
      return response;
    }
    return response.content ?? [];
  }
}
