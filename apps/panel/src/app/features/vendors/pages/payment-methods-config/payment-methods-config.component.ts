import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { BankAccount } from '@neversion/models';
import { VendorPaymentMethodsService } from '../../services/vendor-payment-methods.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-payment-methods-config',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './payment-methods-config.component.html',
  styleUrls: ['./payment-methods-config.component.scss']
})
export class PaymentMethodsConfigComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly paymentMethodsService = inject(VendorPaymentMethodsService);
  private readonly toastService = inject(ToastService);

  readonly isLoading = signal<boolean>(false);
  readonly isSaving = signal<boolean>(false);

  form!: FormGroup;

  readonly popularBanks = [
    'Banrural',
    'Banco Industrial',
    'BAC Credomatic',
    'GyT Continental',
    'BAM',
    'InterBanco',
    'Banco Promerica',
    'Banco Inmobiliario'
  ];

  readonly accountTypes = [
    'Ahorro en Quetzales',
    'Monetaria en Quetzales',
    'Ahorro en Dólares',
    'Monetaria en Dólares',
    'Billetera Móvil / Depósito Express'
  ];

  ngOnInit(): void {
    this.initForm();
    this.loadAccounts();
  }

  private initForm(): void {
    this.form = this.fb.group({
      accounts: this.fb.array([])
    });
  }

  get accounts(): FormArray {
    return this.form.get('accounts') as FormArray;
  }

  createAccountGroup(account?: Partial<BankAccount>): FormGroup {
    return this.fb.group({
      bank: [account?.bank || '', [Validators.required, Validators.minLength(2)]],
      accountType: [account?.accountType || 'Ahorro en Quetzales', [Validators.required]],
      accountNumber: [account?.accountNumber || '', [Validators.required, Validators.minLength(3)]],
      holder: [account?.holder || '', [Validators.required, Validators.minLength(3)]]
    });
  }

  addAccount(): void {
    this.accounts.push(this.createAccountGroup());
  }

  removeAccount(index: number): void {
    this.accounts.removeAt(index);
  }

  private loadAccounts(): void {
    this.isLoading.set(true);
    this.paymentMethodsService.getBankAccounts().subscribe({
      next: (accountsList) => {
        this.isLoading.set(false);
        this.accounts.clear();
        if (accountsList && accountsList.length > 0) {
          accountsList.forEach((acc) => this.accounts.push(this.createAccountGroup(acc)));
        } else {
          // If empty, add a default blank account for convenience
          this.addAccount();
        }
      },
      error: (err: unknown) => {
        this.isLoading.set(false);
        this.toastService.error('Error al cargar los métodos de pago actuales');
        console.error(err);
      }
    });
  }

  onSave(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.error('Por favor completa los campos requeridos de cada cuenta');
      return;
    }

    const rawAccounts = this.accounts.value as BankAccount[];
    const cleanedAccounts: BankAccount[] = rawAccounts.map((a) => ({
      bank: a.bank.trim(),
      accountType: a.accountType.trim(),
      accountNumber: a.accountNumber.trim(),
      holder: a.holder.trim()
    }));

    this.isSaving.set(true);
    this.paymentMethodsService.saveBankAccounts(cleanedAccounts).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.toastService.success('Métodos de pago guardados correctamente');
      },
      error: (err: unknown) => {
        this.isSaving.set(false);
        const errorObj = err as { error?: { message?: string } };
        const msg = errorObj?.error?.message || 'Error al guardar los métodos de pago';
        this.toastService.error(msg);
        console.error(err);
      }
    });
  }
}
