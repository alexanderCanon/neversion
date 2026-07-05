import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { GamesApiService, GameResponse } from '@neversion/api-client';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly gamesApi = inject(GamesApiService);

  /**
   * Fetch active games for the storefront
   */
  public getGames(): Observable<GameResponse[]> {
    return this.gamesApi.listActiveGame(runtimeConfig.storeVendorUuid).pipe(
      catchError(err => {
        console.error('Error fetching storefront games:', err);
        return throwError(() => err);
      })
    );
  }

  public getGameById(id: string): Observable<GameResponse> {
    return this.gamesApi.getByIdGame(id).pipe(
      catchError(err => {
        console.error('Error fetching game details:', err);
        return throwError(() => err);
      })
    );
  }
}
