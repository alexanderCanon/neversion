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
        <div class="card metric-card h-100 border-0 shadow-sm">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-3">
              <div class="icon-box bg-primary-subtle text-primary">
                <i class="bi bi-people"></i>
              </div>
              <div>
                <p class="metric-label mb-0">Clientes Activos</p>
                <h3 class="mb-0 fw-bold">{{ metrics.activeClientsCount }}</h3>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card h-100 border-0 shadow-sm">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-3">
              <div class="icon-box bg-success-subtle text-success">
                <i class="bi bi-graph-up-arrow"></i>
              </div>
              <div>
                <p class="metric-label mb-0">Ganancia Mes</p>
                <h3 class="mb-0 fw-bold">{{ metrics.grossProfit | currency:metrics.currency:'symbol':'1.2-2' }}</h3>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card h-100 border-0 shadow-sm">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-3">
              <div class="icon-box bg-info-subtle text-info">
                <i class="bi bi-arrow-repeat"></i>
              </div>
              <div>
                <p class="metric-label mb-0">Renovaciones</p>
                <h3 class="mb-0 fw-bold">{{ metrics.successfulRenewalsCount }}</h3>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card h-100 border-0 shadow-sm pointer" [routerLink]="['/subscriptions']">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-3">
              <div class="icon-box bg-danger-subtle text-danger">
                <i class="bi bi-calendar2-x"></i>
              </div>
              <div>
                <p class="metric-label mb-0">Vencen Hoy</p>
                <h3 class="mb-0 fw-bold">{{ metrics.expiringTodayCount }}</h3>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-md-6 col-xl">
        <div class="card metric-card h-100 border-0 shadow-sm pointer" [routerLink]="['/accounts']">
          <div class="card-body p-4">
            <div class="d-flex align-items-center gap-3">
              <div class="icon-box bg-warning-subtle text-warning">
                <i class="bi bi-box-seam"></i>
              </div>
              <div>
                <p class="metric-label mb-0">Stock Disponible</p>
                <h3 class="mb-0 fw-bold">{{ metrics.availableProfiles + metrics.availableFullAccounts }}</h3>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .metric-card {
      border-radius: 14px;
      transition: all 0.2s ease;
      background-color: #fff;

      &.pointer { cursor: pointer; }

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05)!important;
      }
    }

    .icon-box {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      flex-shrink: 0;
    }

    .metric-label {
      color: var(--color-text-secondary);
      font-size: 0.75rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.025em;
    }

    h3 {
      font-size: 1.5rem;
      color: var(--color-text-primary);
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
