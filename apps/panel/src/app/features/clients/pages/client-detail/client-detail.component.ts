import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, Location } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClientsService } from '../../services/clients.service';
import { ClientPointsService } from '../../services/client-points.service';
import { ClientDetail } from '@neversion/models';
import { PointsSummaryResponse, PointsMovementResponse } from '@neversion/api-client';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';
import { ToastService } from '../../../../core/services/toast.service';
import {
  getSubscriptionStatusLabel,
  getSubscriptionStatusClass,
  getOrderStatusLabel,
  getOrderStatusClass
} from '@neversion/utils';

@Component({
  selector: 'app-client-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, PhonePipe, DatePipe],
  templateUrl: './client-detail.component.html',
  styleUrl: './client-detail.component.scss'
  })
export class ClientDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly clientsService = inject(ClientsService);
  private readonly clientPointsService = inject(ClientPointsService);
  private readonly toastService = inject(ToastService);
  private readonly location = inject(Location);
  private readonly fb = inject(FormBuilder);

  clientDetail = signal<ClientDetail | null>(null);
  isLoading = signal<boolean>(true);
  error = signal<string | null>(null);
  activeTab = signal<'INFO' | 'SUBS' | 'ORDERS' | 'POINTS'>('INFO');

  pointsSummary = signal<PointsSummaryResponse | null>(null);
  pointsMovements = signal<PointsMovementResponse[]>([]);
  pointsTotalElements = signal<number>(0);
  pointsPage = signal<number>(0);
  readonly pointsPageSize = 10;
  isLoadingPoints = signal<boolean>(false);
  isAdjustingPoints = signal<boolean>(false);
  adjustForm!: FormGroup;

  goBack(): void {
    this.location.back();
  }

  ngOnInit(): void {
    this.adjustForm = this.fb.group({
      points: [null, [Validators.required]],
      notes: ['', [Validators.required, Validators.maxLength(255)]]
    });

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

  setTab(tab: 'INFO' | 'SUBS' | 'ORDERS' | 'POINTS'): void {
    this.activeTab.set(tab);
    if (tab === 'POINTS' && !this.pointsSummary()) {
      this.loadPoints();
    }
  }

  private loadPoints(): void {
    const clientId = this.clientDetail()?.client?.id;
    if (!clientId) return;

    this.isLoadingPoints.set(true);
    this.clientPointsService.getSummary(clientId).subscribe({
      next: (summary) => this.pointsSummary.set(summary),
      error: (err) => {
        console.error('Error loading points summary', err);
        this.toastService.error('Error al cargar el resumen de puntos');
      }
    });
    this.loadMovements(clientId, 0);
  }

  private loadMovements(clientId: string, page: number): void {
    this.isLoadingPoints.set(true);
    this.clientPointsService.getMovements(clientId, page, this.pointsPageSize).subscribe({
      next: (result) => {
        this.pointsMovements.set(result.movements ?? []);
        this.pointsTotalElements.set(result.totalElements ?? 0);
        this.pointsPage.set(page);
        this.isLoadingPoints.set(false);
      },
      error: (err) => {
        console.error('Error loading points movements', err);
        this.toastService.error('Error al cargar los movimientos de puntos');
        this.isLoadingPoints.set(false);
      }
    });
  }

  goToPointsPage(page: number): void {
    const clientId = this.clientDetail()?.client?.id;
    if (!clientId || page < 0) return;
    this.loadMovements(clientId, page);
  }

  get pointsTotalPages(): number {
    return Math.max(1, Math.ceil(this.pointsTotalElements() / this.pointsPageSize));
  }

  onAdjustPoints(): void {
    if (this.adjustForm.invalid) {
      this.adjustForm.markAllAsTouched();
      this.toastService.error('Revisa los campos del formulario');
      return;
    }

    const clientId = this.clientDetail()?.client?.id;
    if (!clientId) return;

    const { points, notes } = this.adjustForm.value;
    this.isAdjustingPoints.set(true);
    this.clientPointsService.adjustPoints(clientId, { points, notes }).subscribe({
      next: () => {
        this.isAdjustingPoints.set(false);
        this.adjustForm.reset({ points: null, notes: '' });
        this.toastService.success('Ajuste de puntos registrado correctamente');
        this.loadPoints();
      },
      error: (err) => {
        this.isAdjustingPoints.set(false);
        const msg = err?.error?.message || 'Error al registrar el ajuste de puntos';
        this.toastService.error(msg);
        console.error(err);
      }
    });
  }

  getMovementTypeLabel(entryType: string | undefined): string {
    switch (entryType) {
      case 'EARN': return 'Acumulación';
      case 'REDEEM': return 'Redención';
      case 'ADJUSTMENT': return 'Ajuste manual';
      case 'REVERSAL': return 'Reversión';
      default: return entryType || '-';
    }
  }

  getMovementTypeClass(entryType: string | undefined): string {
    switch (entryType) {
      case 'EARN': return 'badge bg-success-subtle text-success';
      case 'REDEEM': return 'badge bg-primary-subtle text-primary';
      case 'ADJUSTMENT': return 'badge bg-warning-subtle text-warning';
      case 'REVERSAL': return 'badge bg-danger-subtle text-danger';
      default: return 'badge bg-secondary-subtle text-secondary';
    }
  }

  getStatusLabel(status: string | undefined): string {
    return getSubscriptionStatusLabel(status);
  }

  getStatusClass(status: string | undefined): string {
    return getSubscriptionStatusClass(status);
  }

  getOrderStatusLabel(status: string | undefined): string {
    return getOrderStatusLabel(status);
  }

  getOrderStatusClass(status: string | undefined): string {
    return getOrderStatusClass(status);
  }
}
