import { Component, OnInit, inject, signal, computed, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { SubscriptionResponse, SubscriptionStatus, SubscriptionsFilter } from '@neversion/models';
import { SubscriptionFormComponent } from '../../components/subscription-form/subscription-form.component';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-subscriptions-list',
  standalone: true,
  imports: [CommonModule, FormsModule, SubscriptionFormComponent],
  templateUrl: './subscriptions-list.component.html',
  styleUrls: [],
})
export class SubscriptionsListComponent implements OnInit {
  @ViewChild('subscriptionForm') subscriptionForm!: SubscriptionFormComponent;

  private readonly subscriptionsService = inject(SubscriptionsService);
  private readonly toastService = inject(ToastService);
  private readonly route = inject(ActivatedRoute);

  readonly subscriptions = this.subscriptionsService.subscriptions;
  readonly isLoading = this.subscriptionsService.isLoading;

  filterStatus = signal<SubscriptionStatus | ''>('');
  filterClientId = signal<string | ''>('');
  currentPage = signal(1);
  pageSize = 10;

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

  readonly statusOptions = Object.values(SubscriptionStatus);

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
        if (params['status']) {
            this.filterStatus.set(params['status'] as SubscriptionStatus);
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
    const clientId = this.filterClientId();
    if (clientId) filter.clientId = clientId;

    this.subscriptionsService.getSubscriptions(filter).subscribe();
  }

  onSubscriptionCreated(): void {
    this.loadSubscriptions();
  }

  cancelSubscription(subscription: SubscriptionResponse): void {
    if (confirm(`¿Está seguro de que desea cancelar la suscripción de ${subscription.clientId}?`)) {
      this.subscriptionsService.cancelSubscription(subscription.id).subscribe({

        next: () => {
          this.toastService.success('Suscripción cancelada');
          this.loadSubscriptions();
        },
      });
    }
  }

  suspendSubscription(subscription: SubscriptionResponse): void {
    if (confirm(`¿Seguro que deseas suspender la suscripción ${subscription.id.slice(0, 8)}...?`)) {
      this.subscriptionsService.suspendSubscription(subscription.id).subscribe({
        next: () => {
          this.toastService.success('Suscripción suspendida');
          this.loadSubscriptions();
        },
      });
    }
  }

  onFilterChange(status: SubscriptionStatus | ''): void {
    this.filterStatus.set(status);
    this.currentPage.set(1);
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
    return subscription.id;
  }
}
