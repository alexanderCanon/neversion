import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  GamesApiService,
  GameSKUsApiService,
  GameResponse,
  GameSkuResponse
} from '@neversion/api-client';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly gamesApi = inject(GamesApiService);
  private readonly gameSkusApi = inject(GameSKUsApiService);

  /**
   * Fetch active parent games for the storefront
   */
  public getGames(): Observable<GameResponse[]> {
    return this.gamesApi.listActiveGame(runtimeConfig.storeVendorUuid).pipe(
      catchError(err => {
        console.error('Error fetching storefront games:', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Fetch a single active parent game by slug (store view)
   */
  public getGameBySlug(slug: string): Observable<GameResponse> {
    return this.gamesApi.getActiveBySlugGame(runtimeConfig.storeVendorUuid, slug).pipe(
      catchError(err => {
        console.error('Error fetching game by slug:', err);
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

  /**
   * Fetch active SKUs for a game identified by slug (store view)
   */
  public getGameSkusBySlug(gameSlug: string): Observable<GameSkuResponse[]> {
    return this.gameSkusApi.listActiveByGameSlugGameSku(runtimeConfig.storeVendorUuid, gameSlug).pipe(
      catchError(err => {
        console.error('Error fetching game SKUs by slug:', err);
        return throwError(() => err);
      })
    );
  }
}
