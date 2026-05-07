import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { VendorKpiMetrics } from '../../services/master-dashboard.service';

@Component({
  selector: 'app-dashboard-metrics',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="row mb-4 g-3">
      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card border-primary h-100 shadow-sm">
          <div class="card-body py-3">
            <div class="d-flex align-items-center justify-content-between gap-3">
              <div>
                <p class="metric-label mb-1">Clientes activos</p>
                <h3 class="mb-0 text-primary fw-bold">{{ metrics.activeClientsCount }}</h3>
              </div>
              <i class="bi bi-people fs-2 text-primary"></i>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card border-success h-100 shadow-sm">
          <div class="card-body py-3">
            <div class="d-flex align-items-center justify-content-between gap-3">
              <div>
                <p class="metric-label mb-1">Ganancia del mes</p>
                <h3 class="mb-0 text-success fw-bold">
                  {{ metrics.grossProfit | currency:metrics.currency:'symbol':'1.2-2' }}
                </h3>
              </div>
              <i class="bi bi-cash-coin fs-2 text-success"></i>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card border-info h-100 shadow-sm">
          <div class="card-body py-3">
            <div class="d-flex align-items-center justify-content-between gap-3">
              <div>
                <p class="metric-label mb-1">Renovaciones del mes</p>
                <h3 class="mb-0 text-info fw-bold">{{ metrics.successfulRenewalsCount }}</h3>
              </div>
              <i class="bi bi-arrow-repeat fs-2 text-info"></i>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card border-danger h-100 shadow-sm" [routerLink]="['/subscriptions']">
          <div class="card-body py-3">
            <div class="d-flex align-items-center justify-content-between gap-3">
              <div>
                <p class="metric-label mb-1">Vencen hoy</p>
                <h3 class="mb-0 text-danger fw-bold">{{ metrics.expiringTodayCount }}</h3>
              </div>
              <i class="bi bi-calendar2-x fs-2 text-danger"></i>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card border-warning h-100 shadow-sm" [routerLink]="['/accounts']">
          <div class="card-body py-3">
            <div class="d-flex align-items-center justify-content-between gap-3">
              <div>
                <p class="metric-label mb-1">Inventario disponible</p>
                <h3 class="mb-0 text-warning fw-bold">
                  {{ metrics.availableProfiles + metrics.availableFullAccounts }}
                </h3>
              </div>
              <i class="bi bi-box-seam fs-2 text-warning"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .metric-card {
      cursor: pointer;
      border-radius: 8px;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }
    .metric-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
    }
    .metric-label {
      color: #6c757d;
      font-size: 0.82rem;
      font-weight: 600;
      line-height: 1.2;
    }
  `]
})
export class DashboardMetricsComponent {
  @Input() metrics: VendorKpiMetrics = {
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
}
