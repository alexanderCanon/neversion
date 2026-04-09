import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, finalize } from 'rxjs';
import { ClientRequest, ClientResponse, ClientsFilter } from '../models/client.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClientsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  private readonly _clients = signal<ClientResponse[]>([]);
  readonly clients = this._clients.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getClients(filter?: ClientsFilter): Observable<ClientResponse[]> {
    let params = new HttpParams();
    if (filter?.name) params = params.set('name', filter.name);
    if (filter?.phone) params = params.set('phone', filter.phone);

    this._isLoading.set(true);
    return this.http.get<ClientResponse[]>(`${this.baseUrl}/clients`, { params }).pipe(
      tap((clients) => this._clients.set(clients)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getClientById(id: string): Observable<ClientResponse> {
    return this.http.get<ClientResponse>(`${this.baseUrl}/clients/${id}`);
  }

  createClient(client: ClientRequest): Observable<ClientResponse> {
    return this.http.post<ClientResponse>(`${this.baseUrl}/clients`, client).pipe(
      tap((newClient) => {
        this._clients.update((current) => [...current, newClient]);
      })
    );
  }

  updateClient(id: string, client: ClientRequest): Observable<ClientResponse> {
    return this.http.put<ClientResponse>(`${this.baseUrl}/clients/${id}`, client).pipe(
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
}
