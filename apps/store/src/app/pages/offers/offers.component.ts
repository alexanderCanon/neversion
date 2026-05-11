import { Component, OnInit, inject } from '@angular/core';
import { PlatformService } from '../../services/platform.service';
import { ImageService } from '../../services/image.service';
import { CartService } from '../../services/cart.service';
import { ToastService } from '../../services/toast.service';
import { ServiceResponse } from '@neversion/api-client';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-offers',
  templateUrl: './offers.component.html',
  styleUrls: ['./offers.component.css']
})
export class OffersComponent implements OnInit {
  private readonly platformService = inject(PlatformService);
  private readonly imageService = inject(ImageService);
  private readonly cartService = inject(CartService);
  private readonly toastService = inject(ToastService);

  offers$!: Observable<ServiceResponse[]>;

  // Combo tiers extracted from BR-13 documentation
  comboTiers = [
    { qty: '2 - 3', discount: '5%', label: 'Ahorro Inicial' },
    { qty: '4+', discount: '10%', label: 'Super Ahorro' }
  ];

  ngOnInit(): void {
    // For now, we simulate offers by taking a subset of active services
    this.offers$ = this.platformService.getPlatforms().pipe(
      map(services => services.slice(0, 3)) // Taking first 3 as "special offers"
    );
  }

  resolveImageUrl(url?: string): string {
    return this.imageService.resolveServiceImageUrl(url);
  }

  addToCart(service: ServiceResponse, type: 'PROFILE' | 'COMPLETE'): void {
    this.cartService.addToCart(service, type);
    const planName = type === 'PROFILE' ? 'Perfil Individual' : 'Cuenta Completa';
    this.toastService.show(`${service.name} (${planName}) añadido al carrito`, 'success', '¡Oferta Añadida!');
  }
}
