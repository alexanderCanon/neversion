import { Component } from '@angular/core';

type CustomerPanelTab = 'accesses' | 'orders' | 'receipts' | 'profile';

@Component({
  selector: 'app-customer-panel',
  template: `
    <div class="container py-5">
      <div class="row">
        <div class="col-12">
          <h2 class="mb-4 fw-bold">Panel de Cliente</h2>
          <ul class="nav nav-tabs" role="tablist">
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
})
export class CustomerPanelComponent {
  activeTab: CustomerPanelTab = 'accesses';

  setActiveTab(tab: CustomerPanelTab): void {
    this.activeTab = tab;
  }
}
