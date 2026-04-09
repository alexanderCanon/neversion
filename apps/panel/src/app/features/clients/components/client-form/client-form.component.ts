import { Component, EventEmitter, Output, ViewChild, ElementRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClientRequest } from '../../models/client.model';
import { ClientsService } from '../../services/clients.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-client-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './client-form.component.html',
  styleUrls: [],
})
export class ClientFormComponent implements OnInit {
  @ViewChild('clientModal') modalElement!: ElementRef;
  @Output() clientCreated = new EventEmitter<ClientRequest>();

  clientForm!: FormGroup;
  isSubmitting = false;
  
  mode: 'CREATE' | 'EDIT' = 'CREATE';
  selectedClientId: string | null = null;

  private readonly fb = inject(FormBuilder);
  private readonly clientsService = inject(ClientsService);
  private readonly toastService = inject(ToastService);

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.clientForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.email, Validators.maxLength(255)]],
      phone: ['', [Validators.required, Validators.maxLength(50)]], // Phone is required now
    });
  }

  openModal(clientToEdit?: any): void {
    if (clientToEdit) {
      this.mode = 'EDIT';
      this.selectedClientId = clientToEdit.id;
      this.clientForm.patchValue({
        name: clientToEdit.name,
        email: clientToEdit.email,
        phone: clientToEdit.phone
      });
    } else {
      this.mode = 'CREATE';
      this.selectedClientId = null;
      this.clientForm.reset();
    }

    const modalEl = this.modalElement?.nativeElement;
    if (modalEl) {
      const bootstrap = (window as any).bootstrap;
      if (bootstrap) {
        const modal = new bootstrap.Modal(modalEl);
        modal.show();
      } else {
        modalEl.classList.add('show');
        modalEl.style.display = 'block';
        document.body.classList.add('modal-open');
        const backdrop = document.createElement('div');
        backdrop.classList.add('modal-backdrop', 'fade', 'show');
        document.body.appendChild(backdrop);
      }
    }
  }

  closeModal(): void {
    const modalEl = this.modalElement?.nativeElement;
    if (modalEl) {
      const bootstrap = (window as any).bootstrap;
      if (bootstrap) {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
      } else {
        modalEl.classList.remove('show');
        modalEl.style.display = 'none';
        document.body.classList.remove('modal-open');
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) backdrop.remove();
      }
    }
    this.resetForm();
  }

  onSubmit(): void {
    if (this.clientForm.valid) {
      this.isSubmitting = true;
      const formValue = this.clientForm.value;

      const clientRequest: ClientRequest = {
        name: formValue.name,
        email: formValue.email || undefined,
        phone: formValue.phone || undefined,
      };

      if (this.mode === 'EDIT' && this.selectedClientId) {
        this.clientsService.updateClient(this.selectedClientId, clientRequest).subscribe({
            next: () => {
                this.toastService.success('Cliente actualizado correctamente');
                this.clientCreated.emit(clientRequest);
                this.closeModal();
            },
            error: () => this.isSubmitting = false
        });
      } else {
        this.clientsService.createClient(clientRequest).subscribe({
            next: () => {
                this.toastService.success('Cliente registrado correctamente');
                this.clientCreated.emit(clientRequest);
                this.closeModal();
            },
            error: () => this.isSubmitting = false
        });
      }
    } else {
      Object.keys(this.clientForm.controls).forEach((key) => {
        this.clientForm.get(key)?.markAsTouched();
      });
    }
  }

  resetForm(): void {
    this.clientForm.reset();
    this.isSubmitting = false;
    this.mode = 'CREATE';
    this.selectedClientId = null;
  }
}

