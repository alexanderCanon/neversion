import { Component, OnInit } from '@angular/core';
import { LoyaltyPointsApiService, PointsMovementResponse, PointsSummaryResponse } from '@neversion/api-client';

@Component({
  standalone: false,
  selector: 'app-customer-points',
  templateUrl: './points.component.html',
  styleUrls: []
})
export class PointsComponent implements OnInit {
  summary: PointsSummaryResponse | null = null;
  movements: PointsMovementResponse[] = [];
  totalElements = 0;
  page = 0;
  readonly pageSize = 10;
  isLoading = true;
  error: string | null = null;

  constructor(private loyaltyApi: LoyaltyPointsApiService) {}

  ngOnInit(): void {
    this.loadSummary();
    this.loadMovements(0);
  }

  loadSummary(): void {
    this.loyaltyApi.getMySummaryClientPoints().subscribe({
      next: (s: any) => (this.summary = s),
      error: (err: any) => {
        console.error(err);
        this.error = 'Error al cargar tu saldo de puntos.';
      }
    });
  }

  loadMovements(page: number): void {
    this.isLoading = true;
    this.loyaltyApi.getMyMovementsClientPoints(page, this.pageSize).subscribe({
      next: (result: any) => {
        this.movements = result.movements ?? [];
        this.totalElements = result.totalElements ?? 0;
        this.page = page;
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error(err);
        this.error = 'Error al cargar tus movimientos de puntos.';
        this.isLoading = false;
      }
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.totalElements / this.pageSize));
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.loadMovements(page);
  }

  getMovementTypeLabel(entryType?: string): string {
    const labels: Record<string, string> = {
      EARN: 'Acumulación',
      REDEEM: 'Redención',
      ADJUSTMENT: 'Ajuste manual',
      REVERSAL: 'Reversión'
    };
    return entryType ? (labels[entryType] ?? entryType) : '-';
  }

  getMovementTypeClass(entryType?: string): string {
    const classes: Record<string, string> = {
      EARN: 'bg-success',
      REDEEM: 'bg-info text-dark',
      ADJUSTMENT: 'bg-warning text-dark',
      REVERSAL: 'bg-danger'
    };
    return entryType ? (classes[entryType] ?? 'bg-secondary') : 'bg-secondary';
  }
}
