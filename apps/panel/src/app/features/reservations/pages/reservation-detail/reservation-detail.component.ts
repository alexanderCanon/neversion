import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { ReservationsService } from '../../services/reservations.service';
import { ReservationResponse } from '../../models/reservation.model';
import { ToastService } from '../../../../core/services/toast.service';
import { ClientsService } from '../../../clients/services/clients.service';

@Component({
  selector: 'app-reservation-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reservation-detail.component.html',
  styleUrls: []
})
export class ReservationDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reservationsService = inject(ReservationsService);
  private readonly clientsService = inject(ClientsService);
  private readonly toastService = inject(ToastService);

  reservation = signal<ReservationResponse | null>(null);
  isLoading = signal(true);
  isValidating = signal(false);
  validationNotes = signal('');
  
  // For client attachment
  clients = this.clientsService.clients;
  selectedClientId = signal('');

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadReservation(id);
      this.clientsService.getClients().subscribe();
    }
  }

  loadReservation(id: string): void {
    this.isLoading.set(true);
    this.reservationsService.getReservationById(id).subscribe({
      next: (data) => {
        this.reservation.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.toastService.error('Error al cargar la reserva');
        this.router.navigate(['/reservations']);
      }
    });
  }

  validateReservation(): void {
    const res = this.reservation();
    if (!res) return;

    this.isValidating.set(true);
    this.reservationsService.validateReservation(res.id, this.validationNotes()).subscribe({
      next: () => {
        this.toastService.success('Reserva validada y orden creada');
        this.loadReservation(res.id);
        this.isValidating.set(false);
        this.validationNotes.set('');
      },
      error: () => {
        this.toastService.error('Error al validar la reserva');
        this.isValidating.set(false);
      }
    });
  }

  cancelReservation(): void {
    const res = this.reservation();
    if (!res) return;

    if (confirm('¿Seguro que deseas cancelar esta reserva?')) {
      this.reservationsService.cancelReservation(res.id).subscribe({
        next: () => {
          this.toastService.success('Reserva cancelada');
          this.loadReservation(res.id);
        }
      });
    }
  }

  attachClient(): void {
    const res = this.reservation();
    const clientId = this.selectedClientId();
    if (!res || !clientId) return;

    this.reservationsService.attachClient(res.id, clientId).subscribe({
      next: () => {
        this.toastService.success('Cliente asignado correctamente');
        this.loadReservation(res.id);
      }
    });
  }
}
