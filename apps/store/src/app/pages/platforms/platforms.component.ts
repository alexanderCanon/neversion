import { Component, OnInit, inject } from '@angular/core';
import { PlatformService } from '../../services/platform.service';
import { CartService } from '../../services/cart.service';
import { ImageService } from '../../services/image.service';
import { Observable } from 'rxjs';
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

  platforms$!: Observable<ServiceResponse[]>;

  ngOnInit(): void {
    this.platforms$ = this._platformService.getPlatforms();
  }

  addToCart(service: ServiceResponse, type: 'PROFILE' | 'COMPLETE'): void {
    this._cartService.addToCart(service, type);
    const planName = type === 'PROFILE' ? 'Perfil Individual' : 'Cuenta Completa';
    this._toastService.show(`${service.name} (${planName}) añadido al carrito`, 'success', 'Carrito Actualizado');
  }

  resolveImageUrl(url?: string): string {
    return this._imageService.resolveServiceImageUrl(url);
  }

  resolveImageUrl(url?: string): string {
    return this._imageService.resolveServiceImageUrl(url);
  }
}
