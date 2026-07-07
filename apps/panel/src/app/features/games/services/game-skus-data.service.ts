import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap, finalize, map, of } from 'rxjs';
import {
    GameSKUsApiService,
    GameSkuRequest as ApiGameSkuRequest,
    GameSkuResponse as ApiGameSkuResponse
} from '@neversion/api-client';
import { GameSkuRequest, GameSkuResponse } from '@neversion/models';
import { AuthService } from '../../../core/services/auth.service';

export interface GameSkusFilter {
  gameUuid?: string;
  isActive?: boolean;
}

@Injectable({ providedIn: 'root' })
export class GameSkusDataService {
  private readonly gameSkusApi = inject(GameSKUsApiService);
  private readonly authService = inject(AuthService);

  private readonly _gameSkus = signal<GameSkuResponse[]>([]);
  readonly gameSkus = this._gameSkus.asReadonly();

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = this._isLoading.asReadonly();

  /**
   * List game SKUs for the current authenticated vendor, optionally filtered by game
   */
  getGameSkus(filter?: GameSkusFilter): Observable<GameSkuResponse[]> {
    const vendorUuid = this.authService.currentVendorUuid();
    if (!vendorUuid) return of([]);

    this._isLoading.set(true);
    return this.gameSkusApi.listByVendorGameSku(
      vendorUuid,
      filter?.gameUuid,
      filter?.isActive,
      'body',
      false
    ).pipe(
      map((apiSkus) => this.normalizeResponse(apiSkus).map(api => this.mapToModel(api))),
      tap((skus: GameSkuResponse[]) => this._gameSkus.set(skus)),
      finalize(() => this._isLoading.set(false))
    );
  }

  getGameSkuById(id: string): Observable<GameSkuResponse> {
    return this.gameSkusApi.getByIdGameSku(id).pipe(
      map(api => this.mapToModel(api))
    );
  }

  createGameSku(sku: GameSkuRequest): Observable<GameSkuResponse> {
    const apiRequest: ApiGameSkuRequest = {
      code: sku.code,
      name: sku.name,
      price: sku.price,
      imageUrl: sku.imageUrl,
      gameUuid: sku.gameUuid
    };

    return this.gameSkusApi.createGameSku(apiRequest).pipe(
      map(api => this.mapToModel(api)),
      tap((newSku) => {
        this._gameSkus.update((current) => [...current, newSku]);
      })
    );
  }

  updateGameSku(id: string, sku: GameSkuRequest): Observable<GameSkuResponse> {
    const apiRequest: ApiGameSkuRequest = {
      code: sku.code,
      name: sku.name,
      price: sku.price,
      imageUrl: sku.imageUrl,
      gameUuid: sku.gameUuid
    };

    return this.gameSkusApi.updateGameSku(id, apiRequest).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedSku) => {
            this._gameSkus.update(current =>
                current.map(s => s.id === id ? updatedSku : s)
            );
        })
    );
  }

  /**
   * Toggles the active status of a game SKU
   */
  toggleGameSkuStatus(id: string): Observable<GameSkuResponse> {
    return this.gameSkusApi.toggleStatusGameSku(id).pipe(
        map(api => this.mapToModel(api)),
        tap((updatedSku) => {
            this._gameSkus.update(current =>
                current.map(s => s.id === id ? updatedSku : s)
            );
        })
    );
  }

  deleteGameSku(id: string): Observable<void> {
    return this.gameSkusApi.deleteGameSku(id).pipe(
      tap(() => {
        this._gameSkus.update((current) => current.filter((s) => s.id !== id));
      })
    );
  }

  refreshGameSkus(): Observable<GameSkuResponse[]> {
    return this.getGameSkus();
  }

  private normalizeResponse(response: ApiGameSkuResponse[]): ApiGameSkuResponse[] {
    if (Array.isArray(response)) {
      return response;
    }
    return [];
  }

  private mapToModel(api: ApiGameSkuResponse): GameSkuResponse {
    return {
      id: api.id || '',
      code: api.code || '',
      name: api.name || '',
      price: api.price || 0,
      imageUrl: api.imageUrl || '',
      isActive: api.isActive ?? true,
      gameUuid: api.gameUuid || '',
      gameSlug: api.gameSlug || '',
      gameName: api.gameName || '',
      createdAt: api.createdAt || ''
    };
  }
}
