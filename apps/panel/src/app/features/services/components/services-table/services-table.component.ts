import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServiceResponse } from '@neversion/models';

@Component({
  selector: 'app-services-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './services-table.component.html',
  styleUrls: []
})
export class ServicesTableComponent {
  @Input() services: ServiceResponse[] = [];
  @Output() editService = new EventEmitter<ServiceResponse>();
  @Output() toggleStatus = new EventEmitter<string>();

  getCategoryBadgeClass(category: string | undefined): string {
    switch (category) {
      case 'STREAMING':
        return 'text-bg-primary'; // Blue-ish
      case 'DIGITAL_SERVICE':
        return 'text-bg-success'; // Green-ish
      case 'SOFTWARE':
        return 'text-bg-purple'; // Needs custom CSS, fallback to info/primary or custom
      case 'GIFT_CARD':
        return 'text-bg-warning';
      case 'RECHARGE':
        return 'text-bg-danger';
      default:
        return 'text-bg-secondary';
    }
  }

  getCategoryBadgeStyle(category: string | undefined): Record<string, string> {
     if(category === 'STREAMING') return { 'background-color': '#4285f4', 'color': 'white' };
     if(category === 'DIGITAL_SERVICE') return { 'background-color': '#34a853', 'color': 'white' };
     if(category === 'SOFTWARE') return { 'background-color': '#a142f4', 'color': 'white' };
     return {};
  }
}
