import { Injectable, inject } from '@angular/core';
import { SupabaseService } from './supabase.service';
import { from, Observable, map } from 'rxjs';

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

  /**
   * Deletes a file from Supabase Storage.
   * @param path The path/filename in the bucket (e.g. "1717283921_logo.png")
   * @returns Observable that completes when the deletion is finished
   */
  deleteServiceImage(path: string): Observable<void> {
    const deletePromise = this.supabase.storage
      .from(this.BUCKET_SERVICES)
      .remove([path]);

    return from(deletePromise).pipe(
      map(({ error }) => {
        if (error) throw error;
        return;
      })
    );
  }

  /**
   * Uploads a game image file to Supabase Storage and returns the public URL.
   * Saved under the 'games/' subfolder.
   */
  uploadGameImage(file: File, path: string): Observable<string> {
    const uploadPromise = this.supabase.storage
      .from(this.BUCKET_SERVICES)
      .upload(`games/${path}`, file, {
        upsert: true,
        cacheControl: '3600'
      });

    return from(uploadPromise).pipe(
      map(({ data, error }) => {
        if (error) throw error;
        
        const { data: { publicUrl } } = this.supabase.storage
          .from(this.BUCKET_SERVICES)
          .getPublicUrl(data.path);
          
        return publicUrl;
      })
    );
  }

  /**
   * Deletes a game file from Supabase Storage games subfolder.
   */
  deleteGameImage(path: string): Observable<void> {
    const deletePromise = this.supabase.storage
      .from(this.BUCKET_SERVICES)
      .remove([`games/${path}`]);

    return from(deletePromise).pipe(
      map(({ error }) => {
        if (error) throw error;
        return;
      })
    );
  }
}
