import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-combo',
  templateUrl: './combo.component.html',
  styleUrls: ['./combo.component.css']
})
export class ComboComponent {
  private readonly router = inject(Router);

  // Combo tiers extracted from BR-13 documentation
  comboTiers = [
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

  startBuilding(): void {
    this.router.navigate(['/platforms']);
  }
}
