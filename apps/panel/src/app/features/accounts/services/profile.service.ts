import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, finalize } from 'rxjs';
import { ProfileRequest, ProfileResponse } from '../models/profile.model';
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
    return this.http.get<ProfileResponse[]>(`${this.baseUrl}/profiles/account/${accountId}`).pipe(
      tap((profiles) => this._profiles.set(profiles)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getProfileById(id: string): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.baseUrl}/profiles/${id}`);
  }

  updateProfile(id: string, profile: Partial<ProfileRequest>): Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(`${this.baseUrl}/profiles/${id}`, profile).pipe(
        tap((updatedProfile) => {
            this._profiles.update(current => 
                current.map(p => p.id === id ? updatedProfile : p)
            );
        })
    );
  }
}
