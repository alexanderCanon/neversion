import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservationsApiService, UploadReceiptRequest } from '@neversion/api-client';

@Component({
  selector: 'app-payment-page',
  templateUrl: './payment-page.component.html',
  styleUrls: ['./payment-page.component.css']
})
export class PaymentPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reservationsApi = inject(ReservationsApiService);

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

  uploadReceipt(): void {
    if (!this.selectedFile || !this.reservationId) return;

    this.isUploading = true;
    
    // Simulate file upload to get a URL
    // In a real scenario, we would use Supabase Storage or similar
    // For now, we'll use a mock URL that looks like a Supabase storage URL
    const mockUrl = `https://supabase.co/storage/v1/object/public/receipts/${this.reservationId}_${this.selectedFile.name}`;
    
    const request: UploadReceiptRequest = {
      receiptUrl: mockUrl
    };

    this.reservationsApi.uploadReceipt(this.reservationId, request).subscribe({
      next: () => {
        alert('Comprobante subido exitosamente. Tu orden está siendo procesada.');
        this.router.navigate([this.flow === 'renewal' ? '/customer-panel' : '/platforms']);
      },
      error: (err) => {
        console.error('Error uploading receipt:', err);
        alert('Error al subir el comprobante. Por favor intente de nuevo.');
        this.isUploading = false;
      }
    });
  }
}
