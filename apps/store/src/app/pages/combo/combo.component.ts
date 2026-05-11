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
      title: 'Combo Bronce', 
      description: 'Ideal para parejas o familias pequeñas.',
      icon: 'bi-gem'
    },
    { 
      qty: '4+', 
      discount: '10%', 
      title: 'Combo Oro', 
      description: 'El máximo ahorro para los verdaderos fans del streaming.',
      icon: 'bi-trophy-fill'
    }
  ];

  startBuilding(): void {
    this.router.navigate(['/platforms']);
  }
}
