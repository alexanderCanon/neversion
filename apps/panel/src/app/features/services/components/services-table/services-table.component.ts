import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServiceResponse } from '@neversion/models';

@Component({
  selector: 'app-services-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './services-table.component.html',
  styleUrl: './services-table.component.scss'
})
export class ServicesTableComponent {
  @Input() services: ServiceResponse[] = [];
  @Input() isReadOnly = false;
  @Output() editService = new EventEmitter<ServiceResponse>();
  @Output() toggleStatus = new EventEmitter<string>();

  getCategoryBadgeClass(category: string | undefined): string {
    const cat = category?.toLowerCase();
    switch (cat) {
      case 'streaming':
        return 'text-bg-primary';
      case 'digital_service':
        return 'text-bg-success';
      default:
        return 'text-bg-secondary';
    }
  }

  getCategoryBadgeStyle(category: string | undefined): Record<string, string> {
     const cat = category?.toLowerCase();
     if (cat === 'streaming') return { 'background-color': '#4285f4', 'color': 'white' };
     if (cat === 'digital_service') return { 'background-color': '#34a853', 'color': 'white' };
     return {};
  }

  getCategoryLabel(category: string | undefined): string {
    if (!category) return '';
    const cat = category.toLowerCase();
    const labels: Record<string, string> = {
      streaming: 'streaming',
      digital_service: 'servicio digital'
    };
    return labels[cat] || cat;
  }

  getServiceInitials(name: string): string {
    if (!name) return '';
    const words = name.split(' ');
    if (words.length > 1) {
      return (words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }
}
