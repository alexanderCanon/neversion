import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map } from 'rxjs';
import {
    GamesApiService,
    GameRequest as ApiGameRequest,
    GameResponse as ApiGameResponse
} from '@alexandercanon/api-client-angular';
import { GameRequest, GameResponse } from '@neversion/models';

interface ApiGamesPageResponse {
  content?: ApiGameResponse[];
}

@Injectable({ providedIn: 'root' })
export class GamesDataService {
  private readonly gamesApi = inject(GamesApiService);

  private readonly _games = signal<GameResponse[]>([]);
  readonly games = this._games.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  /**
   * List games (parents) for the current authenticated vendor
   */
  getGames(isActive?: boolean): Observable<GameResponse[]> {
    this._isLoading.set(true);
    return this.gamesApi.listVendorGamesGame(
      isActive,
      'body',
      false
    ).pipe(
      map((apiGames) => this.normalizeGamesResponse(apiGames).map(api => this.mapToModel(api))),
      tap((games: GameResponse[]) => this._games.set(games)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getGameById(id: string): Observable<GameResponse> {
    return this.gamesApi.getByIdGame(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  createGame(game: GameRequest): Observable<GameResponse> {
    const apiRequest: ApiGameRequest = {
      name: game.name,
      slug: game.slug,
      imageUrl: game.imageUrl
    };

    return this.gamesApi.createGame(apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newGame) => {
        this._games.update((current) => [...current, newGame]);
      })
    );
  }

  updateGame(id: string, game: GameRequest): Observable<GameResponse> {
    const apiRequest: ApiGameRequest = {
      name: game.name,
      slug: game.slug,
      imageUrl: game.imageUrl
    };

    return this.gamesApi.updateGame(id, apiRequest).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedGame) => {
            this._games.update(current =>
                current.map(g => g.id === id ? updatedGame : g)
            );
        })
    );
  }

  /**
   * Toggles the active status of a game
   */
  toggleGameStatus(id: string): Observable<GameResponse> {
    return this.gamesApi.toggleStatusGame(id).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedGame) => {
            this._games.update(current =>
                current.map(g => g.id === id ? updatedGame : g)
            );
        })
    );
  }

  deleteGame(id: string): Observable<void> {
    return this.gamesApi.deleteGame(id).pipe(
      tap(() => {
        this._games.update((current) => current.filter((g) => g.id !== id));
      })
    );
  }

  refreshGames(): Observable<GameResponse[]> {
    return this.getGames();
  }

  private normalizeGamesResponse(response: ApiGameResponse[] | ApiGamesPageResponse): ApiGameResponse[] {
    if (Array.isArray(response)) {
      return response;
    }
    return response.content ?? [];
  }

  private mapToModel(api: ApiGameResponse): GameResponse {
    return {
      id: api.id || '',
      name: api.name || '',
      slug: api.slug || '',
      imageUrl: api.imageUrl || '',
      isActive: api.isActive ?? true,
      createdAt: api.createdAt || ''
    };
  }
}
