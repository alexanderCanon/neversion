import { Injectable } from '@angular/core';
import {
  createClient,
  SupabaseClient,
} from '@supabase/supabase-js';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class SupabaseService {

  private supabaseClient: SupabaseClient;

  constructor() {
    this.supabaseClient = createClient(
      runtimeConfig.supabaseUrl,
      runtimeConfig.supabaseKey
    );
  }

  get client(): SupabaseClient {
    return this.supabaseClient;
  }
}
