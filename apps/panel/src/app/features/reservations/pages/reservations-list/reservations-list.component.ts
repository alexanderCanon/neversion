import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ReservationsService } from '../../services/reservations.service';
import { ReservationResponse, ReservationStatus } from '@neversion/models';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-reservations-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reservations-list.component.html',
  styleUrls: []
})
export class ReservationsListComponent implements OnInit {
  private readonly reservationsService = inject(ReservationsService);
  private readonly toastService = inject(ToastService);

  readonly reservations = this.reservationsService.reservations;
  readonly isLoading = this.reservationsService.isLoading;

  statusFilter = signal<ReservationStatus | ''>('');
  searchTerm = signal('');
  currentPage = signal(1);
  pageSize = 10;

  readonly filteredReservations = computed(() => {
    let result = this.reservations();
    const status = this.statusFilter();
    const term = this.searchTerm().toLowerCase();

    if (status) {
      result = result.filter(r => r.status === status);
    }

    if (term) {
      result = result.filter(r => 
        r.id.toLowerCase().includes(term) || 
        (r.clientId && r.clientId.toLowerCase().includes(term))
      );
    }

    return result;
  });

  readonly paginatedReservations = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredReservations().slice(start, start + this.pageSize);
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.filteredReservations().length / this.pageSize)
  );

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.reservationsService.getReservations().subscribe();
  }

  onFilterChange(): void {
    this.currentPage.set(1);
  }

  cancelReservation(reservation: ReservationResponse): void {
    if (confirm(`¿Seguro que deseas cancelar la reserva: ${reservation.id}?`)) {
      this.reservationsService.cancelReservation(reservation.id).subscribe({
        next: () => {
          this.toastService.success(`Reserva ${reservation.id} cancelada`);
          this.loadReservations();
        }
      });
    }
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

  getStatusBadgeClass(status: ReservationStatus): string {
    switch (status) {
      case 'PENDING': return 'bg-warning text-dark';
      case 'UPLOADED': return 'bg-info text-dark';
      case 'VALIDATED': return 'bg-success';
      case 'EXPIRED': return 'bg-secondary';
      case 'CANCELLED': return 'bg-danger';
      default: return 'bg-light text-dark';
    }
  }
}
