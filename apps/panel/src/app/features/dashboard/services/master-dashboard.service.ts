import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, from, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import {
  ProductSummary,
  AccountGroup,
  ProfileItem,
  ProfileSubscription,
  ProfileCustomer
} from '@neversion/models';
import { NotificationsApiService } from '@neversion/api-client';
import { SupabaseService } from '../../../core/services/supabase.service';

export interface ExpiringSubscriptionResult {
  subscriptionId?: string;
  clientName?: string;
  clientPhone?: string;
  serviceName?: string;
  profileName?: string;
  paymentDueDate?: string;
  status?: string;
  vendorExternalId?: string;
}

export interface ExpiringAccountResult {
  accountId?: string;
  email?: string;
  accountEmail?: string;
  serviceName?: string;
  activeProfilesCount?: number;
  maxProfiles?: number;
  renewalDate?: string;
  status?: string;
  vendorExternalId?: string;
}


export interface ExpiringAccountsKpiResult {
  totalAccounts?: number;
  expiring3Days?: number;
  expiring7Days?: number;
  today?: ExpiringAccountResult[];
  tomorrow?: ExpiringAccountResult[];
  thisWeek?: ExpiringAccountResult[];
  accounts?: ExpiringAccountResult[];
}

export interface ServiceProfitSummaryResult {
  serviceId?: string;
  serviceName?: string;
  activeAccounts?: number;
  activeAccountsCount?: number;
  totalProfilesSold?: number;
  totalRevenue?: number;
  totalCost?: number;
  totalAllocatedCost?: number;
  profit?: number;
  totalProfit?: number;
  marginPct?: number;
  avgMarginPct?: number;
}

export interface AccountProfitMarginResult {
  accountUuid?: string;
  accountId?: string;
  email?: string;
  serviceName?: string;
  profilesSold?: number;
  maxProfiles?: number;
  saleMode?: string;
  totalRevenue?: number;
  totalDiscount?: number;
  allocatedCost?: number;
  accountCost?: number;
  profit?: number;
  marginPct?: number;
}

