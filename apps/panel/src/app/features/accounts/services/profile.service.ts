import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map } from 'rxjs';
import { 
    ProfilesApiService, 
    AccountsApiService,
    ProfileRequest as ApiProfileRequest, 
    ProfileResponse as ApiProfileResponse,
    ChangeProfileStatusRequest as ApiStatusRequest
} from '@neversion/api-client';
import { ProfileRequest, ProfileResponse, ProfileStatus, ChangeProfileStatusRequest } from '@neversion/models';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly profilesApi = inject(ProfilesApiService);
  private readonly accountsApi = inject(AccountsApiService);

  private readonly _profiles = signal<ProfileResponse[]>([]);
  readonly profiles = this._profiles.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  getProfilesByAccount(accountId: string, available?: boolean): Observable<ProfileResponse[]> {
    this._isLoading.set(true);
    // Note: API client expects 'number' for accountId in listByAccount. 
    // We cast to any for now to allow UUID strings if the backend supports them but types aren't updated.
    return this.profilesApi.listByAccount(accountId as any, available).pipe(
      map(apiProfiles => apiProfiles.map(api => this.mapToModel(api))),
      tap((profiles) => this._profiles.set(profiles)),
      finalize(() => this._isLoading.set(false))
    );
  }

  /**
   * Bulk generate profiles (US-025)
   */
  generateProfiles(accountId: string, count: number): Observable<void> {
      this._isLoading.set(true);
      return this.accountsApi.generateProfiles(accountId, count).pipe(
          finalize(() => this._isLoading.set(false))
      );
  }

  getProfileById(id: string): Observable<ProfileResponse> {
    return this.profilesApi.getById1(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  updateProfile(id: string, profile: ProfileRequest): Observable<ProfileResponse> {
    const apiRequest: ApiProfileRequest = {
      accountId: profile.accountId,
      name: profile.name,
      pin: profile.pin,
      isOwner: profile.isOwner
    };

    return this.profilesApi.update1(id, apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((updatedProfile) => {
          this._profiles.update(current => 
              current.map(p => p.id === id ? updatedProfile : p)
          );
      })
    );
  }

  /**
   * Manually change status (US-027)
   */
  changeStatus(id: string, request: ChangeProfileStatusRequest): Observable<ProfileResponse> {
      const apiRequest: ApiStatusRequest = {
          status: request.status as any
      };
      return this.profilesApi.changeStatus(id, apiRequest).pipe(
          map(api => this.mapToModel(api)),
          tap((updated) => {
              this._profiles.update(current => 
                current.map(p => p.id === id ? updated : p)
              );
          })
      );
  }

  private mapToModel(api: ApiProfileResponse): ProfileResponse {
    return {
      id: api.id || '',
      accountId: String(api.accountId || ''),
      name: api.name || '',
      pin: api.pin || '',
      isOwner: api.isOwner || false,
      status: api.status as unknown as ProfileStatus,
      createdAt: api.createdAt || ''
    };
  }
}
