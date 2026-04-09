import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccountGroup } from '../../models/dashboard.model';

@Component({
  selector: 'app-account-row',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="account-row-container py-2 px-3 cursor-pointer rounded-2 mb-1" (click)="toggle.emit(account.accountId)" [class.expanded]="expanded">
      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div class="d-flex align-items-center gap-2 text-truncate">
          <i class="bi text-muted" [class.bi-caret-down-fill]="expanded" [class.bi-caret-right-fill]="!expanded" style="font-size: 0.8rem;"></i>
          <span class="fs-6 fw-semibold mb-0 text-truncate" style="letter-spacing: -0.2px;">
            📧 {{ account.email }} <span class="text-muted fw-normal mx-2">🔑 {{ account.password }}</span> <span class="text-muted fw-normal">🗓️ {{ account.cutOffDate || 'N/A' }}</span>
          </span>
        </div>
        <div class="d-flex align-items-center gap-2 flex-shrink-0">
          <span class="badge fw-normal" [ngClass]="availabilityClass" style="font-size: 0.75rem;">
            {{ availabilityLabel }}
          </span>
          <span class="badge bg-light text-secondary border fw-normal" style="font-size: 0.75rem;">
            {{ account.occupiedProfiles }}/{{ account.maxProfiles }}
          </span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .cursor-pointer { cursor: pointer; transition: background-color 0.1s ease; }
    .cursor-pointer:hover { background-color: rgba(0, 0, 0, 0.03); }
    .account-row-container { 
      border-left: 3px solid transparent; 
      transition: all 0.2s ease;
    }
    .account-row-container.expanded {
      background-color: rgba(0,0,0,0.02);
      border-left-color: #495057;
    }
  `]
})
export class AccountRowComponent {
  @Input({ required: true }) account!: AccountGroup;
  @Input() expanded = false;
  @Output() toggle = new EventEmitter<string>();

  get availabilityLabel(): string {
    switch (this.account.availability) {
      case 'PARTIAL': return 'Parcial';
      case 'NO_AVAILABILITY': return 'Sin disponibilidad';
      case 'INDIVIDUAL': return 'Individual';
      case 'COMPLETE': return 'Completa';
      default: return this.account.availability;
    }
  }

  get availabilityClass(): string {
    switch (this.account.availability) {
      case 'PARTIAL': return 'bg-warning text-dark';
      case 'NO_AVAILABILITY': return 'bg-danger';
      case 'INDIVIDUAL': return 'bg-info';
      case 'COMPLETE': return 'bg-success';
      default: return 'bg-secondary';
    }
  }
}