export interface ProfitMarginsResult {
  grandTotal?: {
    totalRevenue?: number;
    totalDiscount?: number;
    allocatedCost?: number;
    totalAllocatedCost?: number;
    profit?: number;
    totalProfit?: number;
    marginPct?: number;
    avgMarginPct?: number;
  };
  serviceSummaries?: ServiceProfitSummaryResult[];
  accountMargins?: AccountProfitMarginResult[];
}





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
  private readonly supabase = inject(SupabaseService);
  private readonly notificationsApi = inject(NotificationsApiService);

  getProductsSummary(category = 'streaming'): Observable<ProductSummary[]> {
    const promise = this.supabase.client
      .from('v_dashboard_products_summary')
      .select('*')
      .eq('category', category.toUpperCase());

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        return (data || []).map((product) => ({
          productId: product.product_id ?? '',
          productName: product.product_name ?? '',
          category: product.category ?? '',
          totalAccounts: product.total_accounts ?? 0
        }));
      }),
      catchError(err => {
        console.error('Error fetching products summary from PostgREST:', err);
        return throwError(() => err);
      })
    );
  }

  getAccountsByProduct(productId: string): Observable<AccountGroup[]> {
    const promise = this.supabase.client
      .from('v_dashboard_accounts_by_product')
      .select('*')
      .eq('product_id', productId);

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        return (data || []).map((account) => {
          const maxProfiles = account.max_profiles ?? 0;
          const occupiedProfiles = account.occupied_profiles ?? 0;
          const availableProfiles = Math.max(0, maxProfiles - occupiedProfiles);
          const accountType = (account.account_type ?? 'FAMILY') as AccountGroup['accountType'];

          let availability: AccountGroup['availability'] = 'NO_AVAILABILITY';
          if (accountType === 'INDIVIDUAL') {
            availability = 'INDIVIDUAL';
          } else if (availableProfiles === 0) {
            availability = 'NO_AVAILABILITY';
          } else if (availableProfiles === maxProfiles) {
            availability = 'COMPLETE';
          } else {
            availability = 'PARTIAL';
          }

          return {
            accountId: account.account_id ?? '',
            email: account.email ?? '',
            password: account.password ?? '',
            cutOffDate: account.cut_off_date ?? '',
            accountType,
            accountStatus: (account.account_status ?? 'AVAILABLE') as AccountGroup['accountStatus'],
            maxProfiles,
            occupiedProfiles,
            availableProfiles,
            availability
          };
        });
      }),
      catchError(err => {
        console.error('Error fetching accounts by product from PostgREST:', err);
        return throwError(() => err);
      })
    );
  }

  getProfilesByAccount(accountId: string): Observable<ProfileItem[]> {
    const promise = this.supabase.client
      .from('v_dashboard_profiles_by_account')
      .select('*')
      .eq('account_id', accountId);

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        return (data || []).map((profile): ProfileItem => ({
          profileId: profile.profile_id ?? '',
          profileName: profile.profile_name ?? null,
          pin: profile.pin ?? null,
          profileStatus: (profile.profile_status ?? 'AVAILABLE') as ProfileItem['profileStatus'],
          subscription: profile.sub_id
            ? {
                subscriptionId: profile.sub_id ?? '',
                startDate: profile.start_date ?? '',
                endDate: profile.end_date ?? '',
                status: (profile.sub_status ?? 'ACTIVE') as ProfileSubscription['status'],
                customer: {
                  id: profile.customer_id ?? '',
                  name: profile.customer_name ?? '',
                  phone: profile.customer_phone ?? '',
                  type: 'CLIENT' as ProfileCustomer['type']
                }
              }
            : null
        }));
      }),
      catchError(err => {
        console.error('Error fetching profiles by account from PostgREST:', err);
        return throwError(() => err);
      })
    );
  }

  getVendorKpis(): Observable<VendorDashboardKpis> {
    const expiringSubsPromise = this.supabase.client
      .from('v_dashboard_expiring_subscriptions')
      .select('*');

    const expiringAccsPromise = this.supabase.client
      .from('v_dashboard_expiring_accounts')
      .select('*');

    const activeClientsPromise = this.supabase.client
      .from('v_dashboard_active_clients_kpi')
      .select('*')
      .maybeSingle();

    const renewalsPromise = this.supabase.client
      .from('v_dashboard_successful_renewals_kpi')
      .select('*')
      .maybeSingle();

    const grossProfitPromise = this.supabase.client
      .from('v_dashboard_gross_profit_kpi')
      .select('*')
      .maybeSingle();

    return forkJoin({
      subs: from(expiringSubsPromise),
      accs: from(expiringAccsPromise),
      activeClients: from(activeClientsPromise),
      renewals: from(renewalsPromise),
      grossProfit: from(grossProfitPromise)
    }).pipe(
      map(({ subs, accs, activeClients, renewals, grossProfit }) => {
        const todayStr = new Date().toISOString().split('T')[0];
        const tomorrowDate = new Date();
        tomorrowDate.setDate(tomorrowDate.getDate() + 1);
        const tomorrowStr = tomorrowDate.toISOString().split('T')[0];

        const weekEndDate = new Date();
        weekEndDate.setDate(weekEndDate.getDate() + 7);
        const weekEndStr = weekEndDate.toISOString().split('T')[0];

        // Process expiring subscriptions
        const subsList = (subs.data || []).map(s => ({
          subscriptionId: s.subscription_id,
          clientName: s.client_name,
          clientPhone: s.client_phone,
          serviceName: s.service_name,
          profileName: s.profile_name,
          paymentDueDate: s.payment_due_date,
          status: s.status
        })) as ExpiringSubscriptionResult[];

        const expiringToday = subsList.filter(s => s.paymentDueDate === todayStr);
        const expiringTomorrow = subsList.filter(s => s.paymentDueDate === tomorrowStr);
        const expiringThisWeek = subsList.filter(s => !!s.paymentDueDate && s.paymentDueDate > tomorrowStr && s.paymentDueDate <= weekEndStr);

        // Process expiring accounts
        const accsList = (accs.data || []).map(a => ({
          accountId: a.account_id,
          serviceName: a.service_name,
          accountEmail: a.account_email,
          renewalDate: a.renewal_date,
          status: a.status
        })) as ExpiringAccountResult[];

        const expiringAccountsToday = accsList.filter(a => a.renewalDate === todayStr);
        const expiringAccountsTomorrow = accsList.filter(a => a.renewalDate === tomorrowStr);
        const expiringAccountsThisWeek = accsList.filter(a => !!a.renewalDate && a.renewalDate > tomorrowStr && a.renewalDate <= weekEndStr);

        const expiringAccounts: ExpiringAccountsKpiResult = {
          today: expiringAccountsToday,
          tomorrow: expiringAccountsTomorrow,
          thisWeek: expiringAccountsThisWeek
        };

        const activeClientsCount = activeClients.data?.active_clients_count ?? 0;
        const successfulRenewalsCount = renewals.data?.successful_renewals_count ?? 0;
        const grossProfitValue = grossProfit.data?.gross_profit ? Number(grossProfit.data.gross_profit) : 0;
        const currency = grossProfit.data?.currency ?? 'GTQ';

        return {
          metrics: {
            activeClientsCount,
            successfulRenewalsCount,
            grossProfit: grossProfitValue,
            currency,
            expiringTodayCount: expiringToday.length,
            expiringTomorrowCount: expiringTomorrow.length,
            expiringThisWeekCount: expiringThisWeek.length
          },
          expiringToday,
          expiringTomorrow,
          expiringThisWeek,
          expiringAccounts
        };
      }),
      catchError(err => {
        console.error('Error fetching vendor KPIs from PostgREST:', err);
        return throwError(() => err);
      })
    );
  }

  sendManualReminder(subscriptionId: string): Observable<void> {
    return this.notificationsApi.sendManualReminderNotification(subscriptionId, 'body', false).pipe(
      map(() => void 0)
    );
  }

  getAccountProfitMargins(year?: number, month?: number): Observable<ProfitMarginsResult> {
    const promise = this.supabase.client.rpc('rpc_get_account_profit_margins', {
      p_year: year ?? null,
      p_month: month ?? null
    });

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        const accounts = (data || []).map((row: any) => ({
          accountId: row.account_id,
          email: row.email,
          serviceName: row.service_name,
          saleMode: row.sale_mode,
          accountCost: Number(row.account_cost || 0),
          maxProfiles: Number(row.max_profiles || 1),
          profilesSold: Number(row.profiles_sold || 0),
          newRevenue: Number(row.new_revenue || 0),
          renewalRevenue: Number(row.renewal_revenue || 0),
          totalRevenue: Number(row.total_revenue || 0),
          totalDiscount: Number(row.total_discount || 0),
          allocatedCost: Number(row.allocated_cost || 0),
          profit: Number(row.profit || 0),
          profitMarginPct: Number(row.profit_margin_pct || 0)
        }));

        const totalRevenue = accounts.reduce((sum: number, acc: any) => sum + acc.totalRevenue, 0);
        const totalAllocatedCost = accounts.reduce((sum: number, acc: any) => sum + acc.allocatedCost, 0);
        const totalProfit = accounts.reduce((sum: number, acc: any) => sum + acc.profit, 0);
        const overallMarginPct = totalRevenue > 0 ? Number(((totalProfit / totalRevenue) * 100).toFixed(2)) : 0;

        return {
          accounts,
          summary: {
            totalRevenue,
            totalAllocatedCost,
            totalProfit,
            overallMarginPct
          }
        } as ProfitMarginsResult;
      }),
      catchError(err => {
        console.error('Error calling rpc_get_account_profit_margins from Supabase RPC:', err);
        return throwError(() => err);
      })
    );
  }
}
