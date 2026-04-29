import { Component, OnInit } from '@angular/core';
import { ClientsApiService, ClientAccessResponse } from '@neversion/api-client';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-customer-accesses',
  templateUrl: './accesses.component.html',
  styleUrls: []
})
export class AccessesComponent implements OnInit {
  subscriptions: ClientAccessResponse[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(
    private clientsApi: ClientsApiService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.id) {
        this.loadAccesses();
      } else {
        this.isLoading = false;
        this.error = 'Usuario no autenticado.';
      }
    });
  }

  loadAccesses(): void {
    this.isLoading = true;
    this.error = null;

    this.clientsApi.getMyAccesses().subscribe({
      next: (accesses) => {
        this.subscriptions = accesses;
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
