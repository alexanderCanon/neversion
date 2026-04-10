import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { OrdersService } from '../../services/orders.service';
import { OrderResponse, OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-orders-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './orders-list.component.html',
  styleUrls: []
})
export class OrdersListComponent implements OnInit {
  private readonly ordersService = inject(OrdersService);

  private readonly _orders = signal<OrderResponse[]>([]);
  readonly isLoading = signal(false);

  searchTerm = signal('');
  currentPage = signal(1);
  pageSize = 10;

  readonly filteredOrders = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const allOrders = this._orders();

    if (!term) return allOrders;

    return allOrders.filter(o => 
      o.id.toLowerCase().includes(term) || 
      o.reservationId.toLowerCase().includes(term)
    );
  });

  readonly paginatedOrders = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredOrders().slice(start, start + this.pageSize);
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.filteredOrders().length / this.pageSize)
  );

  ngOnInit(): void {
    // Note: Since there is no "List all orders" endpoint in the current contract,
    // this would typically be populated via a different flow or a new endpoint.
    // For now, we scaffold it as a placeholder.
    this.loadOrders();
  }

  loadOrders(): void {
    // Placeholder - orders.md doesn't specify a GET /orders list endpoint yet,
    // only GET /orders/{id} and GET /orders/by-reservation/{id}.
    // In a real scenario, we'd need a list endpoint or handle navigation to specific orders.
    this.isLoading.set(true);
    // Simulating empty list for now as per contract limitations
    setTimeout(() => {
        this.isLoading.set(false);
    }, 500);
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

  getStatusBadgeClass(status: OrderStatus): string {
    switch (status) {
      case 'PENDING': return 'bg-warning text-dark';
      case 'COMPLETED': return 'bg-success';
      case 'REJECTED': return 'bg-danger';
      case 'CANCELLED': return 'bg-secondary';
      default: return 'bg-light text-dark';
    }
  }
}
