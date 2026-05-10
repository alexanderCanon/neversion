import { Component, Output, EventEmitter, ViewChild, ElementRef, PLATFORM_ID, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ServiceRequest, ServiceResponse } from '@neversion/models';
import { StorageService } from '../../../../core/services/storage.service';
import { finalize } from 'rxjs';

interface BootstrapModal {
  show(): void;
  hide(): void;
}

interface Bootstrap {
  Modal: {
    new (el: HTMLElement): BootstrapModal;
    getInstance(el: HTMLElement): BootstrapModal | null;
  };
}

@Component({
  selector: 'app-service-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './service-form.component.html',
  styleUrl: './service-form.component.scss'
})
export class ServiceFormComponent {
  @ViewChild('serviceModal') modalElement!: ElementRef;

  @Output() saveService = new EventEmitter<ServiceRequest>();

  private readonly fb = inject(FormBuilder);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly storageService = inject(StorageService);

  serviceForm: FormGroup;
  categories = ['STREAMING', 'SOFTWARE', 'GIFT_CARD', 'RECHARGE', 'DIGITAL_SERVICE'];
  isBrowser: boolean;
  isEditMode = false;
  editingServiceId: string | null = null;
  isUploading = signal(false);

  constructor() {
    this.isBrowser = isPlatformBrowser(this.platformId);

    this.serviceForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      imageUrl: [''],
      category: ['STREAMING', Validators.required],
      maxProfiles: [1, [Validators.required, Validators.min(1)]],
      priceProfile: [0, [Validators.required, Validators.min(0)]],
      priceComplete: [0, [Validators.required, Validators.min(0)]],
      durationDays: [30, [Validators.required, Validators.min(1)]]
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.isUploading.set(true);
      const fileName = `${Date.now()}_${file.name}`;

      this.storageService.uploadServiceImage(file, fileName)
        .pipe(finalize(() => this.isUploading.set(false)))
        .subscribe({
          next: (url) => {
            this.serviceForm.patchValue({ imageUrl: url });
          },
          error: (err) => {
            console.error('Upload failed', err);
          }
        });
    }
  }

  openModal(service?: ServiceResponse): void {    if (service) {
      this.isEditMode = true;
      this.editingServiceId = service.id;
      this.serviceForm.patchValue({
        name: service.name,
        description: service.description,
        imageUrl: service.imageUrl,
        category: service.category,
        maxProfiles: service.maxProfiles,
        priceProfile: service.priceProfile,
        priceComplete: service.priceComplete,
        durationDays: service.durationDays
      });
    } else {
      this.isEditMode = false;
      this.editingServiceId = null;
      this.resetForm();
    }

    if (this.isBrowser) {
        const modalEl = this.modalElement?.nativeElement;
        if(modalEl) {
           const bootstrap = (window as unknown as { bootstrap: Bootstrap }).bootstrap;
           if(bootstrap) {
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
  }

  closeModal(): void {
     if (this.isBrowser) {
        const modalEl = this.modalElement?.nativeElement;
        if(modalEl) {
           const bootstrap = (window as unknown as { bootstrap: Bootstrap }).bootstrap;
           if(bootstrap) {
               const modal = bootstrap.Modal.getInstance(modalEl);
               if(modal) modal.hide();
           } else {
               modalEl.classList.add('show');
               modalEl.style.display = 'none';
               document.body.classList.remove('modal-open');
               
               const backdrop = document.querySelector('.modal-backdrop');
               if(backdrop) backdrop.remove();
           }
        }
     }
     this.resetForm();
  }

  onSubmit(): void {
    if (this.serviceForm.valid) {
      const formValue = this.serviceForm.value;
      const request: ServiceRequest = {
        name: formValue.name,
        category: formValue.category,
        maxProfiles: Number(formValue.maxProfiles),
        priceProfile: Number(formValue.priceProfile),
        priceComplete: Number(formValue.priceComplete),
        durationDays: Number(formValue.durationDays),
        description: formValue.description,
        imageUrl: formValue.imageUrl
      };
      
      this.saveService.emit(request);
      this.closeModal();
    } else {
      Object.keys(this.serviceForm.controls).forEach(key => {
        this.serviceForm.get(key)?.markAsTouched();
      });
    }
  }

  resetForm(): void {
    this.serviceForm.reset({
      category: 'STREAMING',
      maxProfiles: 1,
      priceProfile: 0,
      priceComplete: 0,
      durationDays: 30
    });
  }
}
