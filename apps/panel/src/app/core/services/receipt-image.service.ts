import { Injectable } from '@angular/core';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class ReceiptImageService {

  /**
   * Resolves a receipt image URL.
   * Stored URLs may contain the wrong Supabase domain (`supabase.com`)
   * instead of the project-specific domain (`[project].supabase.co`).
   * This method detects that pattern and rebuilds the URL using the
   * configured supabaseUrl.
   */
  resolveReceiptUrl(url?: string | null): string {
    const value = url?.trim();

    if (!value) {
      return '';
    }

    // Detect malformed domain: bare https://supabase.co instead of https://[project].supabase.co
    const malformedPrefix = 'https://supabase.co';
    if (value.startsWith(malformedPrefix)) {
      const pathAfterDomain = value.slice(malformedPrefix.length);
      return `${runtimeConfig.supabaseUrl}${pathAfterDomain}`;
    }

    return value;
  }
}
