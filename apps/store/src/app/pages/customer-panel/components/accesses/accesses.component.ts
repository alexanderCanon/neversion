import { Component, OnInit } from '@angular/core';
import { SubscriptionsApiService, SubscriptionResponse } from '@neversion/api-client';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-customer-accesses',
  templateUrl: './accesses.component.html',
  styleUrls: []
})
export class AccessesComponent implements OnInit {
  subscriptions: SubscriptionResponse[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(
    private subscriptionsApi: SubscriptionsApiService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.id) {
        this.loadAccesses(user.id);
      } else {
        this.isLoading = false;
        this.error = 'Usuario no autenticado.';
      }
    });
  }

  loadAccesses(clientId: string): void {
    this.isLoading = true;
    this.error = null;

    // Call the API with status ACTIVE and the specific clientId
    // Note: If the backend supports fetching "my subscriptions" via a different endpoint, it would be preferred,
    // but the API client only has `list` which accepts clientId.
    this.subscriptionsApi.list('ACTIVE', clientId).subscribe({
      next: (subs) => {
        this.subscriptions = subs;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching accesses:', err);
        this.error = 'Ocurrió un error al cargar los accesos.';
        this.isLoading = false;
      }
    });
  }
}
