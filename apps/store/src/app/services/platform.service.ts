import { Injectable } from '@angular/core';
import { ConnectionService } from './connection.service';
import { Platforms } from '../model/platforms.model';
import { Observable, from, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PlatformService {

  constructor(private _supabaseService: ConnectionService) { }

  //   async getPlatforms(): Promise<Platforms[]> {
  //   const { data, error } = await this._supabaseService.client
  //     .from('platforms')
  //     .select('*');

  //   if (error) {
  //     throw error;
  //   }

  //   return data;
  // }

  public getPlatforms(): Observable<Platforms[]> {
    return from(
      this._supabaseService.client
        .from("services")
        .select("*")
    ).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        return data as Platforms[];
      }),
      catchError(err => {
        console.error('Error fetching platforms:', err);
        return throwError(() => err);
      })
    );
  }

  public getPlatformById(id: number): Observable<Platforms[]> {
    return from(
      this._supabaseService.client
        .from("services")
        .select("*")
        .eq("id", id)
    ).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        return data as Platforms[];
      }),
      catchError(err => {
        console.error('Error fetching platform:', err);
        return throwError(() => err);
      })
    );
  }

} //end class PlatformService
