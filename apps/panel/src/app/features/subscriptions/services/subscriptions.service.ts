import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, finalize } from 'rxjs';
import { CreateSubscriptionRequest, SubscriptionResponse, SubscriptionDashboardDTO, SubscriptionsFilter, SubscriptionStatus } from '../models/subscription.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SubscriptionsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  private readonly _subscriptions = signal<SubscriptionResponse[]>([]);
  readonly subscriptions = this._subscriptions.asReadonly();

  private readonly _dashboardData = signal<SubscriptionDashboardDTO[]>([]);
  readonly dashboardData = this._dashboardData.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getSubscriptions(filter?: SubscriptionsFilter): Observable<SubscriptionResponse[]> {
    let params = new HttpParams();
    if (filter?.status) params = params.set('status', filter.status);
    if (filter?.clientId) params = params.set('clientId', filter.clientId);
    if (filter?.profileId) params = params.set('profileId', filter.profileId);

    this._isLoading.set(true);
    return this.http.get<SubscriptionResponse[]>(`${this.baseUrl}/subscriptions`, { params }).pipe(
      tap((subscriptions) => this._subscriptions.set(subscriptions)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getDashboard(): Observable<SubscriptionDashboardDTO[]> {
    return this.http.get<SubscriptionDashboardDTO[]>(`${this.baseUrl}/subscriptions/dashboard`).pipe(
      tap((data) => this._dashboardData.set(data))
    );
  }

  createSubscription(subscription: CreateSubscriptionRequest): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${this.baseUrl}/subscriptions`, subscription).pipe(
      tap((newSub) => {
        this._subscriptions.update((current) => [...current, newSub]);
      })
    );
  }

  terminateSubscription(id: string): Observable<SubscriptionResponse> {
    return this.http.put<SubscriptionResponse>(`${this.baseUrl}/subscriptions/${id}/terminate`, {});
  }

  suspendSubscription(id: string): Observable<SubscriptionResponse> {
    return this.http.put<SubscriptionResponse>(`${this.baseUrl}/subscriptions/${id}/suspend`, {});
  }

  refreshSubscriptions(): Observable<SubscriptionResponse[]> {
    return this.getSubscriptions();
  }
}
