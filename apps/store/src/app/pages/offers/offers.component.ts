import { Component, OnInit, inject } from '@angular/core';
import { PlatformService } from '../../services/platform.service';
import { ImageService } from '../../services/image.service';
import { CartService } from '../../services/cart.service';
import { ToastService } from '../../services/toast.service';
import { VendorService } from '../../services/vendor.service';
import { ServiceResponse } from '@neversion/api-client';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  standalone: false,
  selector: 'app-offers',
  templateUrl: './offers.component.html',
  styleUrls: ['./offers.component.css']
})
export class OffersComponent implements OnInit {
  private readonly platformService = inject(PlatformService);
  private readonly imageService = inject(ImageService);
  private readonly cartService = inject(CartService);
  private readonly toastService = inject(ToastService);
  private readonly vendorService = inject(VendorService);

  offers$!: Observable<ServiceResponse[]>;

  comboTiers: { qty: string; discount: string; label: string }[] = [];

  ngOnInit(): void {
    const cfg = this.vendorService.getDiscountConfig();
    if (cfg && cfg.tiers.length > 0) {
      this.comboTiers = cfg.tiers.map((tier, idx) => ({
        qty: `${tier.count}`,
        discount: `${tier.discountPct}%`,
        label: idx === 0 ? 'Ahorro Inicial' : (idx === cfg.tiers.length - 1 ? 'Super Ahorro' : `Nivel ${idx + 1}`)
      }));
    } else {
      this.comboTiers = [
        { qty: '2 - 3', discount: '5%', label: 'Ahorro Inicial' },
        { qty: '4+', discount: '10%', label: 'Super Ahorro' }
      ];
    }

    this.offers$ = this.platformService.getPlatforms().pipe(
      map(services => services.slice(0, 3))
    );
  }

  resolveImageUrl(url?: string): string {
    return this.imageService.resolveServiceImageUrl(url);
  }

  addToCart(service: ServiceResponse, type: 'PROFILE' | 'COMPLETE'): void {
    const result = this.cartService.addToCart(service, type);
    if (!result.ok) {
      this.toastService.show(result.message || 'No se pudo agregar al carrito', 'danger', 'Error');
      return;
    }
    const planName = type === 'PROFILE' ? 'Perfil Individual' : 'Cuenta Completa';
    this.toastService.show(`${service.name} (${planName}) añadido al carrito`, 'success', '¡Oferta Añadida!');
  }
}
