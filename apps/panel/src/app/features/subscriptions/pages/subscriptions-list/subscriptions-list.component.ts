import { Component, OnInit, inject, signal, computed, ViewChild } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { ServicesDataService } from '../../../services/services/services-data.service';
import { SubscriptionResponse } from '@neversion/api-client';
import { SubscriptionStatus, SubscriptionsFilter } from '@neversion/models';
import { SubscriptionFormComponent } from '../../components/subscription-form/subscription-form.component';
import { BatchSubscriptionFormComponent } from '../../components/batch-subscription-form/batch-subscription-form.component';
import { ManualAssignmentModalComponent } from '../../../assignments/components/manual-assignment-modal/manual-assignment-modal.component';
import { ProfitMarginsComponent } from '../../components/profit-margins/profit-margins.component';
import { ToastService } from '../../../../core/services/toast.service';
import { getSubscriptionStatusClass, getSubscriptionStatusLabel } from '@neversion/utils';

type ViewMode = 'table' | 'grouped';
type GroupMode = 'service' | 'client' | 'date';
type PageMode = 'subscriptions' | 'margins';

interface GroupStats {
  total: number;
  active: number;
  suspended: number;
  expired: number;
  cancelled: number;
}

interface SubscriptionGroup {
  key: string;
  label: string;
  icon: string;
  items: SubscriptionResponse[];
  stats: GroupStats;
}

@Component({
  selector: 'app-subscriptions-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SubscriptionFormComponent, BatchSubscriptionFormComponent, ManualAssignmentModalComponent, ProfitMarginsComponent],
  templateUrl: './subscriptions-list.component.html',
  styleUrl: './subscriptions-list.component.scss'
  })
export class SubscriptionsListComponent implements OnInit {
  @ViewChild('subscriptionForm') subscriptionForm!: SubscriptionFormComponent;
  @ViewChild('batchForm') batchForm!: BatchSubscriptionFormComponent;
  @ViewChild('manualModal') manualModal!: ManualAssignmentModalComponent;

  private readonly subscriptionsService = inject(SubscriptionsService);
  private readonly servicesService = inject(ServicesDataService);
  private readonly toastService = inject(ToastService);
  private readonly route = inject(ActivatedRoute);

  readonly subscriptions = this.subscriptionsService.subscriptions;
  readonly isLoading = this.subscriptionsService.isLoading;
  readonly services = this.servicesService.services;

  filterStatus = signal<SubscriptionStatus | ''>('');
  filterServiceId = signal<string | ''>('');
  filterClientId = signal<string | ''>('');
  currentPage = signal(1);
  pageSize = 10;

  viewMode = signal<ViewMode>('grouped');
  groupMode = signal<GroupMode>('service');
  collapsedGroups = signal<Set<string>>(new Set());
  pageMode = signal<PageMode>('subscriptions');

  readonly filteredSubscriptions = computed(() => {
    let result = this.subscriptions();

    const status = this.filterStatus();
    if (status) {
        result = result.filter(s => s.status === status);
    }

    const clientId = this.filterClientId();
    if (clientId) {
        result = result.filter(s => s.clientId === clientId);
    }

    return result;
  });

