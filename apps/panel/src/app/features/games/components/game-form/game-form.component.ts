import { Component, Output, EventEmitter, ViewChild, ElementRef, PLATFORM_ID, inject, signal, Renderer2 } from '@angular/core';
import { CommonModule, isPlatformBrowser, DOCUMENT } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GameRequest, GameResponse } from '@neversion/models';
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
  selector: 'app-game-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './game-form.component.html',
  styleUrl: './game-form.component.scss'
})
export class GameFormComponent {
  @ViewChild('gameModal') modalElement!: ElementRef;

  @Output() saveGame = new EventEmitter<GameRequest>();

  private readonly fb = inject(FormBuilder);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly storageService = inject(StorageService);
  private readonly toastService = inject(ToastService);
  private readonly renderer = inject(Renderer2);
  private readonly document = inject(DOCUMENT);

  gameForm: FormGroup;
  isBrowser: boolean;
  isEditMode = false;
  editingGameId: string | null = null;
  isUploading = signal(false);
  private manualBackdrop: HTMLElement | null = null;

  constructor() {
    this.isBrowser = isPlatformBrowser(this.platformId);

    this.gameForm = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(25)]],
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      price: [0, [Validators.required, Validators.min(0)]],
      imageUrl: ['']
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      // 1. Validar tamaño (máximo 2MB)
      const maxSize = 2 * 1024 * 1024;
      if (file.size > maxSize) {
        this.toastService.error('El archivo es demasiado grande. El límite es de 2MB.');
        input.value = '';
        return;
      }

      // 2. Validar tipo MIME
      const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/svg+xml'];
      if (!allowedTypes.includes(file.type)) {
        this.toastService.error('Formato no permitido. Solo se permiten imágenes (JPG, PNG, WEBP, GIF, SVG).');
        input.value = '';
        return;
      }

      this.isUploading.set(true);
      const sanitizedName = file.name.replace(/\s+/g, '_');
      const fileName = `${Date.now()}_${sanitizedName}`;

      // Guardar URL previa para borrar del storage si existe
      const previousUrl = this.gameForm.get('imageUrl')?.value;

      this.storageService.uploadGameImage(file, fileName)
        .pipe(finalize(() => this.isUploading.set(false)))
        .subscribe({
          next: (url) => {
            this.gameForm.patchValue({ imageUrl: url });
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
      const parts = url.split('/games/');
      if (parts.length > 1) {
        const fileName = parts[parts.length - 1];
        this.storageService.deleteGameImage(fileName).subscribe({
          next: () => console.log('Previous game image deleted from storage:', fileName),
          error: (err) => console.error('Failed to delete old game image from storage:', err)
        });
      }
    } catch (e) {
      console.error('Error parsing previous game image URL:', e);
    }
  }

  openModal(game?: GameResponse): void {
    if (game) {
      this.isEditMode = true;
      this.editingGameId = game.id;
      this.gameForm.patchValue({
        code: game.code,
        name: game.name,
        price: game.price,
        imageUrl: game.imageUrl
      });
    } else {
      this.isEditMode = false;
      this.editingGameId = null;
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
    if (this.gameForm.valid) {
      const formValue = this.gameForm.value;
      const request: GameRequest = {
        code: formValue.code,
        name: formValue.name,
        price: Number(formValue.price),
        imageUrl: formValue.imageUrl
      };
      
      this.saveGame.emit(request);
      this.closeModal();
    } else {
      Object.keys(this.gameForm.controls).forEach(key => {
        this.gameForm.get(key)?.markAsTouched();
      });
    }
  }

  resetForm(): void {
    this.gameForm.reset({
      code: '',
      name: '',
      price: 0,
      imageUrl: ''
    });
  }
}
