import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, finalize, map } from 'rxjs';
import { ProfileRequest as ApiProfileRequest, ProfileResponse as ApiProfileResponse } from '@neversion/api-client';
import { ProfileRequest, ProfileResponse } from '@neversion/models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  private readonly _profiles = signal<ProfileResponse[]>([]);
  readonly profiles = this._profiles.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getProfilesByAccount(accountId: string | number): Observable<ProfileResponse[]> {
    this._isLoading.set(true);
    return this.http.get<ApiProfileResponse[]>(`${this.baseUrl}/profiles`, { params: { accountId } }).pipe(
      map(apiProfiles => apiProfiles.map(api => this.mapToModel(api))),
      tap((profiles) => this._profiles.set(profiles)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getProfileById(id: string): Observable<ProfileResponse> {
    return this.http.get<ApiProfileResponse>(`${this.baseUrl}/profiles/${id}`).pipe(
      map(api => this.mapToModel(api))
    );
  }

  updateProfile(id: string, profile: Partial<ProfileRequest>): Observable<ProfileResponse> {
    const apiRequest: Partial<ApiProfileRequest> = {
      accountId: profile.accountId,
      name: profile.name,
      pin: profile.pin,
      isOwner: profile.isOwner
    };

    return this.http.put<ApiProfileResponse>(`${this.baseUrl}/profiles/${id}`, apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((updatedProfile) => {
          this._profiles.update(current => 
              current.map(p => p.id === id ? updatedProfile : p)
          );
      })
    );
  }

  private mapToModel(api: ApiProfileResponse): ProfileResponse {
    return {
      id: api.id || '',
      accountId: api.accountId || 0,
      name: api.name || '',
      pin: api.pin || '',
      isOwner: api.isOwner || false,
      createdAt: api.createdAt || ''
    };
  }
}
