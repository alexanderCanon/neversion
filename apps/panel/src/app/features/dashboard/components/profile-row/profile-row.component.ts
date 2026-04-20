import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileItem } from '@neversion/models';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';

@Component({
  selector: 'app-profile-row',
  standalone: true,
  imports: [CommonModule, PhonePipe],
  template: `
      <td class="px-3">
        @if (profile.subscription) {
          <span class="fw-medium text-dark">{{ profile.subscription.customer.name }}</span>
        } @else {
          <span class="text-muted fst-italic">Perfil libre</span>
        }
      </td>
      <td>
        @if (profile.subscription) {
          <span class="text-muted text-nowrap">{{ profile.subscription.customer.phone | phone }}</span>
        } @else {
          <span class="text-muted">—</span>
        }
      </td>
      <td>
        <span class="fw-medium">{{ profile.profileName || '—' }}</span>
      </td>
      <td>
        <span class="font-monospace text-muted">{{ profile.pin || '—' }}</span>
      </td>
      <td>
        @if (profile.subscription) {
          <span class="text-muted">{{ profile.subscription.startDate }}</span>
        } @else {
          <span class="text-muted">—</span>
        }
      </td>
      <td>
        @if (profile.subscription) {
          <span class="text-muted">{{ profile.subscription.endDate }}</span>
        } @else {
          <span class="text-muted">—</span>
        }
      </td>
      <td>
        @if (profile.subscription) {
          <span class="badge rounded-pill fw-normal" [ngClass]="statusClass" style="font-size: 0.8rem;">
            {{ statusLabel }}
          </span>
        } @else {
           <span class="badge rounded-pill bg-light text-secondary border fw-normal" style="font-size: 0.8rem;">Libre</span>
        }
      </td>
  `,
  styles: [`
    :host {
      display: table-row;
      vertical-align: middle;
    }
  `]
})
export class ProfileRowComponent {
  @Input({ required: true }) profile!: ProfileItem;

  get statusLabel(): string {
    if (!this.profile.subscription) return '';
    switch (this.profile.subscription.status) {
      case 'ACTIVE': return 'Activo';
      case 'EXPIRED': return 'Vencido';
      case 'EXPIRING_SOON': return 'Vence pronto';
      case 'CANCELLED': return 'Cancelado';
      case 'SUSPENDED': return 'Suspendido';
      default: return this.profile.subscription.status;
    }
  }

  get statusClass(): string {
    if (!this.profile.subscription) return '';
    switch (this.profile.subscription.status) {
      case 'ACTIVE': return 'bg-success-subtle text-success border border-success-subtle';
      case 'EXPIRED': return 'bg-danger-subtle text-danger border border-danger-subtle';
      case 'EXPIRING_SOON': return 'bg-warning-subtle text-warning-emphasis border border-warning-subtle';
      case 'CANCELLED': return 'bg-secondary-subtle text-secondary border border-secondary-subtle';
      case 'SUSPENDED': return 'bg-dark-subtle text-dark border border-dark-subtle';
      default: return 'bg-light text-secondary border';
    }
  }
}
