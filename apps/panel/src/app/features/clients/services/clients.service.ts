import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map } from 'rxjs';
import {
  ClientsApiService,
  ClientDetailResponse as ApiClientDetailResponse,
  ClientDeletionCheckResponse,
  ClientRequest as ApiClientRequest,
  ClientResponse as ApiClientResponse,
  UpdateClientRequest as ApiUpdateClientRequest
} from '@alexandercanon/api-client-angular';
import { ClientsFilter, ClientRequest, ClientResponse, ClientDetail } from '@neversion/models';

@Injectable({ providedIn: 'root' })
export class ClientsService {
  private readonly clientsApi = inject(ClientsApiService);

  private readonly _clients = signal<ClientResponse[]>([]);
  readonly clients = this._clients.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getClients(filter?: ClientsFilter): Observable<ClientResponse[]> {
    this._isLoading.set(true);
    return this.clientsApi.listClientsClient(
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
    return this.clientsApi.getByIdClient(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  getClientDetail(id: string): Observable<ClientDetail> {
    return this.clientsApi.getDetailClient(id).pipe(
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

    return this.clientsApi.createClient(apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap(newClient => {
        this._clients.update(current => [newClient, ...current]);
      })
    );
  }

  updateClient(id: string, client: ClientRequest): Observable<ClientResponse> {
    const apiRequest: ApiUpdateClientRequest = {
      name: client.name,
      phone: client.phone,
      notes: client.notes
    };

    return this.clientsApi.updateClient(id, apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap(updatedClient => {
        this._clients.update(current =>
          current.map(c => (c.id === id ? updatedClient : c))
        );
      })
    );
  }

  checkDeletion(id: string): Observable<ClientDeletionCheckResponse> {
    return this.clientsApi.checkDeletionClient(id);
  }

  checkClientDeletion(id: string): Observable<ClientDeletionCheckResponse> {
    return this.clientsApi.checkDeletionClient(id);
  }

  deleteClient(id: string): Observable<void> {
    return this.clientsApi.deleteClient(id).pipe(
      tap(() => {
        this._clients.update(current => current.filter(c => c.id !== id));
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
    const client = api.client ? this.mapToModel(api.client) : {
      id: '',
      name: '',
      email: '',
      phone: '',
      notes: '',
      activeSubscriptionCount: 0,
      createdAt: ''
    };

    return {
      client,
      orderHistory: (api.orderHistory || []).map(o => ({
        id: o.id || '',
        status: o.status || 'PENDING',
        createdAt: o.createdAt || ''
      })),
      activeSubscriptions: (api.activeSubscriptions || []).map(s => ({
        id: s.id || '',
        serviceName: s.serviceName || '',
        profileName: s.profileName || '',
        status: s.status || 'PENDING',
        paymentDueDate: s.paymentDueDate || ''
      }))
    };
  }
}
