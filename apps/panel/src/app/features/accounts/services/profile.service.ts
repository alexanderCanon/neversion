import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map } from 'rxjs';
import { 
    ProfilesApiService, 
    AccountsApiService,
    ProfileRequest as ApiProfileRequest, 
    ProfileResponse as ApiProfileResponse,
    ChangeProfileStatusRequest as ApiStatusRequest
} from '@alexandercanon/api-client-angular';
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
    // Note: API client expects 'string' for accountId in listByAccountProfile.
    return this.profilesApi.listByAccountProfile(accountId, available).pipe(
      map((apiProfiles: ApiProfileResponse[]) => apiProfiles.map((api: ApiProfileResponse) => this.mapToModel(api))),
      tap((profiles: ProfileResponse[]) => this._profiles.set(profiles)),
      finalize(() => this._isLoading.set(false))
    );
  }

  /**
   * Bulk generate profiles (US-025)
   */
  generateProfiles(accountId: string, count: number): Observable<void> {
      this._isLoading.set(true);
      return this.accountsApi.generateProfilesAccount(accountId, count).pipe(
          finalize(() => this._isLoading.set(false))
      );
  }

  getProfileById(id: string): Observable<ProfileResponse> {
    return this.profilesApi.getByIdProfile(id).pipe(
      map((api: ApiProfileResponse) => this.mapToModel(api))
    );
  }

  updateProfile(id: string, profile: ProfileRequest): Observable<ProfileResponse> {
    const apiRequest: ApiProfileRequest = {
      accountId: profile.accountId,
      name: profile.name,
      pin: profile.pin,
      notes: profile.notes,
      isOwner: profile.isOwner
    };

    return this.profilesApi.updateProfile(id, apiRequest).pipe(
      map((api: ApiProfileResponse) => this.mapToModel(api)),
      tap((updatedProfile: ProfileResponse) => {
          this._profiles.update((current: ProfileResponse[]) => 
              current.map((p: ProfileResponse) => p.id === id ? updatedProfile : p)
          );
      })
    );
  }

  /**
   * Manually change status (US-027)
   */
  changeStatus(id: string, request: ChangeProfileStatusRequest): Observable<ProfileResponse> {
      const apiRequest: ApiStatusRequest = {
          status: request.status as unknown as ApiStatusRequest['status']
      };
      return this.profilesApi.changeStatusProfile(id, apiRequest).pipe(
          map((api: ApiProfileResponse) => this.mapToModel(api)),
          tap((updated: ProfileResponse) => {
              this._profiles.update((current: ProfileResponse[]) => 
                current.map((p: ProfileResponse) => p.id === id ? updated : p)
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
      notes: api.notes,
      isOwner: api.isOwner || false,
      status: api.status as unknown as ProfileStatus,
      createdAt: api.createdAt || ''
    };
  }
}
