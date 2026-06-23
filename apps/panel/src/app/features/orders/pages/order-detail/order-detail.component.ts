import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { OrdersService } from '../../services/orders.service';
import { OrderDetailResponse, SuggestAssignmentResponse } from '@neversion/api-client';
import { ToastService } from '../../../../core/services/toast.service';
import { ReceiptImageService } from '../../../../core/services/receipt-image.service';
import { AssignmentsService } from '../../../assignments/services/assignments.service';
import { ProfileService } from '../../../accounts/services/profile.service';
import { ProfileResponse } from '@neversion/models';
import { getOrderStatusLabel, getOrderStatusClass } from '@neversion/utils';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.scss'
  })
export class OrderDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly ordersService = inject(OrdersService);
  private readonly toastService = inject(ToastService);
  readonly receiptImageService = inject(ReceiptImageService);
  private readonly assignmentsService = inject(AssignmentsService);
  private readonly profileService = inject(ProfileService);

  order = signal<OrderDetailResponse | null>(null);
  isLoading = signal(true);

  suggestion = signal<SuggestAssignmentResponse | null>(null);
  isLoadingSuggestion = signal(false);

  availableProfiles = signal<ProfileResponse[]>([]);
  selectedProfileId = signal<string>('');
  isConfirming = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadOrder(id);
    }
  }

  loadOrder(id: string): void {
    this.isLoading.set(true);
    this.ordersService.getOrderById(id).subscribe({
      next: (data) => {
        this.order.set(data);
        this.isLoading.set(false);
        if (data.status === 'VALIDATED') {
            this.loadSuggestion(data.id!);
        }
      },
      error: () => {
        this.toastService.error('Error al cargar la orden');
        this.router.navigate(['/orders']);
      }
    });
  }

  loadSuggestion(orderId: string): void {
      this.isLoadingSuggestion.set(true);
      this.assignmentsService.suggestAssignment(orderId).subscribe({
          next: (res) => {
              this.suggestion.set(res);
              if (res.hasSuggestion && res.suggestedAccountId) {
                  this.loadProfilesForAccount(res.suggestedAccountId, res.suggestedProfileId);
              }
              this.isLoadingSuggestion.set(false);
          },
          error: () => {
              this.isLoadingSuggestion.set(false);
              this.toastService.error('No se pudo cargar la sugerencia de asignación');
          }
      });
  }

  loadProfilesForAccount(accountId: string, suggestedProfileId?: string): void {
      this.profileService.getProfilesByAccount(accountId, true).subscribe({
          next: (profiles) => {
              this.availableProfiles.set(profiles);
              if (suggestedProfileId) {
                  this.selectedProfileId.set(suggestedProfileId);
              }
          },
          error: () => {
              this.toastService.error('Error al cargar perfiles disponibles');
          }
      });
  }

  confirmAssignment(): void {
      const orderId = this.order()?.id;
      const profileId = this.selectedProfileId();

      if (!orderId || !profileId) return;

      this.isConfirming.set(true);
      this.assignmentsService.confirmAssignment(orderId, profileId).subscribe({
          next: () => {
              this.isConfirming.set(false);
              this.toastService.success('Asignación confirmada y accesos enviados.');
              this.loadOrder(orderId);
          },
          error: () => {
              this.isConfirming.set(false);
              this.toastService.error('Error al confirmar la asignación');
          }
      });
  }

  getStatusLabel(status: string | undefined): string {
    return getOrderStatusLabel(status);
  }

  getStatusBadgeClass(status: string | undefined): string {
    return getOrderStatusClass(status);
  }
}
