import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MasterDashboardService } from './services/master-dashboard.service';
import { ProductSummary, DashboardMetrics } from '@neversion/models';
import { DashboardMetricsComponent } from './components/dashboard-metrics/dashboard-metrics.component';
import { ProductSummaryCardComponent } from './components/product-summary-card/product-summary-card.component';

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
  isLoading = signal(false);
  hasError = signal(false);

  metrics = computed<DashboardMetrics>(() => {
    const prods = this.products();
    const totalAccounts = prods.reduce((sum, p) => sum + p.totalAccounts, 0);
    return {
      totalAccounts,
      availableProfiles: 0,
      occupiedProfiles: 0,
      activeSubscriptions: 0,
      expiringSoon: 0
    };
  });

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading.set(true);
    this.hasError.set(false);
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

  onViewAccounts(productId: string): void {
    this.router.navigate(['/dashboard/productos', productId]);
  }
}
