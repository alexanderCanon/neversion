import { Component, OnInit, inject, signal, computed, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientsService } from '../../services/clients.service';
import { ClientResponse } from '../../models/client.model';
import { ClientFormComponent } from '../../components/client-form/client-form.component';
import { ToastService } from '../../../../core/services/toast.service';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';

import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-clients-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ClientFormComponent, PhonePipe],
  templateUrl: './clients-list.component.html',
  styleUrls: [],
})
export class ClientsListComponent implements OnInit {
  @ViewChild('clientForm') clientForm!: ClientFormComponent;

  private readonly clientsService = inject(ClientsService);
  private readonly toastService = inject(ToastService);

  readonly clients = this.clientsService.clients;
  readonly isLoading = this.clientsService.isLoading;

  searchTerm = signal('');
  currentPage = signal(1);
  pageSize = 10;

  readonly filteredClients = computed(() => {
    const term = this.searchTerm().toLowerCase();
    if (!term) return this.clients();

    return this.clients().filter(
      (u) =>
        u.name?.toLowerCase().includes(term) ||
        u.email?.toLowerCase().includes(term) ||
        u.phone?.includes(term)
    );
  });

  readonly paginatedClients = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredClients().slice(start, start + this.pageSize);
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.filteredClients().length / this.pageSize)
  );

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.clientsService.getClients().subscribe();
  }

  onClientCreated(): void {
    this.loadClients();
  }

  deactivateClient(client: ClientResponse): void {
    if (confirm(`¿Seguro que deseas desactivar al cliente: ${client.name}?`)) {
      this.clientsService.deleteClient(client.id).subscribe({
        next: () => {
          this.toastService.success(`Cliente ${client.name} eliminado`);
        },
      });
    }
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
    this.currentPage.set(1);
  }

  prevPage(): void {
    if (this.currentPage() > 1) {
      this.currentPage.update((p) => p - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages()) {
      this.currentPage.update((p) => p + 1);
    }
  }

  trackByClientId(index: number, client: ClientResponse): string {
    return client.id;
  }
}
