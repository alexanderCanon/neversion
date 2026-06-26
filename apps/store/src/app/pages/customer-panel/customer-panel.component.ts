import { Component } from '@angular/core';

type CustomerPanelTab = 'accesses' | 'orders' | 'receipts' | 'profile';

@Component({
  selector: 'app-customer-panel',
  template: `
    <div class="container py-4 py-md-5 mt-3 mt-md-4">
      <div class="row">
        <div class="col-12">
          <h2 class="mb-3 mb-md-4 fw-bold text-dark">Panel de Cliente</h2>
          <ul class="nav nav-tabs mb-4" role="tablist">
            <li class="nav-item">
              <button type="button"
                      class="nav-link"
                      [class.active]="activeTab === 'accesses'"
                      (click)="setActiveTab('accesses')">
                Accesos
              </button>
            </li>
            <li class="nav-item">
              <button type="button"
                      class="nav-link"
                      [class.active]="activeTab === 'orders'"
                      (click)="setActiveTab('orders')">
                Órdenes
              </button>
            </li>
            <li class="nav-item">
              <button type="button"
                      class="nav-link"
                      [class.active]="activeTab === 'receipts'"
                      (click)="setActiveTab('receipts')">
                Comprobantes
              </button>
            </li>
            <li class="nav-item">
              <button type="button"
                      class="nav-link"
                      [class.active]="activeTab === 'profile'"
                      (click)="setActiveTab('profile')">
                Perfil
              </button>
            </li>
          </ul>
 
          <app-customer-accesses *ngIf="activeTab === 'accesses'"></app-customer-accesses>
          <app-customer-orders *ngIf="activeTab === 'orders'"></app-customer-orders>
          <app-customer-receipts *ngIf="activeTab === 'receipts'"></app-customer-receipts>
          <app-customer-profile *ngIf="activeTab === 'profile'"></app-customer-profile>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .nav-tabs {
      flex-wrap: nowrap !important;
      overflow-x: auto !important;
      overflow-y: hidden !important;
      -webkit-overflow-scrolling: touch;
      border-bottom: 1px solid var(--color-surface-200) !important;
      scrollbar-width: none; /* Firefox */
    }
    .nav-tabs::-webkit-scrollbar {
      display: none; /* Chrome, Safari */
    }
    .nav-item {
      white-space: nowrap;
    }
    .nav-link {
      background: transparent !important;
      border: none !important;
      border-bottom: 3px solid transparent !important;
      color: var(--color-text-secondary) !important;
      font-weight: var(--font-weight-semibold);
      padding: 0.75rem 1.25rem !important;
      transition: all 0.2s ease;
      font-size: 0.95rem;
    }
    .nav-link.active {
      color: var(--color-brand-500) !important;
      border-bottom-color: var(--color-brand-500) !important;
      font-weight: var(--font-weight-bold);
    }
    .nav-link:hover:not(.active) {
      color: var(--color-brand-400) !important;
      border-bottom-color: var(--color-surface-200) !important;
    }
  `]
})
export class CustomerPanelComponent {
  activeTab: CustomerPanelTab = 'accesses';

  setActiveTab(tab: CustomerPanelTab): void {
    this.activeTab = tab;
  }
}
