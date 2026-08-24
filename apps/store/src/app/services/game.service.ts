import { Injectable, inject } from '@angular/core';
import { Observable, from, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { GameResponse, GameSkuResponse } from '@alexandercanon/api-client-angular';
import { SupabaseService } from './supabase.service';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly supabase = inject(SupabaseService);

  /**
   * Fetch active parent games for the storefront via Supabase PostgREST view
   */
  public getGames(): Observable<GameResponse[]> {
    const promise = this.supabase.client
      .from('v_store_games')
      .select('*')
      .eq('vendor_uuid', runtimeConfig.storeVendorUuid);

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        return (data || []).map(item => ({
          id: item.game_uuid,
          uuid: item.game_uuid,
          name: item.game_name,
          slug: item.game_slug,
          imageUrl: item.image_url
        })) as GameResponse[];
      }),
      catchError(err => {
        console.error('Error fetching storefront games from Supabase view:', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Fetch a single active parent game by slug (store view)
   */
  public getGameBySlug(slug: string): Observable<GameResponse> {
    const promise = this.supabase.client
      .from('v_store_games')
      .select('*')
      .eq('vendor_uuid', runtimeConfig.storeVendorUuid)
      .eq('game_slug', slug)
      .maybeSingle();

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        if (!data) throw new Error(`Game not found for slug: ${slug}`);
        return {
          id: data.game_uuid,
          uuid: data.game_uuid,
          name: data.game_name,
          slug: data.game_slug,
          imageUrl: data.image_url
        } as GameResponse;
      }),
      catchError(err => {
        console.error('Error fetching game by slug from Supabase view:', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Fetch a single active parent game by ID / UUID
   */
  public getGameById(id: string): Observable<GameResponse> {
    const promise = this.supabase.client
      .from('v_store_games')
      .select('*')
      .eq('game_uuid', id)
      .maybeSingle();

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        if (!data) throw new Error(`Game not found for id: ${id}`);
        return {
          id: data.game_uuid,
          uuid: data.game_uuid,
          name: data.game_name,
          slug: data.game_slug,
          imageUrl: data.image_url
        } as GameResponse;
      }),
      catchError(err => {
        console.error('Error fetching game details from Supabase view:', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * Fetch active SKUs for a game identified by slug (store view)
   */
  public getGameSkusBySlug(gameSlug: string): Observable<GameSkuResponse[]> {
    const promise = this.supabase.client
      .from('v_store_game_skus')
      .select('*')
      .eq('vendor_uuid', runtimeConfig.storeVendorUuid)
      .eq('game_slug', gameSlug);

    return from(promise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        return (data || []).map(item => ({
          id: item.sku_uuid,
          uuid: item.sku_uuid,
          code: item.sku_code,
          name: item.sku_name,
          price: Number(item.sku_price),
          imageUrl: item.sku_image_url
        })) as GameSkuResponse[];
      }),
      catchError(err => {
        console.error('Error fetching game SKUs by slug from Supabase view:', err);
        return throwError(() => err);
      })
    );
  }
}
