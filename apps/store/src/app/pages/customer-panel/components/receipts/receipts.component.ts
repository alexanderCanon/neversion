import { Component, OnInit } from '@angular/core';
import { ClientsApiService, ClientReservationStatusResponse } from '@neversion/api-client';

@Component({
  selector: 'app-customer-receipts',
  templateUrl: './receipts.component.html',
  styleUrls: []
})
export class ReceiptsComponent implements OnInit {
  reservations: ClientReservationStatusResponse[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(private clientsApi: ClientsApiService) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.isLoading = true;
    this.error = null;

    this.clientsApi.getMyReservationsClient().subscribe({
      next: (reservations: any) => {
        this.reservations = reservations;
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error fetching client reservations:', err);
        this.error = 'Ocurrió un error al cargar tus comprobantes.';
        this.isLoading = false;
      }
    });
  }

  getStatusLabel(status?: string): string {
    const labels: Record<string, string> = {
      PENDING: 'Pendiente',
      UPLOADED: 'Subido',
      VALIDATED: 'Validado',
      REJECTED: 'Rechazado',
      EXPIRED: 'Expirado',
      CANCELLED: 'Cancelado'
    };
    return status ? labels[status] ?? status : 'Sin estado';
  }

  getStatusClass(status?: string): string {
    const classes: Record<string, string> = {
      PENDING: 'bg-warning text-dark',
      UPLOADED: 'bg-info text-dark',
      VALIDATED: 'bg-success',
      REJECTED: 'bg-danger',
      EXPIRED: 'bg-secondary',
      CANCELLED: 'bg-secondary'
    };
    return status ? classes[status] ?? 'bg-secondary' : 'bg-secondary';
  }

  getServicesLabel(reservation: ClientReservationStatusResponse): string {
    const services = reservation.services ?? [];
    if (!services.length) return 'Servicios no disponibles';
    return services
      .map((service) => `${service.serviceName ?? 'Servicio'} x${service.quantity ?? 1}`)
      .join(', ');
  }
}
