import { Component, OnInit, signal, computed, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServicesDataService } from '../../services/services-data.service';
import { ServiceResponse } from '../../models/service.model';
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

  services = signal<ServiceResponse[]>([]);
  isLoading = signal(true);

  // Filters
  searchTerm = signal('');
  selectedCategory = signal('');

  // Computed state
  filteredServices = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const category = this.selectedCategory();
    
    return this.services().filter(s => {
      const matchName = s.name?.toLowerCase().includes(term) ?? false;
      return matchName;
    });
  });

  constructor(private servicesDataService: ServicesDataService) {}

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
      error: (err: any) => {
        console.error('Failed to fetch services', err);
        this.isLoading.set(false);
      }
    });
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  onCategoryChange(event: any): void {
    this.selectedCategory.set(event.target.value);
  }

  openNewServiceModal(): void {
    if (this.newServiceModal) {
      this.newServiceModal.openModal();
    }
  }

  onSaveService(request: any): void {
    this.servicesDataService.createService(request).subscribe({
      next: () => {
        this.loadServices();
      },
      error: (err: any) => {
        console.error('Failed to create service', err);
        alert('Error al crear el servicio. Verifique su conexión y vuelva a intentarlo.');
        this.loadServices(); // reload in case it passed something
      }
    });
  }
}
