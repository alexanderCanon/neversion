import { Component, Output, EventEmitter, ViewChild, ElementRef, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ServiceRequest } from '../../models/service.model';

@Component({
  selector: 'app-service-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './service-form.component.html',
  styleUrls: []
})
export class ServiceFormComponent implements OnInit {
  @ViewChild('serviceModal') modalElement!: ElementRef;
  
  @Output() saveService = new EventEmitter<ServiceRequest>();
  
  serviceForm: FormGroup;
  categories = ['STREAMING', 'SOFTWARE', 'GIFT_CARD', 'RECHARGE', 'DIGITAL_SERVICE'];
  isBrowser: boolean;
  
  constructor(
      private fb: FormBuilder,
      @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    
    this.serviceForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      imageUrl: [''],
      category: ['STREAMING', Validators.required],
      maxProfiles: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {}

  openModal(): void {
    this.resetForm();
    if (this.isBrowser) {
        const modalEl = this.modalElement?.nativeElement;
        if(modalEl) {
           const bootstrap = (window as any).bootstrap;
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
           const bootstrap = (window as any).bootstrap;
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
