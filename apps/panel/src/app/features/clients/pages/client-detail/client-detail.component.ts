import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, Location } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ClientsService } from '../../services/clients.service';
import { ClientDetail } from '@neversion/models';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';
import { ToastService } from '../../../../core/services/toast.service';
import {
  getSubscriptionStatusLabel,
  getSubscriptionStatusClass,
  getOrderStatusLabel,
  getOrderStatusClass
} from '@neversion/utils';

@Component({
  selector: 'app-client-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, PhonePipe, DatePipe],
  templateUrl: './client-detail.component.html',
  styleUrl: './client-detail.component.scss'
  })
export class ClientDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly clientsService = inject(ClientsService);
  private readonly toastService = inject(ToastService);
  private readonly location = inject(Location);

  clientDetail = signal<ClientDetail | null>(null);
  isLoading = signal<boolean>(true);
  error = signal<string | null>(null);
  activeTab = signal<'INFO' | 'SUBS' | 'ORDERS'>('INFO');

  goBack(): void {
    this.location.back();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadClientDetail(id);
    } else {
      this.error.set('No se proporcionó un ID de cliente válido.');
      this.isLoading.set(false);
    }
  }

  private loadClientDetail(id: string): void {
    this.isLoading.set(true);
    this.clientsService.getClientDetail(id).subscribe({
      next: (detail) => {
        this.clientDetail.set(detail);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading client detail', err);
        this.error.set('Ocurrió un error al cargar los detalles del cliente.');
        this.toastService.error('Error al cargar detalles del cliente');
        this.isLoading.set(false);
      }
    });
  }

  setTab(tab: 'INFO' | 'SUBS' | 'ORDERS'): void {
    this.activeTab.set(tab);
  }

  getStatusLabel(status: string | undefined): string {
    return getSubscriptionStatusLabel(status);
  }

  getStatusClass(status: string | undefined): string {
    return getSubscriptionStatusClass(status);
  }

  getOrderStatusLabel(status: string | undefined): string {
    return getOrderStatusLabel(status);
  }

  getOrderStatusClass(status: string | undefined): string {
    return getOrderStatusClass(status);
  }
}
