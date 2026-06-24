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
      @for (card of cards; track card.label) {
        <div class="col-12 col-md-6 col-xl">
          <div class="card metric-card h-100 border-0 shadow-sm"
               [class.pointer]="!!card.link"
               [routerLink]="card.link">
            <div class="card-body p-4">
              <div class="d-flex align-items-center gap-3">
                <div class="icon-box" [ngClass]="card.colorClass">
                  <i class="bi" [ngClass]="card.icon"></i>
                </div>
                <div>
                  <p class="metric-label mb-0">{{ card.label }}</p>
                  <h3 class="mb-0 fw-bold">
                    @if (card.isCurrency) {
                      {{ card.value | currency:metrics.currency:'symbol':'1.2-2' }}
                    } @else {
                      {{ card.value }}
                    }
                  </h3>
                </div>
              </div>
            </div>
          </div>
        </div>
      }
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
    expiringThisWeekCount: 0
  };

  get cards() {
    return [
      {
        label: 'Clientes Activos',
        value: this.metrics.activeClientsCount,
        icon: 'bi-people',
        colorClass: 'bg-primary-subtle text-primary',
        link: ['/clients']
      },
      {
        label: 'Ganancia Mes',
        value: this.metrics.grossProfit,
        isCurrency: true,
        icon: 'bi-graph-up-arrow',
        colorClass: 'bg-success-subtle text-success',
        link: null
      },
      {
        label: 'Renovaciones',
        value: this.metrics.successfulRenewalsCount,
        icon: 'bi-arrow-repeat',
        colorClass: 'bg-info-subtle text-info',
        link: null
      },
      {
        label: 'Vencen Hoy',
        value: this.metrics.expiringTodayCount,
        icon: 'bi-calendar2-x',
        colorClass: 'bg-danger-subtle text-danger',
        link: ['/subscriptions']
      }
    ];
  }
}
