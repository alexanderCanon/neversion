import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MasterDashboardService } from '../services/master-dashboard.service';
import { AccountGroup, ProfileItem } from '../models/dashboard.model';
import { AccountRowComponent } from '../components/account-row/account-row.component';
import { ProfileRowComponent } from '../components/profile-row/profile-row.component';

@Component({
  selector: 'app-product-accounts-page',
  standalone: true,
  imports: [CommonModule, AccountRowComponent, ProfileRowComponent],
  template: `
    <div class="container-fluid py-4 max-w-7xl mx-auto">
      <!-- Minimalist Header -->
      <div class="d-flex align-items-center mb-5">
        <button class="btn btn-link text-decoration-none text-muted p-0 me-3 fs-5" (click)="goBack()" title="Volver">
          <i class="bi bi-arrow-left"></i>
        </button>
        <h2 class="mb-0 fw-bold fs-3" style="letter-spacing: -0.5px;">
          <i class="bi bi-hdd-network me-2 text-primary"></i> Cuentas del Producto
        </h2>
      </div>

      @if (isLoading()) {
        <div class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Cargando...</span>
          </div>
          <p class="mt-2 text-muted">Cargando cuentas...</p>
        </div>
      }

      @if (hasError()) {
        <div class="alert alert-danger d-flex align-items-center rounded-3 border-0" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          <div>No se pudieron cargar las cuentas. Intente nuevamente.</div>
        </div>
      }

      @if (!isLoading() && !hasError()) {
        @if (accounts().length === 0) {
          <div class="text-center py-5">
            <i class="bi bi-inbox fs-1 text-muted"></i>
            <p class="mt-2 text-muted">No hay cuentas para este producto.</p>
          </div>
        } @else {
          <!-- Accounts Container (Clean List) -->
          <div class="accounts-list-container">
            @for (account of accounts(); track account.accountId) {
              <div class="mb-4">
                <app-account-row
                  [account]="account"
                  [expanded]="expandedAccounts().has(account.accountId)"
                  (accountToggle)="toggleAccount($event)">
                </app-account-row>

                @if (expandedAccounts().has(account.accountId)) {
                  <div class="mt-2 mb-4 animate-fade-in" style="margin-left: 1.5rem;">
                    @if (profilesLoading().has(account.accountId)) {
                      <div class="text-center py-4">
                        <div class="spinner-border spinner-border-sm text-secondary"></div>
                        <small class="ms-2 text-muted">Cargando perfiles...</small>
                      </div>
                    } @else if (accountProfiles()[account.accountId]) {
                      <div class="table-responsive rounded-3 border">
                        <table class="table table-hover table-borderless align-middle mb-0">
                          <thead class="bg-light text-muted border-bottom" style="font-size: 0.85rem; letter-spacing: 0.5px;">
                            <tr>
                              <th class="fw-semibold px-3 py-2"><i class="bi bi-person-fill me-1"></i> Cliente</th>
                              <th class="fw-semibold py-2"><i class="bi bi-telephone-fill me-1"></i> Teléfono</th>
                              <th class="fw-semibold py-2"><i class="bi bi-list-task me-1"></i> Perfil</th>
                              <th class="fw-semibold py-2"><span class="me-1">#</span> PIN</th>
                              <th class="fw-semibold py-2">🗓️ Inicio</th>
                              <th class="fw-semibold py-2">🗓️ Vencimiento</th>
                              <th class="fw-semibold py-2">✅ Estado</th>
                            </tr>
                          </thead>
                          <tbody>
                            @for (profile of accountProfiles()[account.accountId]; track profile.profileId) {
                              <app-profile-row [profile]="profile" class="align-middle"></app-profile-row>
                            }
                          </tbody>
                        </table>
                      </div>
                    }
                  </div>
                }
              </div>
            }
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .max-w-7xl { max-width: 80rem; }
    .animate-fade-in { animation: fadeIn 0.3s ease-in-out; }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-5px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class ProductAccountsPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dashboardService = inject(MasterDashboardService);

  accounts = signal<AccountGroup[]>([]);
  isLoading = signal(false);
  hasError = signal(false);
  expandedAccounts = signal(new Set<string>());
  profilesLoading = signal(new Set<string>());
  accountProfiles = signal<Record<string, ProfileItem[]>>({});

  private productId = '';

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('productId') || '';
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.isLoading.set(true);
    this.hasError.set(false);
    this.dashboardService.getAccountsByProduct(this.productId).subscribe({
      next: (data) => {
        this.accounts.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }

  toggleAccount(accountId: string): void {
    const expanded = new Set(this.expandedAccounts());
    if (expanded.has(accountId)) {
      expanded.delete(accountId);
      this.expandedAccounts.set(expanded);
    } else {
      expanded.add(accountId);
      this.expandedAccounts.set(expanded);
      this.loadProfiles(accountId);
    }
  }

  private loadProfiles(accountId: string): void {
    if (this.accountProfiles()[accountId]) return;

    const loading = new Set(this.profilesLoading());
    loading.add(accountId);
    this.profilesLoading.set(loading);

    this.dashboardService.getProfilesByAccount(accountId).subscribe({
      next: (profiles) => {
        this.accountProfiles.update(prev => ({ ...prev, [accountId]: profiles }));
        const done = new Set(this.profilesLoading());
        done.delete(accountId);
        this.profilesLoading.set(done);
      },
      error: () => {
        const done = new Set(this.profilesLoading());
        done.delete(accountId);
        this.profilesLoading.set(done);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
