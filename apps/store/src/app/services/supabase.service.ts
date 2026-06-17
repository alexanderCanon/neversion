import { Injectable } from '@angular/core';
import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class SupabaseService {
  private supabaseClient?: SupabaseClient;

  get client(): SupabaseClient {
    return this.supabaseClient ??= createClient(
      runtimeConfig.supabaseUrl,
      runtimeConfig.supabaseKey
    );
  }
}
