import { Component, OnInit, signal, computed, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServicesDataService } from '../../services/services-data.service';
import { ServiceResponse, ServiceRequest } from '@neversion/models';
import { ServicesTableComponent } from '../../components/services-table/services-table.component';
import { ServiceFormComponent } from '../../components/service-form/service-form.component';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-services-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ServicesTableComponent, ServiceFormComponent],
  templateUrl: './services-list.component.html',
  styleUrls: ['./services-list.component.scss']
})
export class ServicesListComponent implements OnInit {
  @ViewChild(ServiceFormComponent) serviceModal!: ServiceFormComponent;

  private readonly servicesDataService = inject(ServicesDataService);
  private readonly authService = inject(AuthService);

  isSuperAdmin = computed(() => this.authService.userRole() === 'super_admin');

  services = this.servicesDataService.services;
  isLoading = this.servicesDataService.isLoading;

  // Filters
  searchTerm = signal('');
  selectedCategory = signal('');

  // Computed state
  filteredServices = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const category = this.selectedCategory();
    
    return this.services().filter(s => {
      const matchName = term ? (s.name?.toLowerCase().includes(term) ?? false) : true;
      const matchCategory = category ? (s.category === category) : true;
      return matchName && matchCategory;
    });
  });

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {
    this.servicesDataService.getServices().subscribe();
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  onCategoryChange(category: string): void {
    this.selectedCategory.set(category);
  }

  openNewServiceModal(): void {
    if (this.serviceModal) {
      this.serviceModal.openModal();
    }
  }

  onEditService(service: ServiceResponse): void {
    if (this.serviceModal) {
      this.serviceModal.openModal(service);
    }
  }

  onToggleStatus(id: string): void {
    this.servicesDataService.toggleServiceStatus(id).subscribe();
  }

  onSaveService(request: ServiceRequest): void {
    const editId = this.serviceModal.editingServiceId;

    if (editId) {
      this.servicesDataService.updateService(editId, request).subscribe({
        next: () => this.loadServices(),
        error: (err) => console.error('Failed to update service', err)
      });
    } else {
      this.servicesDataService.createService(request).subscribe({
        next: () => this.loadServices(),
        error: (err) => console.error('Failed to create service', err)
      });
    }
  }
}
