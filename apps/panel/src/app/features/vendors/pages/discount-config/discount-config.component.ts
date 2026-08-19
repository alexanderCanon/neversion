import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { VendorsApiService, VendorProfileResponse } from '@neversion/api-client';
import { ToastService } from '../../../../core/services/toast.service';

interface DiscountTier {
  count: number;
  discount_pct: number;
}

interface DiscountConfig {
  min_items: number;
  max_items: number;
  round_to: number;
  tiers: DiscountTier[];
}

@Component({
  selector: 'app-discount-config',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './discount-config.component.html',
  styleUrls: ['./discount-config.component.scss']
})
export class DiscountConfigComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly vendorsApi = inject(VendorsApiService);
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
      minItems: [2, [Validators.required, Validators.min(2)]],
      maxItems: [4, [Validators.required, Validators.max(4)]],
      roundTo: [5, [Validators.required, Validators.min(1)]],
      tiers: this.fb.array([])
    });
  }

  private loadConfig(): void {
    this.isLoading.set(true);
    this.vendorsApi.meVendor().subscribe({
      next: (vendor: VendorProfileResponse) => {
        this.isLoading.set(false);
        if (vendor.discountCfg) {
          try {
            const cfg = JSON.parse(vendor.discountCfg) as DiscountConfig;
            this.populateForm(cfg);
          } catch {
            this.toastService.error('La configuración actual tiene un formato inválido');
          }
        }
      },
      error: (err: unknown) => {
        this.isLoading.set(false);
        this.toastService.error('Error al cargar la configuración de descuentos');
        console.error(err);
      }
    });
  }

  private populateForm(cfg: DiscountConfig): void {
    this.configForm.patchValue({
      minItems: cfg.min_items,
      maxItems: cfg.max_items,
      roundTo: cfg.round_to
    });

    const tiersArray = this.tiers;
    tiersArray.clear();
    cfg.tiers.forEach(tier => {
      tiersArray.push(this.fb.group({
        count: [tier.count, [Validators.required, Validators.min(1)]],
        discountPct: [tier.discount_pct, [Validators.required, Validators.min(0), Validators.max(100)]]
      }));
    });
  }

  get tiers(): FormArray {
    return this.configForm.get('tiers') as FormArray;
  }

  addTier(): void {
    this.tiers.push(this.fb.group({
      count: [this.tiers.length + 2, [Validators.required, Validators.min(1)]],
      discountPct: [0, [Validators.required, Validators.min(0), Validators.max(100)]]
    }));
  }

  removeTier(index: number): void {
    this.tiers.removeAt(index);
  }

  onSave(): void {
    if (this.configForm.invalid) {
      this.configForm.markAllAsTouched();
      this.toastService.error('Revisa los campos del formulario');
      return;
    }

    const formValue = this.configForm.value;
    const cfg: DiscountConfig = {
      min_items: formValue.minItems,
      max_items: formValue.maxItems,
      round_to: formValue.roundTo,
      tiers: formValue.tiers.map((t: { count: number; discountPct: number }) => ({
        count: t.count,
        discount_pct: t.discountPct
      }))
    };

    this.isSaving.set(true);
    this.vendorsApi.updateDiscountConfigVendor({ discountCfg: JSON.stringify(cfg) }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.toastService.success('Configuración de descuentos guardada correctamente');
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
