import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ClientsService } from '../../services/clients.service';
import { ClientDetail } from '@neversion/models';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-client-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, PhonePipe, DatePipe],
  templateUrl: './client-detail.component.html',
  styleUrl: './client-detail.component.scss'
  })
export class ClientDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly clientsService = inject(ClientsService);
  private readonly toastService = inject(ToastService);

  clientDetail = signal<ClientDetail | null>(null);
  isLoading = signal<boolean>(true);
  error = signal<string | null>(null);
  activeTab = signal<'INFO' | 'SUBS' | 'ORDERS'>('INFO');

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadClientDetail(id);
    } else {
      this.error.set('No se proporcionó un ID de cliente válido.');
      this.isLoading.set(false);
    }
  }

  private loadClientDetail(id: string): void {
    this.isLoading.set(true);
    this.clientsService.getClientDetail(id).subscribe({
      next: (detail) => {
        this.clientDetail.set(detail);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading client detail', err);
        this.error.set('Ocurrió un error al cargar los detalles del cliente.');
        this.toastService.error('Error al cargar detalles del cliente');
        this.isLoading.set(false);
      }
    });
  }

  setTab(tab: 'INFO' | 'SUBS' | 'ORDERS'): void {
    this.activeTab.set(tab);
  }

  getStatusLabel(status: string | undefined): string {
    if (!status) return '';
    const labels: Record<string, string> = {
      ACTIVE:    'Activo',
      EXPIRED:   'Vencido',
      CANCELLED: 'Cancelado',
      SUSPENDED: 'Suspendido',
    };
    return labels[status.toUpperCase()] ?? status;
  }

  getStatusClass(status: string | undefined): string {
    if (!status) return 'badge-status default';
    switch (status.toUpperCase()) {
      case 'ACTIVE':    return 'badge-status active';
      case 'EXPIRED':   return 'badge-status expired';
      case 'CANCELLED': return 'badge-status cancelled';
      case 'SUSPENDED': return 'badge-status suspended';
      default:          return 'badge-status default';
    }
  }

  getOrderStatusLabel(status: string | undefined): string {
    if (!status) return '';
    const labels: Record<string, string> = {
      PENDING: 'Pendiente',
      VALIDATED: 'Validada',
      COMPLETED: 'Completada',
      REJECTED: 'Rechazada',
      CANCELLED: 'Cancelada'
    };
    return labels[status.toUpperCase()] ?? status;
  }

  getOrderStatusClass(status: string | undefined): string {
    if (!status) return 'badge-status default';
    switch (status.toUpperCase()) {
      case 'PENDING': return 'badge-status pending';
      case 'VALIDATED': return 'badge-status validated';
      case 'COMPLETED': return 'badge-status completed';
      case 'REJECTED': return 'badge-status rejected';
      case 'CANCELLED': return 'badge-status cancelled';
      default: return 'badge-status default';
    }
  }
}