  readonly paginatedSubscriptions = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredSubscriptions().slice(start, start + this.pageSize);
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.filteredSubscriptions().length / this.pageSize) || 1
  );

  readonly statusOptions: SubscriptionStatus[] = Object.values(SubscriptionStatus);

  private computeStats(items: SubscriptionResponse[]): GroupStats {
    return {
      total: items.length,
      active: items.filter(s => s.status === 'ACTIVE').length,
      suspended: items.filter(s => s.status === 'SUSPENDED').length,
      expired: items.filter(s => (s.status as string) === 'EXPIRED').length,
      cancelled: items.filter(s => s.status === 'CANCELLED').length,
    };
  }

  readonly serviceGroups = computed<SubscriptionGroup[]>(() => {
    const subs = this.filteredSubscriptions();
    const map = new Map<string, SubscriptionResponse[]>();
    for (const sub of subs) {
      const key = sub.serviceName ?? 'Sin servicio';
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(sub);
    }
    return Array.from(map.entries())
      .map(([key, items]) => ({
        key,
        label: key,
        icon: 'bi-collection-fill',
        items: items.sort((a, b) => (a.clientName ?? '').localeCompare(b.clientName ?? '')),
        stats: this.computeStats(items),
      }))
      .sort((a, b) => b.stats.active - a.stats.active || b.stats.total - a.stats.total);
  });

  readonly clientGroups = computed<SubscriptionGroup[]>(() => {
    const subs = this.filteredSubscriptions();
    const map = new Map<string, SubscriptionResponse[]>();
    for (const sub of subs) {
      const key = sub.clientName ?? 'Sin cliente';
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(sub);
    }
    return Array.from(map.entries())
      .map(([key, items]) => ({
        key,
        label: key,
        icon: 'bi-person-circle',
        items: items.sort((a, b) => (a.serviceName ?? '').localeCompare(b.serviceName ?? '')),
        stats: this.computeStats(items),
      }))
      .sort((a, b) => b.stats.active - a.stats.active || b.stats.total - a.stats.total);
  });

  readonly dateGroups = computed<SubscriptionGroup[]>(() => {
    const subs = this.filteredSubscriptions();
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const weekEnd = new Date(today);
    weekEnd.setDate(weekEnd.getDate() + 7);
    const monthEnd = new Date(today.getFullYear(), today.getMonth() + 1, 0);

    const buckets: Record<string, SubscriptionResponse[]> = {
      'Vencen hoy': [],
      'Vencen mañana': [],
      'Esta semana': [],
      'Este mes': [],
      'Vencidas': [],
      'Sin fecha': [],
    };

    for (const sub of subs) {
      if (!sub.paymentDueDate) {
        buckets['Sin fecha'].push(sub);
        continue;
      }
      const due = new Date(sub.paymentDueDate);
      const dueDay = new Date(due.getFullYear(), due.getMonth(), due.getDate());

      if (sub.status === 'CANCELLED') {
        buckets['Sin fecha'].push(sub);
      } else if (dueDay < today && (sub.status as string) !== 'EXPIRED') {
        buckets['Vencidas'].push(sub);
      } else if (dueDay.getTime() === today.getTime()) {
        buckets['Vencen hoy'].push(sub);
      } else if (dueDay.getTime() === tomorrow.getTime()) {
        buckets['Vencen mañana'].push(sub);
      } else if (dueDay <= weekEnd) {
        buckets['Esta semana'].push(sub);
      } else if (dueDay <= monthEnd) {
        buckets['Este mes'].push(sub);
      } else {
        buckets['Sin fecha'].push(sub);
      }
    }

    const iconMap: Record<string, string> = {
      'Vencen hoy': 'bi-exclamation-circle-fill',
      'Vencen mañana': 'bi-clock-fill',
      'Esta semana': 'bi-calendar-week-fill',
      'Este mes': 'bi-calendar-month-fill',
      'Vencidas': 'bi-x-circle-fill',
      'Sin fecha': 'bi-calendar-x',
    };

    return Object.entries(buckets)
      .filter(([, items]) => items.length > 0)
      .map(([key, items]) => ({
        key,
        label: key,
        icon: iconMap[key] ?? 'bi-calendar',
        items: items.sort((a, b) => (a.paymentDueDate ?? '').localeCompare(b.paymentDueDate ?? '')),
        stats: this.computeStats(items),
      }));
  });

  readonly activeGroups = computed<SubscriptionGroup[]>(() => {
    const mode = this.groupMode();
    if (mode === 'service') return this.serviceGroups();
    if (mode === 'client') return this.clientGroups();
    return this.dateGroups();
  });

  readonly totalStats = computed<GroupStats>(() => this.computeStats(this.filteredSubscriptions()));

  ngOnInit(): void {
    this.servicesService.getServices({ isActive: true }).subscribe();
    this.route.queryParams.subscribe(params => {
        if (params['status']) {
            this.filterStatus.set(params['status'] as SubscriptionStatus);
        }
        if (params['serviceId']) {
            this.filterServiceId.set(params['serviceId']);
        }
        if (params['clientId']) {
            this.filterClientId.set(params['clientId']);
        }
        this.loadSubscriptions();
    });
  }

  loadSubscriptions(): void {
    const filter: SubscriptionsFilter = {};
    const status = this.filterStatus();
    if (status) filter.status = status;
    const serviceId = this.filterServiceId();
    if (serviceId) filter.serviceId = serviceId;
    const clientId = this.filterClientId();
    if (clientId) filter.clientId = clientId;

    this.subscriptionsService.getSubscriptions(filter).subscribe();
  }

  detectExpired(): void {
    if (confirm('¿Deseas ejecutar la detección manual de suscripciones vencidas? Esto actualizará los estados en todo el sistema.')) {
      this.subscriptionsService.detectExpiredSubscriptions().subscribe({
        next: (resp) => {
          this.toastService.success(`Proceso completado. ${resp.suspendedCount} suscripciones procesadas.`);
          this.loadSubscriptions();
        },
        error: () => this.toastService.error('Error al ejecutar el proceso de detección.')
      });
    }
  }

  onSubscriptionCreated(): void {
    this.loadSubscriptions();
  }

  onBatchCreated(): void {
    this.loadSubscriptions();
  }

  onManualAssignmentCreated(): void {
    this.loadSubscriptions();
  }

  cancelSubscription(subscription: SubscriptionResponse): void {
    if (confirm(`¿Está seguro de que desea revocar el acceso de la suscripción de ${subscription.clientName || 'este cliente'}?`)) {
      this.subscriptionsService.cancelSubscription(subscription.id!).subscribe({
        next: () => {
          this.toastService.success('Suscripción revocada y perfil liberado');
          this.loadSubscriptions();
        },
      });
    }
  }

  suspendSubscription(subscription: SubscriptionResponse): void {
    if (confirm(`¿Seguro que deseas suspender la suscripción ${subscription.id!.slice(0, 8)}...?`)) {
      this.subscriptionsService.suspendSubscription(subscription.id!).subscribe({
        next: () => {
          this.toastService.success('Suscripción suspendida');
          this.loadSubscriptions();
        },
      });
    }
  }

  onStatusFilterChange(status: SubscriptionStatus | ''): void {
    this.filterStatus.set(status);
    this.currentPage.set(1);
    this.loadSubscriptions();
  }

  onServiceFilterChange(serviceId: string | ''): void {
    this.filterServiceId.set(serviceId);
    this.currentPage.set(1);
    this.loadSubscriptions();
  }

  setViewMode(mode: ViewMode): void {
    this.viewMode.set(mode);
  }

  setPageMode(mode: PageMode): void {
    this.pageMode.set(mode);
  }

  setGroupMode(mode: GroupMode): void {
    this.groupMode.set(mode);
    this.collapsedGroups.set(new Set());
  }

  toggleGroup(key: string): void {
    const current = new Set(this.collapsedGroups());
    if (current.has(key)) {
      current.delete(key);
    } else {
      current.add(key);
    }
    this.collapsedGroups.set(current);
  }

  isGroupCollapsed(key: string): boolean {
    return this.collapsedGroups().has(key);
  }

  prevPage(): void {
    if (this.currentPage() > 1) {
      this.currentPage.update((p) => p - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages()) {
      this.currentPage.update((p) => p + 1);
    }
  }

  getStatusClass(status: string): string {
    return getSubscriptionStatusClass(status);
  }

  getStatusLabel(status: string): string {
    return getSubscriptionStatusLabel(status);
  }

  trackBySubscriptionId(index: number, subscription: SubscriptionResponse): string {
    return subscription.id || '';
  }

  trackByGroupKey(index: number, group: SubscriptionGroup): string {
    return group.key;
  }
}
