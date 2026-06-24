import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PlatformService } from '../../services/platform.service';
import { CartService } from '../../services/cart.service';
import { ImageService } from '../../services/image.service';
import { ToastService } from '../../services/toast.service';
import { Observable, combineLatest } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { ServiceResponse } from '@neversion/api-client';

@Component({
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
