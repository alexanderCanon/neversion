import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { VendorsApiService, VendorsPublicApiService, VendorPublicResponse } from '@neversion/api-client';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

interface RewardsConfig {
  enabled: boolean;
  earn_pct: number;
}

@Component({
  selector: 'app-rewards-config',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './rewards-config.component.html',
  styleUrls: ['./rewards-config.component.scss']
})
export class RewardsConfigComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly vendorsApi = inject(VendorsApiService);
  private readonly vendorsPublicApi = inject(VendorsPublicApiService);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  readonly isLoading = signal<boolean>(false);
  readonly isSaving = signal<boolean>(false);

  configForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadConfig();
  }

  private initForm(): void {
    this.configForm = this.fb.group({
      enabled: [false],
      earnPct: [2, [Validators.required, Validators.min(0), Validators.max(100)]]
    });
  }

  private loadConfig(): void {
    const vendorUuid = this.authService.currentVendorUuid();
    if (!vendorUuid) {
      this.toastService.error('No se pudo identificar el vendedor actual');
      return;
    }

    this.isLoading.set(true);
    this.vendorsPublicApi.getByUuidVendorPublic(vendorUuid).subscribe({
      next: (vendor: VendorPublicResponse) => {
        this.isLoading.set(false);
        if (vendor.rewardsCfg) {
          try {
            const cfg = JSON.parse(vendor.rewardsCfg) as RewardsConfig;
            this.populateForm(cfg);
          } catch {
            this.toastService.error('La configuración actual tiene un formato inválido');
          }
        }
      },
      error: (err: unknown) => {
        this.isLoading.set(false);
        this.toastService.error('Error al cargar la configuración de recompensas');
        console.error(err);
      }
    });
  }

  private populateForm(cfg: RewardsConfig): void {
    this.configForm.patchValue({
      enabled: cfg.enabled,
      earnPct: cfg.earn_pct
    });
  }

  onSave(): void {
    if (this.configForm.invalid) {
      this.configForm.markAllAsTouched();
      this.toastService.error('Revisa los campos del formulario');
      return;
    }

    const formValue = this.configForm.value;
    const cfg: RewardsConfig = {
      enabled: formValue.enabled,
      earn_pct: formValue.earnPct
    };

    this.isSaving.set(true);
    this.vendorsApi.updateRewardsConfigVendor({ rewardsCfg: JSON.stringify(cfg) }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.toastService.success('Configuración de recompensas guardada correctamente');
      },
      error: (err: unknown) => {
        this.isSaving.set(false);
        const errorObj = err as { error?: { message?: string } };
        const msg = errorObj?.error?.message || 'Error al guardar la configuración';
        this.toastService.error(msg);
        console.error(err);
      }
    });
  }
}
