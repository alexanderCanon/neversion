import { Component, OnInit } from '@angular/core';
import { ClientsApiService, ClientOrderHistoryResponse } from '@neversion/api-client';

@Component({
  selector: 'app-customer-orders',
  templateUrl: './orders.component.html',
  styleUrls: []
})
export class OrdersComponent implements OnInit {
  orders: ClientOrderHistoryResponse[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(private clientsApi: ClientsApiService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.error = null;

    this.clientsApi.getMyOrdersClient().subscribe({
      next: (orders: any) => {
        this.orders = orders;
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error fetching client orders:', err);
        this.error = 'Ocurrió un error al cargar tu historial de órdenes.';
        this.isLoading = false;
      }
    });
  }

  getStatusLabel(status?: string): string {
    const labels: Record<string, string> = {
      PENDING: 'Pendiente',
      VALIDATED: 'Validada',
      COMPLETED: 'Completada',
      REJECTED: 'Rechazada',
      CANCELLED: 'Cancelada'
    };
    return status ? labels[status] ?? status : 'Sin estado';
  }

  getStatusClass(status?: string): string {
    const classes: Record<string, string> = {
      PENDING: 'bg-warning text-dark',
      VALIDATED: 'bg-info text-dark',
      COMPLETED: 'bg-success',
      REJECTED: 'bg-danger',
      CANCELLED: 'bg-secondary'
    };
    return status ? classes[status] ?? 'bg-secondary' : 'bg-secondary';
  }

  getServicesLabel(order: ClientOrderHistoryResponse): string {
    const services = order.services ?? [];
    if (!services.length) return 'Servicios no disponibles';
    return services
      .map((service) => `${service.serviceName ?? 'Servicio'} x${service.quantity ?? 1}`)
      .join(', ');
  }
}
