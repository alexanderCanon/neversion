import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map, of } from 'rxjs';
import {
  ClientsApiService,
  ClientDetailResponse as ApiClientDetailResponse,
  ClientDeletionCheckResponse,
  ClientRequest as ApiClientRequest,
  ClientResponse as ApiClientResponse,
  UpdateClientRequest as ApiUpdateClientRequest
} from '@neversion/api-client';
import { ClientsFilter, ClientRequest, ClientResponse, ClientDetail } from '@neversion/models';
import { AuthService } from '../../../core/services/auth.service';

@Injectable({ providedIn: 'root' })
export class ClientsService {
  private readonly clientsApi = inject(ClientsApiService);
  private readonly authService = inject(AuthService);

  private readonly _clients = signal<ClientResponse[]>([]);
  readonly clients = this._clients.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getClients(filter?: ClientsFilter): Observable<ClientResponse[]> {
    const vendorUuid = this.authService.currentVendorUuid();
    if (!vendorUuid) return of([]);

    this._isLoading.set(true);
    return this.clientsApi.listByVendor3(
      vendorUuid,
      filter?.name,
      filter?.phone,
      filter?.email,
      'body',
      false,
    ).pipe(
      map(apiClients => apiClients.map(api => this.mapToModel(api))),
      tap((clients) => this._clients.set(clients)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getClientById(id: string): Observable<ClientResponse> {
    return this.clientsApi.getById2(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  getClientDetail(id: string): Observable<ClientDetail> {
    return this.clientsApi.getDetail(id).pipe(
      map(api => this.mapDetailToModel(api))
    );
  }

  createClient(client: ClientRequest): Observable<ClientResponse> {
    const apiRequest: ApiClientRequest = {
      name: client.name,
      email: client.email,
      phone: client.phone,
      notes: client.notes
    };

    return this.clientsApi.create2(apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newClient) => {
        this._clients.update((current) => [...current, newClient]);
      })
    );
  }

  updateClient(id: string, client: ClientRequest): Observable<ClientResponse> {
    const apiRequest: ApiUpdateClientRequest = {
      name: client.name,
      phone: client.phone,
      notes: client.notes
    };

    return this.clientsApi.update2(id, apiRequest).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedClient) => {
            this._clients.update(current => 
                current.map(c => c.id === id ? updatedClient : c)
            );
        })
    );
  }

  checkDeletion(id: string): Observable<ClientDeletionCheckResponse> {
    return this.clientsApi.checkDeletion(id);
  }

  deleteClient(id: string): Observable<void> {
    return this.clientsApi.delete2(id).pipe(
      tap(() => {
        this._clients.update((current) => current.filter((c) => c.id !== id));
      })
    );
  }

  refreshClients(): Observable<ClientResponse[]> {
    return this.getClients();
  }

  private mapToModel(api: ApiClientResponse): ClientResponse {
    return {
      id: api.id || '',
      name: api.name || '',
      email: api.email || '',
      phone: api.phone || '',
      notes: api.notes || '',
      activeSubscriptionCount: api.activeSubscriptionCount || 0,
      createdAt: api.createdAt || ''
    };
  }

  private mapDetailToModel(api: ApiClientDetailResponse): ClientDetail {
    return {
      client: api.client ? this.mapToModel(api.client) : {} as ClientResponse,
      activeSubscriptions: (api.activeSubscriptions ?? []).map(s => ({
        id: s.id || '',
        serviceName: s.serviceName || '',
        profileName: s.profileName || '',
        paymentDueDate: s.paymentDueDate || '',
        status: s.status || ''
      })),
      orderHistory: (api.orderHistory ?? []).map(o => ({
        id: o.id || '',
        status: o.status || '',
        createdAt: o.createdAt || ''
      }))
    };
  }
}
