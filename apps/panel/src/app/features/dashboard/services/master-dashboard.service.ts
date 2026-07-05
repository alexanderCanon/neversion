import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';
import {
  ProductSummary,
  AccountGroup,
  ProfileItem,
  ProfileSubscription,
  ProfileCustomer
} from '@neversion/models';
import {
  DashboardApiService,
  ExpiringSubscriptionResult,
  ExpiringAccountResult,
  ExpiringAccountsKpiResult,
  NotificationsApiService,
  ProfitMarginsResult
} from '@neversion/api-client';

export interface VendorKpiMetrics {
  activeClientsCount: number;
  successfulRenewalsCount: number;
  grossProfit: number;
  currency: string;
  expiringTodayCount: number;
  expiringTomorrowCount: number;
  expiringThisWeekCount: number;
}

export interface VendorDashboardKpis {
  metrics: VendorKpiMetrics;
  expiringToday: ExpiringSubscriptionResult[];
  expiringTomorrow: ExpiringSubscriptionResult[];
  expiringThisWeek: ExpiringSubscriptionResult[];
  expiringAccounts: ExpiringAccountsKpiResult;
}

@Injectable({ providedIn: 'root' })
export class MasterDashboardService {
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly notificationsApi = inject(NotificationsApiService);

  getProductsSummary(category = 'streaming'): Observable<ProductSummary[]> {
    return this.dashboardApi.getProductsSummaryDashboard(
      category as 'streaming',
      'body',
      false,
    ).pipe(
      map((products) => products.map((product) => ({
        productId: product.productId ?? '',
        productName: product.productName ?? '',
        category: product.category ?? '',
        totalAccounts: product.totalAccounts ?? 0
      })))
    );
  }

  getAccountsByProduct(productId: string): Observable<AccountGroup[]> {
    return this.dashboardApi.getAccountsByProductDashboard(
      productId,
      'body',
      false,
    ).pipe(
      map((accounts) => accounts.map((account) => ({
        accountId: account.accountId ?? '',
        email: account.email ?? '',
        password: account.password ?? '',
        cutOffDate: account.cutOffDate ?? '',
        accountType: (account.accountType ?? 'FAMILY') as AccountGroup['accountType'],
        accountStatus: (account.accountStatus ?? 'AVAILABLE') as AccountGroup['accountStatus'],
        maxProfiles: account.maxProfiles ?? 0,
        occupiedProfiles: account.occupiedProfiles ?? 0,
        availableProfiles: account.availableProfiles ?? 0,
        availability: (account.availability ?? 'NO_AVAILABILITY') as AccountGroup['availability']
      })))
    );
  }

  getProfilesByAccount(accountId: string): Observable<ProfileItem[]> {
    return this.dashboardApi.getProfilesByAccountDashboard(
      accountId,
      'body',
      false,
    ).pipe(
      map((profiles) => profiles.map((profile) => ({
        profileId: profile.profileId ?? '',
        profileName: profile.profileName ?? null,
        pin: profile.pin ?? null,
        profileStatus: (profile.profileStatus ?? 'AVAILABLE') as ProfileItem['profileStatus'],
        subscription: profile.subscription
          ? {
              subscriptionId: profile.subscription.subscriptionId ?? '',
              startDate: profile.subscription.startDate ?? '',
              endDate: profile.subscription.endDate ?? '',
              status: (profile.subscription.status ?? 'ACTIVE') as ProfileSubscription['status'],
              customer: {
                id: profile.subscription.customer?.id ?? '',
                name: profile.subscription.customer?.name ?? '',
                phone: profile.subscription.customer?.phone ?? '',
                type: (profile.subscription.customer?.type ?? 'CLIENT') as ProfileCustomer['type']
              }
            }
          : null
       })))
    );
  }

  getVendorKpis(): Observable<VendorDashboardKpis> {
    return forkJoin({
      expiring: this.dashboardApi.getExpiringSubscriptionsDashboard(),
      expiringAccounts: this.dashboardApi.getExpiringAccountsDashboard(),
      activeClients: this.dashboardApi.getActiveClientsDashboard(),
      successfulRenewals: this.dashboardApi.getSuccessfulRenewalsDashboard(),
      grossProfit: this.dashboardApi.getGrossProfitDashboard()
    }).pipe(
      map(({ expiring, expiringAccounts, activeClients, successfulRenewals, grossProfit }) => {
        const expiringToday = expiring.today ?? [];
        const expiringTomorrow = expiring.tomorrow ?? [];
        const expiringThisWeek = expiring.thisWeek ?? [];

        return {
          metrics: {
            activeClientsCount: activeClients.activeClientsCount ?? 0,
            successfulRenewalsCount: successfulRenewals.successfulRenewalsCount ?? 0,
            grossProfit: grossProfit.grossProfit ?? 0,
            currency: grossProfit.currency ?? 'GTQ',
            expiringTodayCount: expiringToday.length,
            expiringTomorrowCount: expiringTomorrow.length,
            expiringThisWeekCount: expiringThisWeek.length
          },
          expiringToday,
          expiringTomorrow,
          expiringThisWeek,
          expiringAccounts
        };
      })
    );
  }

  sendManualReminder(subscriptionId: string): Observable<void> {
    return this.notificationsApi.sendManualReminderNotification(subscriptionId, 'body', false).pipe(
      map(() => void 0)
    );
  }

  getAccountProfitMargins(year?: number, month?: number): Observable<ProfitMarginsResult> {
    return this.dashboardApi.getAccountProfitMarginsDashboard(year, month, 'body', false);
  }
}
