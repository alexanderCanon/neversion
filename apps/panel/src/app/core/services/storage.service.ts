import { Injectable, inject } from '@angular/core';
import { SupabaseService } from './supabase.service';
import { from, Observable, map, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly supabase = inject(SupabaseService).client;
  private readonly BUCKET_SERVICES = 'services';

  /**
   * Uploads a file to Supabase Storage and returns the public URL.
   * @param file The file to upload
   * @param path The path/filename in the bucket
   * @returns Observable with the public URL
   */
  uploadServiceImage(file: File, path: string): Observable<string> {
    // 1. Upload the file
    const uploadPromise = this.supabase.storage
      .from(this.BUCKET_SERVICES)
      .upload(path, file, {
        upsert: true,
        cacheControl: '3600'
      });

    return from(uploadPromise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        
        // 2. Get the public URL
        const { data: { publicUrl } } = this.supabase.storage
          .from(this.BUCKET_SERVICES)
          .getPublicUrl(data.path);
          
        return publicUrl;
      })
    );
  }
}
