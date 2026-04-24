import { Injectable } from '@angular/core';
import { createSupabaseClient, SupabaseClient } from '@neversion/utils';

@Injectable({
  providedIn: 'root'
})
export class SupabaseService {
  private _supabaseClient: SupabaseClient;

  constructor() {
    // These should ideally come from environment files
    const supabaseUrl = 'https://your-project.supabase.co';
    const supabaseKey = 'your-anon-key';

    this._supabaseClient = createSupabaseClient({
      url: supabaseUrl,
      key: supabaseKey
    });
  }

  get client(): SupabaseClient {
    return this._supabaseClient;
  }
}
