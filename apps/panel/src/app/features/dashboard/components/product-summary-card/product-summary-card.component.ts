import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductSummary } from '../../models/dashboard.model';

@Component({
  selector: 'app-product-summary-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card h-100 shadow-sm border-0">
      <div class="card-body d-flex flex-column">
        <div class="d-flex align-items-center mb-3">
          <div class="rounded-circle d-flex align-items-center justify-content-center me-3"
               style="width: 48px; height: 48px; background-color: var(--primary-color);">
            <i class="bi bi-display fs-5 text-white"></i>
          </div>
          <div>
            <h5 class="card-title fw-bold mb-0">{{ product.productName }}</h5>
            <span class="badge bg-secondary">{{ product.category }}</span>
          </div>
        </div>
        <div class="mb-3">
          <span class="fs-3 fw-bold text-primary">{{ product.totalAccounts }}</span>
          <span class="text-muted ms-1">cuentas</span>
        </div>
        <button class="btn btn-primary btn-sm mt-auto w-100"
                (click)="viewAccounts.emit(product.productId)">
          <i class="bi bi-eye me-1"></i>Ver cuentas
        </button>
      </div>
    </div>
  `
})
export class ProductSummaryCardComponent {
  @Input({ required: true }) product!: ProductSummary;
  @Output() viewAccounts = new EventEmitter<string>();
}
