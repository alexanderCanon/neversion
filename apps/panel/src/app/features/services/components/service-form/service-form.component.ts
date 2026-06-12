import { Component, Output, EventEmitter, ViewChild, ElementRef, PLATFORM_ID, inject, signal, Renderer2 } from '@angular/core';
import { CommonModule, isPlatformBrowser, DOCUMENT } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ServiceRequest, ServiceResponse } from '@neversion/models';
import { StorageService } from '../../../../core/services/storage.service';
import { ToastService } from '../../../../core/services/toast.service';
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
  private readonly toastService = inject(ToastService);
  private readonly renderer = inject(Renderer2);
  private readonly document = inject(DOCUMENT);

  serviceForm: FormGroup;
  categoryDetails = [
    {
      value: 'streaming',
      label: 'streaming',
      description: 'Todas las plataformas para visualizar contenido multimedia como películas, videos y música (Netflix, Disney, YouTube, Crunchyroll, etc.)'
    },
    {
      value: 'digital_service',
      label: 'digital_service',
      description: 'Suscripciones a otros servicios digitales como herramientas de productividad o entretenimiento (Canva, ChatGPT Plus, Microsoft 365, etc.).'
    }
  ];
  isBrowser: boolean;
  isEditMode = false;
  editingServiceId: string | null = null;
  isUploading = signal(false);
  private manualBackdrop: HTMLElement | null = null;

  constructor() {
    this.isBrowser = isPlatformBrowser(this.platformId);

    this.serviceForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      imageUrl: [''],
      category: ['streaming', Validators.required],
      maxProfiles: [1, [Validators.required, Validators.min(1)]],
      priceProfile: [0, [Validators.required, Validators.min(0)]],
      priceComplete: [0, [Validators.required, Validators.min(0)]],
      durationDays: [30, [Validators.required, Validators.min(1)]]
    });

    // Escuchar cambios de categoría para ajustar campos dinámicamente
    this.serviceForm.get('category')?.valueChanges.subscribe(value => {
      this.adjustFieldsForCategory(value);
    });
  }

  getSelectedCategoryDescription(): string {
    const selected = this.serviceForm?.get('category')?.value;
    const detail = this.categoryDetails.find(c => c.value === selected);
    return detail ? detail.description : '';
  }

  showDurationDays(): boolean {
    const category = this.serviceForm?.get('category')?.value;
    return category === 'streaming' || category === 'digital_service';
  }

  private adjustFieldsForCategory(category: string): void {
    if (category !== 'streaming') {
      // digital_service: solo 1 perfil, precio mínimo requerido por @Positive
      this.serviceForm.patchValue({
        maxProfiles: 1,
        priceProfile: 1
      }, { emitEvent: false });
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      // 1. Validar tamaño (máximo 2MB)
      const maxSize = 2 * 1024 * 1024;
      if (file.size > maxSize) {
        this.toastService.error('El archivo es demasiado grande. El límite es de 2MB.');
        input.value = ''; // Reset input
        return;
      }

      // 2. Validar tipo MIME
      const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/svg+xml'];
      if (!allowedTypes.includes(file.type)) {
        this.toastService.error('Formato no permitido. Solo se permiten imágenes (JPG, PNG, WEBP, GIF, SVG).');
        input.value = ''; // Reset input
        return;
      }

      this.isUploading.set(true);
      const sanitizedName = file.name.replace(/\s+/g, '_');
      const fileName = `${Date.now()}_${sanitizedName}`;

      // Guardar URL previa para borrar del storage si existe
      const previousUrl = this.serviceForm.get('imageUrl')?.value;

      this.storageService.uploadServiceImage(file, fileName)
        .pipe(finalize(() => this.isUploading.set(false)))
        .subscribe({
          next: (url) => {
            this.serviceForm.patchValue({ imageUrl: url });
            this.toastService.success('Imagen subida correctamente');

            // Si se subió con éxito y había una imagen previa, borrar la antigua del storage
            if (previousUrl) {
              this.deleteOldImageFromStorage(previousUrl);
            }
          },
          error: (err) => {
            console.error('Upload failed', err);
            this.toastService.error('Error al subir la imagen');
          }
        });
    }
  }

  private deleteOldImageFromStorage(url: string): void {
    try {
      const parts = url.split('/services/');
      if (parts.length > 1) {
        const fileName = parts[parts.length - 1];
        this.storageService.deleteServiceImage(fileName).subscribe({
          next: () => console.log('Previous image deleted from storage:', fileName),
          error: (err) => console.error('Failed to delete old image from storage:', err)
        });
      }
    } catch (e) {
      console.error('Error parsing previous image URL:', e);
    }
  }

  openModal(service?: ServiceResponse): void {    if (service) {
      this.isEditMode = true;
      this.editingServiceId = service.id;
      const normalizedCategory = service.category ? service.category.toLowerCase() : 'streaming';
      this.serviceForm.patchValue({
        name: service.name,
        description: service.description,
        imageUrl: service.imageUrl,
        category: normalizedCategory,
        maxProfiles: service.maxProfiles,
        priceProfile: service.priceProfile,
        priceComplete: service.priceComplete,
        durationDays: service.durationDays
      });
      this.adjustFieldsForCategory(normalizedCategory);
    } else {
      this.isEditMode = false;
      this.editingServiceId = null;
      this.resetForm();
      this.adjustFieldsForCategory('streaming');
    }

    if (this.isBrowser) {
        const modalEl = this.modalElement?.nativeElement;
        if(modalEl) {
           const bootstrap = (window as unknown as { bootstrap: Bootstrap }).bootstrap;
           if(bootstrap) {
               const modal = new bootstrap.Modal(modalEl);
               modal.show();
           } else {
               this.renderer.addClass(modalEl, 'show');
               this.renderer.setStyle(modalEl, 'display', 'block');
               this.renderer.addClass(this.document.body, 'modal-open');
               
               this.manualBackdrop = this.renderer.createElement('div') as HTMLElement;
               this.renderer.addClass(this.manualBackdrop, 'modal-backdrop');
               this.renderer.addClass(this.manualBackdrop, 'fade');
               this.renderer.addClass(this.manualBackdrop, 'show');
               this.renderer.appendChild(this.document.body, this.manualBackdrop);
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
               this.renderer.removeClass(modalEl, 'show');
               this.renderer.setStyle(modalEl, 'display', 'none');
               this.renderer.removeClass(this.document.body, 'modal-open');
               
               if (this.manualBackdrop) {
                   this.renderer.removeChild(this.document.body, this.manualBackdrop);
                   this.manualBackdrop = null;
               } else {
                   const backdrop = this.document.querySelector('.modal-backdrop');
                   if (backdrop) {
                       this.renderer.removeChild(this.document.body, backdrop);
                   }
               }
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
      category: 'streaming',
      maxProfiles: 1,
      priceProfile: 0,
      priceComplete: 0,
      durationDays: 30
    });
  }
}
