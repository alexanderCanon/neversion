import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PlatformService } from '../../services/platform.service';
import { CartService } from '../../services/cart.service';
import { ImageService } from '../../services/image.service';
import { ToastService } from '../../services/toast.service';
import { Observable, combineLatest } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { ServiceResponse } from '@alexandercanon/api-client-angular';

@Component({
  standalone: false,
  selector: 'app-platforms',
  templateUrl: './platforms.component.html',
  styleUrls: ['./platforms.component.css'],
})
export class PlatformsComponent implements OnInit {

  private readonly _platformService = inject(PlatformService);
  private readonly _cartService = inject(CartService);
  private readonly _imageService = inject(ImageService);
  private readonly _toastService = inject(ToastService);
  private readonly _route = inject(ActivatedRoute);

  platforms$!: Observable<ServiceResponse[]>;
  searchQuery$!: Observable<string>;

  activePlatformId: string | null = null;
  addingState: { [key: string]: 'idle' | 'adding' | 'success' | 'error' } = {};
  addingErrorMessage: { [key: string]: string } = {};

  cartHasProfile$: Observable<boolean> = this._cartService.items$.pipe(
    map(items => items.some(i => i.type === 'PROFILE'))
  );
  cartHasComplete$: Observable<boolean> = this._cartService.items$.pipe(
    map(items => items.some(i => i.type === 'COMPLETE'))
  );

  ngOnInit(): void {
    const allPlatforms$ = this._platformService.getPlatforms();
    this.searchQuery$ = this._route.queryParams.pipe(
      map(params => (params['q'] || '').trim())
    );

    this.platforms$ = combineLatest([allPlatforms$, this.searchQuery$]).pipe(
      map(([platforms, query]) => {
        if (!query) return platforms;
        const lower = query.toLowerCase();
        return platforms.filter(p =>
          (p.name?.toLowerCase().includes(lower)) ||
          (p.category?.toLowerCase().includes(lower)) ||
          (p.description?.toLowerCase().includes(lower))
        );
      })
    );
  }

  openCartOverlay(platformId: string | undefined, event: Event): void {
    if (!platformId) return;
    event.stopPropagation();
    this.activePlatformId = platformId;
    this.addingState[platformId] = 'idle';
  }

  closeCartOverlay(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.activePlatformId = null;
  }

  confirmAddToCart(service: ServiceResponse, type: 'PROFILE' | 'COMPLETE', event: Event): void {
    event.stopPropagation();
    const serviceId = service.id;
    if (!serviceId) return;

    // Set loading state
    this.addingState[serviceId] = 'adding';

    // Tiny delay for smooth animation flow
    setTimeout(() => {
      const result = this._cartService.addToCart(service, type);
      if (!result.ok) {
        this.addingState[serviceId] = 'error';
        this.addingErrorMessage[serviceId] = result.message || 'No se pudo agregar al carrito';
        
        // Auto revert to idle after 3.5s
        setTimeout(() => {
          if (this.addingState[serviceId] === 'error') {
            this.addingState[serviceId] = 'idle';
          }
        }, 3500);
        return;
      }

      // Success state
      this.addingState[serviceId] = 'success';

      // Smoothly close overlay after 1200ms
      setTimeout(() => {
        if (this.activePlatformId === serviceId) {
          this.closeCartOverlay();
        }
        // Reset state after animation finishes
        setTimeout(() => {
          this.addingState[serviceId] = 'idle';
        }, 300);
      }, 1200);
    }, 600);
  }

  getAddingState(id?: string): 'idle' | 'adding' | 'success' | 'error' {
    return id ? (this.addingState[id] || 'idle') : 'idle';
  }

  getAddingErrorMessage(id?: string): string {
    return id ? (this.addingErrorMessage[id] || '') : '';
  }

  addToCart(service: ServiceResponse, type: 'PROFILE' | 'COMPLETE'): void {
    const result = this._cartService.addToCart(service, type);
    if (!result.ok) {
      this._toastService.show(result.message || 'No se pudo agregar al carrito', 'danger', 'Error');
      return;
    }
    const planName = type === 'PROFILE' ? 'Perfil Individual' : 'Cuenta Completa';
    this._toastService.show(`${service.name} (${planName}) añadido al carrito`, 'success', 'Carrito Actualizado');
  }

  resolveImageUrl(url?: string): string {
    return this._imageService.resolveServiceImageUrl(url);
  }
}
