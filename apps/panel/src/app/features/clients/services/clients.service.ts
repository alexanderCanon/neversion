import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, finalize, map } from 'rxjs';
import { ClientRequest as ApiClientRequest, ClientResponse as ApiClientResponse } from '@neversion/api-client';
import { ClientsFilter, ClientRequest, ClientResponse, ClientDetail } from '@neversion/models';
import { runtimeConfig } from '../../../core/config/runtime-config';

@Injectable({ providedIn: 'root' })
export class ClientsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = runtimeConfig.apiUrl;

  private readonly _clients = signal<ClientResponse[]>([]);
  readonly clients = this._clients.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getClients(filter?: ClientsFilter): Observable<ClientResponse[]> {
    let params = new HttpParams();
    if (filter?.name) params = params.set('name', filter.name);
    if (filter?.phone) params = params.set('phone', filter.phone);

    this._isLoading.set(true);
    return this.http.get<ApiClientResponse[]>(`${this.baseUrl}/clients`, { params }).pipe(
      map(apiClients => apiClients.map(api => this.mapToModel(api))),
      tap((clients) => this._clients.set(clients)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getClientById(id: string): Observable<ClientResponse> {
    return this.http.get<ApiClientResponse>(`${this.baseUrl}/clients/${id}`).pipe(
      map(api => this.mapToModel(api))
    );
  }

  getClientDetail(id: string): Observable<ClientDetail> {
    return this.http.get<unknown>(`${this.baseUrl}/clients/${id}/detail`).pipe(
      map(apiUnknown => {
        const api = apiUnknown as Record<string, unknown>;
        return {
          client: api['client'] ? this.mapToModel(api['client'] as unknown as ApiClientResponse) : {} as ClientResponse,
          activeSubscriptions: ((api['activeSubscriptions'] || []) as Record<string, string>[]).map(s => ({
            id: s['id'] || '',
            serviceName: s['serviceName'] || '',
            profileName: s['profileName'] || '',
            paymentDueDate: s['paymentDueDate'] || '',
            status: s['status'] || ''
          })),
          orderHistory: ((api['orderHistory'] || []) as Record<string, string>[]).map(o => ({
            id: o['id'] || '',
            status: o['status'] || '',
            createdAt: o['createdAt'] || ''
          }))
        };
      })
    );
  }

  createClient(client: ClientRequest): Observable<ClientResponse> {
    const apiRequest: ApiClientRequest = {
      name: client.name,
      email: client.email,
      phone: client.phone,
      notes: client.notes
    };

    return this.http.post<ApiClientResponse>(`${this.baseUrl}/clients`, apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newClient) => {
        this._clients.update((current) => [...current, newClient]);
      })
    );
  }

  updateClient(id: string, client: ClientRequest): Observable<ClientResponse> {
    const apiRequest: ApiClientRequest = {
      name: client.name,
      email: client.email,
      phone: client.phone,
      notes: client.notes
    };

    return this.http.put<ApiClientResponse>(`${this.baseUrl}/clients/${id}`, apiRequest).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedClient) => {
            this._clients.update(current => 
                current.map(c => c.id === id ? updatedClient : c)
            );
        })
    );
  }

  deleteClient(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/clients/${id}`).pipe(
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
      id: api.id || (api as unknown as Record<string, string>)['uuid'] || '', // Handle potential fallback if 'uuid' was used in some backend responses
      name: api.name || '',
      email: api.email || '',
      phone: api.phone || '',
      notes: api.notes || '',
      activeSubscriptionCount: api.activeSubscriptionCount || 0,
      createdAt: api.createdAt || ''
    };
  }
}
