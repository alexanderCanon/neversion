import { Injectable, inject } from '@angular/core';
import { runtimeConfig } from '../config/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private readonly STORAGE_BUCKET = 'services';
  private readonly PLACEHOLDER = '/assets/placeholder-service.svg';

  /**
   * Resolves a service image URL.
   * If the URL is empty, returns the default placeholder.
   * If the URL starts with 'http', returns it as is.
   * Otherwise, assumes it's a path within the Supabase 'services' bucket.
   */
  resolveServiceImageUrl(url?: string): string {
    if (!url || url.trim() === '') {
      return this.PLACEHOLDER;
    }

    if (url.startsWith('http')) {
      return url;
    }

    // Resolve as Supabase Storage Public URL
    // Format: https://[project-id].supabase.co/storage/v1/object/public/[bucket]/[path]
    return `${runtimeConfig.supabaseUrl}/storage/v1/object/public/${this.STORAGE_BUCKET}/${url}`;
  }
}
