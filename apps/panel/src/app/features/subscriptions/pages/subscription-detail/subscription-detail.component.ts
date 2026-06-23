import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { SubscriptionsService } from '../../services/subscriptions.service';
import { SubscriptionDetailResponse } from '@neversion/api-client';
import { ToastService } from '../../../../core/services/toast.service';
import { copyToClipboard, getSubscriptionStatusLabel, getSubscriptionStatusClass } from '@neversion/utils';

@Component({
  selector: 'app-subscription-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './subscription-detail.component.html',
  styleUrl: './subscription-detail.component.scss'
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

  copyToClipboard(text: string | undefined, label: string): void {
    if (!text) return;
    copyToClipboard(text).then(() => {
      this.toastService.success(`${label} copiado al portapapeles`);
    }).catch(() => {
      this.toastService.error(`No se pudo copiar el ${label.toLowerCase()}`);
    });
  }

  getStatusClass(status: string): string {
    return getSubscriptionStatusClass(status);
  }

  getStatusLabel(status: string): string {
    return getSubscriptionStatusLabel(status);
  }

  getSaleModeLabel(saleMode?: string): string {
    return saleMode === 'FULL_ACCOUNT' ? 'Cuenta Completa' : 'Perfil Individual';
  }

  /**
   * Returns true when the subscription is a Spotify Family (BY_PROFILE) slot.
   * In this mode the master account credentials are intentionally hidden by the
   * backend — the client uses their own personal account or an invitation link.
   */
  isSpotifyByProfile(): boolean {
    const sub = this.subscription();
    return (
      sub?.financialSnapshot?.serviceName?.toLowerCase() === 'spotify' &&
      sub?.access?.saleMode === 'BY_PROFILE'
    );
  }
}
