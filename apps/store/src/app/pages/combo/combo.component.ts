import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { VendorService, DiscountTier } from '../../services/vendor.service';

interface ComboTierDisplay {
  qty: string;
  discount: string;
  title: string;
  description: string;
}

@Component({
  standalone: false,
  selector: 'app-combo',
  templateUrl: './combo.component.html',
  styleUrls: ['./combo.component.css']
})
export class ComboComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly vendorService = inject(VendorService);

  comboTiers: ComboTierDisplay[] = [];

  ngOnInit(): void {
    const cfg = this.vendorService.getDiscountConfig();
    if (cfg && cfg.tiers.length > 0) {
      this.comboTiers = cfg.tiers.map((tier, idx) => ({
        qty: `${tier.count}`,
        discount: `${tier.discountPct}%`,
        title: `Descuento del ${tier.discountPct}%`,
        description: this.buildTierDescription(tier, idx, cfg.tiers)
      }));
    } else {
      this.comboTiers = [
        {
          qty: '2 - 3',
          discount: '5%',
          title: 'Descuento del 5%',
          description: 'Aplica de forma automática al comprar 2 o 3 servicios en el mismo pedido. Ideal para tus plataformas indispensables.'
        },
        {
          qty: '4+',
          discount: '10%',
          title: 'Descuento del 10%',
          description: 'Obtén el máximo beneficio al adquirir 4 o más servicios. Disfruta de cobertura total para todo tu entretenimiento.'
        }
      ];
    }
  }

  private buildTierDescription(tier: DiscountTier, idx: number, allTiers: DiscountTier[]): string {
    const next = allTiers[idx + 1];
    if (next) {
      return `Aplica de forma automática al comprar ${tier.count} servicios en el mismo pedido. Ideal para tus plataformas indispensables.`;
    }
    return `Obtén el máximo beneficio al adquirir ${tier.count} o más servicios. Disfruta de cobertura total para todo tu entretenimiento.`;
  }

  startBuilding(): void {
    this.router.navigate(['/platforms']);
  }
}
