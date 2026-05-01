import { Injectable } from '@angular/core';
import { createSupabaseClient } from '@neversion/utils';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class SupabaseService {
  private _supabaseClient: any;

  constructor() {
    this._supabaseClient = createSupabaseClient({
      url: runtimeConfig.supabaseUrl,
      key: runtimeConfig.supabaseKey
    });
  }

  get client(): any {
    return this._supabaseClient;
  }
}
