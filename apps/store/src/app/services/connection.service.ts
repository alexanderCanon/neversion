import { Injectable } from '@angular/core';
import { SupabaseClient, createClient } from '@supabase/supabase-js';

@Injectable({
  providedIn: 'root'
})
export class ConnectionService {

  private static _supabaseClient: SupabaseClient;

  constructor() {
    if (!ConnectionService._supabaseClient) {
      ConnectionService._supabaseClient = createClient(
        "https://ujadaoctohwplnhiwgrm.supabase.co",
        "sb_publishable_L7CsO8W5dHpkmy0RzeEOyA_8LK4rZ2N",
        { auth: { persistSession: false, autoRefreshToken: false } }
      );
    }
  }

  get client(): SupabaseClient { return ConnectionService._supabaseClient; }
}
