import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardMetrics } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard-metrics',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="row mb-4 g-3">
      <!-- Total Accounts -->
      <div class="col">
        <div class="card border-primary h-100 shadow-sm custom-hover-card" [routerLink]="['/accounts']" style="cursor: pointer;">
          <div class="card-body text-center py-3">
            <h3 class="mb-0 text-primary fw-bold">{{ metrics.totalAccounts }}</h3>
            <small class="text-muted">Total de Cuentas</small>
          </div>
        </div>
      </div>
      <!-- Available Perfiles -->
      <div class="col">
        <div class="card border-success h-100 shadow-sm custom-hover-card" [routerLink]="['/accounts']" [queryParams]="{filter: 'AVAILABLE'}" style="cursor: pointer;">
          <div class="card-body text-center py-3">
            <h3 class="mb-0 text-success fw-bold">{{ metrics.availableProfiles }}</h3>
            <small class="text-muted">Perfiles Disponibles</small>
          </div>
        </div>
      </div>
      <!-- Occupied Perfiles -->
      <div class="col">
        <div class="card border-warning h-100 shadow-sm custom-hover-card" [routerLink]="['/accounts']" [queryParams]="{filter: 'OCCUPIED'}" style="cursor: pointer;">
          <div class="card-body text-center py-3">
            <h3 class="mb-0 text-warning fw-bold">{{ metrics.occupiedProfiles }}</h3>
            <small class="text-muted">Perfiles Ocupados</small>
          </div>
        </div>
      </div>
      <!-- Active Subscriptions -->
      <div class="col">
        <div class="card border-info h-100 shadow-sm custom-hover-card" [routerLink]="['/subscriptions']" [queryParams]="{status: 'ACTIVE'}" style="cursor: pointer;">
          <div class="card-body text-center py-3">
            <h3 class="mb-0 text-info fw-bold">{{ metrics.activeSubscriptions }}</h3>
            <small class="text-muted">Suscripciones Activas</small>
          </div>
        </div>
      </div>
      <!-- Expiring Soon -->
      <div class="col">
        <div class="card border-danger h-100 shadow-sm custom-hover-card" [routerLink]="['/subscriptions']" style="cursor: pointer;">
          <div class="card-body text-center py-3">
            <h3 class="mb-0 text-danger fw-bold">{{ metrics.expiringSoon }}</h3>
            <small class="text-muted">Vencen Pronto</small>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .custom-hover-card {
        transition: transform 0.2s ease, box-shadow 0.2s ease;
    }
    .custom-hover-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
    }
  `]
})
export class DashboardMetricsComponent {
  @Input() metrics: DashboardMetrics = {
    totalAccounts: 0,
    availableProfiles: 0,
    occupiedProfiles: 0,
    activeSubscriptions: 0,
    expiringSoon: 0
  };
}
