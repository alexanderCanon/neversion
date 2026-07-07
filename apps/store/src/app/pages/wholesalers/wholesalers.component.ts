import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-wholesalers',
  templateUrl: './wholesalers.component.html',
  styleUrls: ['./wholesalers.component.css']
})
export class WholesalersComponent {
  private readonly router = inject(Router);

  benefits = [
    {
      title: 'Precios de Distribuidor',
      description: 'Accede a tarifas preferenciales diseñadas para maximizar tu margen de ganancia.',
      icon: 'bi-graph-up-arrow'
    },
    {
      title: 'Soporte Prioritario',
      description: 'Atención personalizada y rápida para resolver cualquier duda o inconveniente técnico.',
      icon: 'bi-headset'
    },
    {
      title: 'Panel Multi-vendedor',
      description: 'Gestiona tus propias ventas y clientes con herramientas profesionales (Próximamente).',
      icon: 'bi-grid-1x2-fill'
    },
    {
      title: 'Garantía Extendida',
      description: 'Cobertura total en todos los servicios para que vendas con total tranquilidad.',
      icon: 'bi-patch-check-fill'
    }
  ];

  contactSupport(): void {
    this.router.navigate(['/contact']);
  }
}
