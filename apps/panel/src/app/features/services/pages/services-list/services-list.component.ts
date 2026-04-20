import { Component, OnInit, signal, computed, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServicesDataService } from '../../services/services-data.service';
import { ServiceResponse, ServiceRequest } from '../../models/service.model';
import { ServicesTableComponent } from '../../components/services-table/services-table.component';
import { ServiceFormComponent } from '../../components/service-form/service-form.component';

@Component({
  selector: 'app-services-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ServicesTableComponent, ServiceFormComponent],
  templateUrl: './services-list.component.html',
  styleUrls: ['./services-list.component.scss']
})
export class ServicesListComponent implements OnInit {
  @ViewChild(ServiceFormComponent) newServiceModal!: ServiceFormComponent;

  private readonly servicesDataService = inject(ServicesDataService);

  services = signal<ServiceResponse[]>([]);
  isLoading = signal(true);

  // Filters
  searchTerm = signal('');
  selectedCategory = signal('');

  // Computed state
  filteredServices = computed(() => {
    const term = this.searchTerm().toLowerCase();
    
    return this.services().filter(s => {
      const matchName = s.name?.toLowerCase().includes(term) ?? false;
      return matchName;
    });
  });

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {
    this.isLoading.set(true);
    this.servicesDataService.getServices().subscribe({
      next: (data) => {
        this.services.set(data);
        this.isLoading.set(false);
      },
      error: (err: unknown) => {
        console.error('Failed to fetch services', err);
        this.isLoading.set(false);
      }
    });
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  onCategoryChange(event: Event): void {
    const element = event.target as HTMLSelectElement;
    this.selectedCategory.set(element.value);
  }

  openNewServiceModal(): void {
    if (this.newServiceModal) {
      this.newServiceModal.openModal();
    }
  }

  onSaveService(request: ServiceRequest): void {
    this.servicesDataService.createService(request).subscribe({
      next: () => {
        this.loadServices();
      },
      error: (err: unknown) => {
        console.error('Failed to create service', err);
        alert('Error al crear el servicio. Verifique su conexión y vuelva a intentarlo.');
        this.loadServices(); // reload in case it passed something
      }
    });
  }
}
