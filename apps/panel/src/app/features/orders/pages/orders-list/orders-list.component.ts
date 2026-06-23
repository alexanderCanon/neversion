import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { OrdersService } from '../../services/orders.service';
import { AuthService } from '../../../../core/services/auth.service';
import { OrderResponse, OrderStatus } from '@neversion/models';
import { getOrderStatusLabel, getOrderStatusClass } from '@neversion/utils';

@Component({
  selector: 'app-orders-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './orders-list.component.html',
  styleUrl: './orders-list.component.scss'
})
export class OrdersListComponent implements OnInit {
  private readonly ordersService = inject(OrdersService);
  private readonly authService = inject(AuthService);

  private readonly _orders = signal<OrderResponse[]>([]);
  readonly isLoading = signal(false);

  statusFilter = signal<OrderStatus | ''>('');
  searchTerm = signal('');
  currentPage = signal(1);
  pageSize = 10;

  readonly filteredOrders = computed(() => {
    let result = this._orders();
    const status = this.statusFilter();
    const term = this.searchTerm().toLowerCase();

    if (status) {
      result = result.filter(o => o.status === status);
    }

    if (term) {
      result = result.filter(o => 
        o.id.toLowerCase().includes(term) || 
        o.reservationId.toLowerCase().includes(term)
      );
    }

    return result;
  });

  readonly paginatedOrders = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredOrders().slice(start, start + this.pageSize);
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.filteredOrders().length / this.pageSize)
  );

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    const vendorUuid = this.authService.currentVendorUuid();
    if (!vendorUuid) return;

    this.isLoading.set(true);
    this.ordersService.getOrdersByVendor(vendorUuid).subscribe({
      next: (data) => {
        this._orders.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  onFilterChange(): void {
    this.currentPage.set(1);
  }

  prevPage(): void {
    if (this.currentPage() > 1) {
      this.currentPage.update(p => p - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages()) {
      this.currentPage.update(p => p + 1);
    }
  }

  getStatusLabel(status: OrderStatus | undefined): string {
    return getOrderStatusLabel(status);
  }

  getStatusBadgeClass(status: OrderStatus | undefined): string {
    return getOrderStatusClass(status);
  }
}
