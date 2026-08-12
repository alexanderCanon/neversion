import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  MasterDashboardService,
  ProfitMarginsResult,
  AccountProfitMarginResult,
  ServiceProfitSummaryResult
} from '../../../dashboard/services/master-dashboard.service';


@Component({
  selector: 'app-profit-margins',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profit-margins.component.html',
  styleUrl: './profit-margins.component.scss'
})
export class ProfitMarginsComponent implements OnInit {
  private readonly dashboardService = inject(MasterDashboardService);

  readonly data = signal<ProfitMarginsResult | null>(null);
  readonly isLoading = signal(false);
  readonly error = signal<string | null>(null);

  readonly selectedYear = signal(new Date().getFullYear());
  readonly selectedMonth = signal(new Date().getMonth() + 1);
  readonly expandedService = signal<string | null>(null);

  readonly monthLabels = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];

  readonly availableMonths = computed(() => {
    const now = new Date();
    const months: { year: number; month: number; label: string }[] = [];
    for (let i = 0; i < 12; i++) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      months.push({
        year: d.getFullYear(),
        month: d.getMonth() + 1,
        label: `${this.monthLabels[d.getMonth()]} ${d.getFullYear()}`
      });
    }
    return months;
  });

  readonly currentMonthLabel = computed(() => {
    const y = this.selectedYear();
    const m = this.selectedMonth();
    return `${this.monthLabels[m - 1]} ${y}`;
  });

  readonly grandTotal = computed(() => this.data()?.grandTotal ?? null);
  readonly serviceSummaries = computed(() => this.data()?.serviceSummaries ?? []);
  readonly accountMargins = computed(() => this.data()?.accountMargins ?? []);

  readonly accountsByService = computed(() => {
    const accounts = this.accountMargins();
    const map = new Map<string, AccountProfitMarginResult[]>();
    for (const a of accounts) {
      const key = a.serviceName ?? 'Sin servicio';
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(a);
    }
    return map;
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.dashboardService.getAccountProfitMargins(
      this.selectedYear(),
      this.selectedMonth()
    ).subscribe({
      next: (result) => {
        this.data.set(result);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Error al cargar los márgenes de ganancia');
        this.isLoading.set(false);
      }
    });
  }

  onMonthChange(value: string): void {
    const [y, m] = value.split('-').map(Number);
    this.selectedYear.set(y);
    this.selectedMonth.set(m);
    this.expandedService.set(null);
    this.load();
  }

  toggleService(serviceName: string): void {
    this.expandedService.set(
      this.expandedService() === serviceName ? null : serviceName
    );
  }

  isExpanded(serviceName: string): boolean {
    return this.expandedService() === serviceName;
  }

  getAccountsForService(serviceName: string): AccountProfitMarginResult[] {
    return this.accountsByService().get(serviceName) ?? [];
  }

  formatCurrency(value: number | undefined | null): string {
    if (value == null) return 'Q0.00';
    return `Q${Number(value).toFixed(2)}`;
  }

  formatPct(value: number | undefined | null): string {
    if (value == null) return '0%';
    return `${Number(value).toFixed(1)}%`;
  }

  getProfitClass(value: number | undefined | null): string {
    if (value == null || value === 0) return 'profit-neutral';
    return value > 0 ? 'profit-positive' : 'profit-negative';
  }

  getMarginClass(value: number | undefined | null): string {
    if (value == null || value === 0) return 'margin-neutral';
    if (value >= 50) return 'margin-excellent';
    if (value >= 30) return 'margin-good';
    if (value >= 10) return 'margin-fair';
    return 'margin-poor';
  }

  trackByServiceName(index: number, item: ServiceProfitSummaryResult): string {
    return item.serviceName ?? `svc-${index}`;
  }

  trackByAccountUuid(index: number, item: AccountProfitMarginResult): string {
    return item.accountUuid ?? `acc-${index}`;
  }

  get selectedMonthValue(): string {
    return `${this.selectedYear()}-${this.selectedMonth()}`;
  }
}
