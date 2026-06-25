import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MasterDashboardService,
  VendorDashboardKpis,
  VendorKpiMetrics
} from './services/master-dashboard.service';
import { DashboardMetricsComponent } from './components/dashboard-metrics/dashboard-metrics.component';
import { ExpiringSubscriptionResult, ExpiringAccountResult } from '@neversion/api-client';

interface ExpiringSection {
  title: string;
  class: string;
  items: ExpiringSubscriptionResult[];
}

type GroupMode = 'date' | 'client' | 'service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, DashboardMetricsComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(MasterDashboardService);

  kpis = signal<VendorDashboardKpis | null>(null);
  isLoading = signal(false);
  hasError = signal(false);
  groupMode = signal<GroupMode>('date');
  remindingId = signal<string | null>(null);
  reminderSuccess = signal<string | null>(null);
  reminderError = signal<string | null>(null);

  metrics = computed<VendorKpiMetrics>(() => {
    return this.kpis()?.metrics ?? {
      activeClientsCount: 0,
      successfulRenewalsCount: 0,
      grossProfit: 0,
      currency: 'GTQ',
      expiringTodayCount: 0,
      expiringTomorrowCount: 0,
      expiringThisWeekCount: 0
    };
  });

  expiringSections = computed<ExpiringSection[]>(() => {
    const data = this.kpis();
    return [
      {
        title: 'Vencen hoy',
        class: 'due-today',
        items: data?.expiringToday ?? []
      },
      {
        title: 'Vencen mañana',
        class: 'due-tomorrow',
        items: data?.expiringTomorrow ?? []
      },
      {
        title: 'Vencen esta semana',
        class: 'due-week',
        items: data?.expiringThisWeek ?? []
      }
    ];
  });

  clientGroups = computed(() => {
    const data = this.kpis();
    const all = [
      ...(data?.expiringToday ?? []),
      ...(data?.expiringTomorrow ?? []),
      ...(data?.expiringThisWeek ?? [])
    ];
    const map = new Map<string, ExpiringSubscriptionResult[]>();
    for (const sub of all) {
      const key = sub.clientName ?? 'Sin cliente';
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(sub);
    }
    return Array.from(map.entries()).map(([clientName, items]) => ({ clientName, items }));
  });

  serviceGroups = computed(() => {
    const data = this.kpis();
    const all = [
      ...(data?.expiringToday ?? []),
      ...(data?.expiringTomorrow ?? []),
      ...(data?.expiringThisWeek ?? [])
    ];
    const map = new Map<string, ExpiringSubscriptionResult[]>();
    for (const sub of all) {
      const key = sub.serviceName ?? 'Sin servicio';
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(sub);
    }
    return Array.from(map.entries()).map(([serviceName, items]) => ({ serviceName, items }));
  });

  expiringAccountSections = computed(() => {
    const data = this.kpis()?.expiringAccounts;
    return [
      { title: 'Vencen hoy', class: 'due-today', items: data?.today ?? [] },
      { title: 'Vencen mañana', class: 'due-tomorrow', items: data?.tomorrow ?? [] },
      { title: 'Vencen esta semana', class: 'due-week', items: data?.thisWeek ?? [] }
    ];
  });

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading.set(true);
    this.hasError.set(false);
    this.dashboardService.getVendorKpis().subscribe({
      next: (kpis) => {
        this.kpis.set(kpis);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }

  trackExpiringSubscription(_index: number, subscription: ExpiringSubscriptionResult): string {
    return subscription.subscriptionId ?? `${subscription.clientName}-${subscription.paymentDueDate}`;
  }

  trackExpiringAccount(_index: number, account: ExpiringAccountResult): string {
    return account.accountId ?? `${account.serviceName}-${account.renewalDate}`;
  }

  setGroupMode(mode: GroupMode): void {
    this.groupMode.set(mode);
  }

  sendEmailReminder(subscription: ExpiringSubscriptionResult): void {
    const id = subscription.subscriptionId;
    if (!id) return;
    this.remindingId.set(id);
    this.reminderSuccess.set(null);
    this.reminderError.set(null);
    this.dashboardService.sendManualReminder(id).subscribe({
      next: () => {
        this.remindingId.set(null);
        this.reminderSuccess.set(`Recordatorio enviado a ${subscription.clientName ?? 'el cliente'}`);
        setTimeout(() => this.reminderSuccess.set(null), 4000);
      },
      error: () => {
        this.remindingId.set(null);
        this.reminderError.set('No se pudo enviar el recordatorio. Verifique que el cliente tenga email.');
        setTimeout(() => this.reminderError.set(null), 5000);
      }
    });
  }

  openWhatsApp(subscription: ExpiringSubscriptionResult): void {
    const phone = subscription.clientPhone?.replace(/\D/g, '') ?? '';
    if (!phone) return;
    const message = `Hola ${subscription.clientName ?? ''}, te recordamos que tu suscripción a ${subscription.serviceName ?? ''} vence el ${subscription.paymentDueDate ?? ''}. ¡Gracias!`;
    const url = `https://wa.me/${phone}?text=${encodeURIComponent(message)}`;
    window.open(url, '_blank');
  }

}
