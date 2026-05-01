import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  MasterDashboardService,
  VendorDashboardKpis,
  VendorKpiMetrics
} from './services/master-dashboard.service';
import { ProductSummary } from '@neversion/models';
import { DashboardMetricsComponent } from './components/dashboard-metrics/dashboard-metrics.component';
import { ProductSummaryCardComponent } from './components/product-summary-card/product-summary-card.component';
import { ExpiringSubscriptionResult, InventoryAvailabilityResult } from '@neversion/api-client';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, DashboardMetricsComponent, ProductSummaryCardComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(MasterDashboardService);
  private readonly router = inject(Router);

  products = signal<ProductSummary[]>([]);
  kpis = signal<VendorDashboardKpis | null>(null);
  isLoading = signal(false);
  hasError = signal(false);

  metrics = computed<VendorKpiMetrics>(() => {
    return this.kpis()?.metrics ?? {
      activeClientsCount: 0,
      successfulRenewalsCount: 0,
      grossProfit: 0,
      currency: 'GTQ',
      expiringTodayCount: 0,
      expiringTomorrowCount: 0,
      expiringThisWeekCount: 0,
      availableProfiles: 0,
      occupiedProfiles: 0,
      availableFullAccounts: 0,
      occupiedFullAccounts: 0
    };
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
        this.loadProducts();
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }

  onViewAccounts(productId: string): void {
    this.router.navigate(['/dashboard/productos', productId]);
  }

  trackExpiringSubscription(_index: number, subscription: ExpiringSubscriptionResult): string {
    return subscription.subscriptionId ?? `${subscription.clientName}-${subscription.paymentDueDate}`;
  }

  trackInventory(_index: number, item: InventoryAvailabilityResult): string {
    return item.serviceId ?? item.serviceName ?? `${_index}`;
  }

  private loadProducts(): void {
    this.dashboardService.getProductsSummary('STREAMING').subscribe({
      next: (data) => {
        this.products.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }
}
