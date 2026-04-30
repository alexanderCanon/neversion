import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  ClientsApiService,
  ClientAccessResponse,
  CreateRenewalReservationRequest,
  ReservationsApiService
} from '@neversion/api-client';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-customer-accesses',
  templateUrl: './accesses.component.html',
  styleUrls: []
})
export class AccessesComponent implements OnInit {
  subscriptions: ClientAccessResponse[] = [];
  isLoading = true;
  error: string | null = null;
  renewalError: string | null = null;
  renewingSubscriptionId: string | null = null;

  constructor(
    private clientsApi: ClientsApiService,
    private reservationsApi: ReservationsApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.id) {
        this.loadAccesses();
      } else {
        this.isLoading = false;
        this.error = 'Usuario no autenticado.';
      }
    });
  }

  loadAccesses(): void {
    this.isLoading = true;
    this.error = null;

    this.clientsApi.getMyAccesses().subscribe({
      next: (accesses) => {
        this.subscriptions = accesses;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching accesses:', err);
        this.error = 'Ocurrió un error al cargar los accesos.';
        this.isLoading = false;
      }
    });
  }

  canRenew(subscription: ClientAccessResponse): boolean {
    return !!subscription.subscriptionId
      && (subscription.status === 'ACTIVE' || subscription.status === 'SUSPENDED');
  }

  isRenewing(subscription: ClientAccessResponse): boolean {
    return this.renewingSubscriptionId === subscription.subscriptionId;
  }

  startRenewal(subscription: ClientAccessResponse): void {
    if (!subscription.subscriptionId || this.renewingSubscriptionId) return;

    this.renewalError = null;
    this.renewingSubscriptionId = subscription.subscriptionId;

    const request: CreateRenewalReservationRequest = {
      subscriptionId: subscription.subscriptionId,
      paymentMethod: 'TRANSFERENCIA'
    };

    this.reservationsApi.createRenewalReservation(request).subscribe({
      next: (reservation) => {
        this.renewingSubscriptionId = null;
        this.router.navigate(['/payment-page'], {
          queryParams: {
            reservationId: reservation.id,
            flow: 'renewal'
          }
        });
      },
      error: (err) => {
        console.error('Error creating renewal reservation:', err);
        this.renewalError = err?.status === 400
          ? 'Ya existe una renovación pendiente para esta suscripción.'
          : 'No se pudo iniciar la renovación. Intenta de nuevo.';
        this.renewingSubscriptionId = null;
      }
    });
  }
}
