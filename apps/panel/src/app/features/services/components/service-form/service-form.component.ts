import { Component, Output, EventEmitter, ViewChild, ElementRef, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ServiceRequest } from '@neversion/models';

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
  styleUrls: []
})
export class ServiceFormComponent {
  @ViewChild('serviceModal') modalElement!: ElementRef;
  
  @Output() saveService = new EventEmitter<ServiceRequest>();
  
  private readonly fb = inject(FormBuilder);
  private readonly platformId = inject(PLATFORM_ID);

  serviceForm: FormGroup;
  categories = ['STREAMING', 'SOFTWARE', 'GIFT_CARD', 'RECHARGE', 'DIGITAL_SERVICE'];
  isBrowser: boolean;
  
  constructor() {
    this.isBrowser = isPlatformBrowser(this.platformId);
    
    this.serviceForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      imageUrl: [''],
      category: ['STREAMING', Validators.required],
      maxProfiles: [1, [Validators.required, Validators.min(1)]]
    });
  }

  openModal(): void {
    this.resetForm();
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
               modalEl.classList.remove('show');
               modalEl.style.display = 'none';
               document.body.classList.remove('modal-open');
               
               const backdrop = document.querySelector('.modal-backdrop');
               if(backdrop) backdrop.remove();
           }
        }
        this.resetForm();
     }
  }

  onSubmit(): void {
    if (this.serviceForm.valid) {
      const formValue = this.serviceForm.value;
      const request: ServiceRequest = {
        name: formValue.name,
        maxProfiles: Number(formValue.maxProfiles),
        details: {
          description: formValue.description,
          imageUrl: formValue.imageUrl,
          category: formValue.category
        }
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
    if (this.serviceForm) {
      this.serviceForm.reset({
        category: 'STREAMING',
        maxProfiles: 1
      });
    }
  }
}
