import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservationsApiService, UploadReceiptRequest } from '@neversion/api-client';
import { SupabaseService } from '../../services/supabase.service';

@Component({
  selector: 'app-payment-page',
  templateUrl: './payment-page.component.html',
  styleUrls: ['./payment-page.component.css']
})
export class PaymentPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reservationsApi = inject(ReservationsApiService);
  private readonly supabaseService = inject(SupabaseService);

  reservationId: string | null = null;
  flow: 'purchase' | 'renewal' = 'purchase';
  selectedFile: File | null = null;
  isUploading = false;

  ngOnInit(): void {
    this.reservationId = this.route.snapshot.queryParamMap.get('reservationId');
    this.flow = this.route.snapshot.queryParamMap.get('flow') === 'renewal'
      ? 'renewal'
      : 'purchase';
    if (!this.reservationId) {
      this.router.navigate(['/platforms']);
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  async uploadReceipt(): Promise<void> {
    if (!this.selectedFile || !this.reservationId) return;

    this.isUploading = true;
    
    try {
      const supabase = this.supabaseService.client;
      const fileExt = this.selectedFile.name.split('.').pop();
      const fileName = `${this.reservationId}_${Date.now()}.${fileExt}`;
      
      const { data, error } = await supabase.storage
        .from('receipts')
        .upload(fileName, this.selectedFile, {
          cacheControl: '3600',
          upsert: true
        });

      if (error) {
        throw error;
      }

      const { data: publicUrlData } = supabase.storage
        .from('receipts')
        .getPublicUrl(fileName);

      if (!publicUrlData || !publicUrlData.publicUrl) {
        throw new Error('No se pudo obtener la URL pública del comprobante.');
      }

      const request: UploadReceiptRequest = {
        receiptUrl: publicUrlData.publicUrl
      };

      this.reservationsApi.uploadReceipt(this.reservationId, request).subscribe({
        next: () => {
          alert('Comprobante subido exitosamente. Tu orden está siendo procesada.');
          this.router.navigate([this.flow === 'renewal' ? '/customer-panel' : '/platforms']);
        },
        error: (err) => {
          console.error('Error uploading receipt url to backend:', err);
          alert('Error al registrar el comprobante en el servidor. Por favor intente de nuevo.');
          this.isUploading = false;
        }
      });
    } catch (err: any) {
      console.error('Error uploading receipt to Supabase:', err);
      alert('Error al subir el comprobante. Por favor intente de nuevo.');
      this.isUploading = false;
    }
  }
}
