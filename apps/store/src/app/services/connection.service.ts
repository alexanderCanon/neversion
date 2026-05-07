import { Injectable } from '@angular/core';
import { SupabaseClient, createClient } from '@supabase/supabase-js';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class ConnectionService {

  private static _supabaseClient: SupabaseClient;

  constructor() {
    if (!ConnectionService._supabaseClient) {
      ConnectionService._supabaseClient = createClient(
        runtimeConfig.supabaseUrl,
        runtimeConfig.supabaseKey,
        { auth: { persistSession: false, autoRefreshToken: false } }
      );
    }
  }

  get client(): SupabaseClient { return ConnectionService._supabaseClient; }
}
