import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ServicesDataService } from '../../../services/services/services-data.service';
import { ServiceResponse } from '@neversion/models';

@Component({
  selector: 'app-service-selector',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './service-selector.component.html',
  styleUrl: './service-selector.component.scss'
})
export class ServiceSelectorComponent implements OnInit {
  private readonly servicesDataService = inject(ServicesDataService);
  private readonly router = inject(Router);

  readonly services = signal<ServiceResponse[]>([]);
  readonly isLoading = signal(true);

  ngOnInit(): void {
    this.servicesDataService.getServices({ isActive: true }).subscribe({
      next: (services) => {
        this.services.set(services);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  selectService(service: ServiceResponse): void {
    this.router.navigate(['/accounts', service.id]);
  }

  getCategoryIcon(category: string): string {
    switch (category) {
      case 'STREAMING': return 'bi-play-circle-fill';
      case 'SOFTWARE': return 'bi-laptop';
      case 'GIFT_CARD': return 'bi-gift-fill';
      case 'RECHARGE': return 'bi-phone-fill';
      case 'DIGITAL_SERVICE': return 'bi-cloud-fill';
      default: return 'bi-box';
    }
  }

  getCategoryLabel(category: string): string {
    const labels: Record<string, string> = {
      STREAMING: 'Streaming',
      SOFTWARE: 'Software',
      GIFT_CARD: 'Gift Card',
      RECHARGE: 'Recarga',
      DIGITAL_SERVICE: 'Servicio Digital'
    };
    return labels[category] || category;
  }
}
