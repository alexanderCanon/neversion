import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { SubscriptionDetailResponse } from '@neversion/api-client';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-subscription-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './subscription-detail.component.html',
  styleUrls: []
})
export class SubscriptionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly subscriptionsService = inject(SubscriptionsService);
  private readonly toastService = inject(ToastService);

  subscription = signal<SubscriptionDetailResponse | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadSubscription(id);
    } else {
      this.error.set('ID de suscripción no válido');
      this.isLoading.set(false);
    }
  }

  loadSubscription(id: string): void {
    this.isLoading.set(true);
    this.subscriptionsService.getSubscriptionDetail(id).subscribe({
      next: (detail) => {
        this.subscription.set(detail);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading subscription detail:', err);
        this.error.set('No se pudo cargar el detalle de la suscripción.');
        this.isLoading.set(false);
      }
    });
  }

  renew(): void {
    const sub = this.subscription();
    if (!sub || !sub.id) return;

    if (confirm('¿Deseas renovar esta suscripción por un periodo adicional?')) {
      this.subscriptionsService.renewSubscription(sub.id).subscribe({
        next: () => {
          this.toastService.success('Suscripción renovada con éxito');
          this.loadSubscription(sub.id!);
        },
        error: () => this.toastService.error('Error al renovar la suscripción')
      });
    }
  }

  revoke(): void {
    const sub = this.subscription();
    if (!sub || !sub.id) return;

    if (confirm('¿Estás seguro de que deseas REVOCAR el acceso? El perfil volverá a estar disponible y el cliente será notificado.')) {
      this.subscriptionsService.cancelSubscription(sub.id).subscribe({
        next: () => {
          this.toastService.success('Acceso revocado y suscripción cancelada');
          this.loadSubscription(sub.id!);
        },
        error: () => this.toastService.error('Error al revocar el acceso')
      });
    }
  }

  suspend(): void {
    const sub = this.subscription();
    if (!sub || !sub.id) return;

    if (confirm('¿Deseas suspender temporalmente esta suscripción?')) {
      this.subscriptionsService.suspendSubscription(sub.id).subscribe({
        next: () => {
          this.toastService.success('Suscripción suspendida');
          this.loadSubscription(sub.id!);
        },
        error: () => this.toastService.error('Error al suspender la suscripción')
      });
    }
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':    return 'bg-success';
      case 'EXPIRED':   return 'bg-danger';
      case 'CANCELLED': return 'bg-secondary';
      case 'SUSPENDED': return 'bg-warning text-dark';
      default:          return 'bg-info';
    }
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE:    'Activo',
      EXPIRED:   'Vencido',
      CANCELLED: 'Cancelado',
      SUSPENDED: 'Suspendido',
    };
    return labels[status?.toUpperCase()] ?? status;
  }
}
