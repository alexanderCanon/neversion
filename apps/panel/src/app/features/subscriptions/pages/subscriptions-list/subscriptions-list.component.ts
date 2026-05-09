import { Component, OnInit, inject, signal, computed, ViewChild } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { ServicesDataService } from '../../../services/services/services-data.service';
import { SubscriptionResponse } from '@neversion/api-client';
import { SubscriptionStatus, SubscriptionsFilter } from '@neversion/models';
import { SubscriptionFormComponent } from '../../components/subscription-form/subscription-form.component';
import { ManualAssignmentModalComponent } from '../../../assignments/components/manual-assignment-modal/manual-assignment-modal.component';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-subscriptions-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SubscriptionFormComponent, ManualAssignmentModalComponent],
  templateUrl: './subscriptions-list.component.html',
  styleUrl: './subscriptions-list.component.scss'
  })
export class SubscriptionsListComponent implements OnInit {
  @ViewChild('subscriptionForm') subscriptionForm!: SubscriptionFormComponent;
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

  readonly filteredSubscriptions = computed(() => {
    let result = this.subscriptions();
    
    const status = this.filterStatus();
    if (status) {
        result = result.filter(s => s.status === status);
    }

    const serviceId = this.filterServiceId();
    if (serviceId) {
        // Since we are now filtering at API level, this is mostly for signal consistency
        // but it doesn't hurt to keep it for local search.
        // However, some fields might not be present in SubscriptionResponse yet.
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
    switch (status?.toUpperCase()) {
      case 'ACTIVE':    return 'bg-success';
      case 'EXPIRED':   return 'bg-danger';
      case 'CANCELLED': return 'bg-secondary';
      case 'SUSPENDED': return 'bg-warning text-dark';
      default:          return 'bg-info';
    }
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE:    'Activo',
      EXPIRED:   'Vencido',
      CANCELLED: 'Cancelado',
      SUSPENDED: 'Suspendido',
    };
    return labels[status?.toUpperCase()] ?? status;
  }

  trackBySubscriptionId(index: number, subscription: SubscriptionResponse): string {
    return subscription.id || '';
  }
}
