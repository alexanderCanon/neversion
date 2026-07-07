import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservationsApiService, UploadReceiptRequest, ReservationResponse } from '@neversion/api-client';
import { SupabaseService } from '../../services/supabase.service';
import { ToastService } from '../../services/toast.service';

@Component({
  standalone: false,
  selector: 'app-payment-page',
  templateUrl: './payment-page.component.html',
  styleUrls: ['./payment-page.component.css']
})
export class PaymentPageComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reservationsApi = inject(ReservationsApiService);
  private readonly supabaseService = inject(SupabaseService);
  private readonly toastService = inject(ToastService);

  reservationId: string | null = null;
  flow: 'purchase' | 'renewal' = 'purchase';
  selectedFile: File | null = null;
  isUploading = false;

  reservation: ReservationResponse | null = null;
  isLoadingReservation = true;
  timeRemainingStr = '';
  isExpired = false;
  private timerId: any = null;

  ngOnInit(): void {
    this.reservationId = this.route.snapshot.queryParamMap.get('reservationId');
    this.flow = this.route.snapshot.queryParamMap.get('flow') === 'renewal'
      ? 'renewal'
      : 'purchase';
    if (!this.reservationId) {
      this.router.navigate(['/platforms']);
      return;
    }
    this.loadReservation();
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  loadReservation(): void {
    if (!this.reservationId) return;
    this.isLoadingReservation = true;
    this.reservationsApi.getReservationReservation(this.reservationId).subscribe({
      next: (res: any) => {
        this.reservation = res;
        this.isLoadingReservation = false;
        this.checkReservationStatus();
      },
      error: (err: any) => {
        console.error('Error loading reservation:', err);
        this.toastService.show('Error al cargar la información de la reserva.', 'danger');
        this.isLoadingReservation = false;
        this.router.navigate(['/platforms']);
      }
    });
  }

  checkReservationStatus(): void {
    if (!this.reservation) return;
    const status = this.reservation.status;
    
    if (status === 'EXPIRED' || status === 'CANCELLED') {
      this.isExpired = true;
      this.clearTimer();
      return;
    }

    if (status !== 'PENDING') {
      this.clearTimer();
      return;
    }

    this.startTimer();
  }

  startTimer(): void {
    this.clearTimer();
    if (!this.reservation || !this.reservation.expirationDate) return;

    // Convert ISO string to timestamp
    const expirationTime = new Date(this.reservation.expirationDate).getTime();

    const updateTimer = () => {
      const now = new Date().getTime();
      const distance = expirationTime - now;

      if (distance < 0) {
        this.isExpired = true;
        this.timeRemainingStr = '00:00';
        this.clearTimer();
        if (this.reservation) {
          this.reservation.status = 'EXPIRED';
        }
        this.toastService.show('La reservación ha expirado.', 'danger');
        return;
      }

      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);

      const minStr = minutes < 10 ? '0' + minutes : minutes;
      const secStr = seconds < 10 ? '0' + seconds : seconds;

      this.timeRemainingStr = `${minStr}:${secStr}`;
    };

    updateTimer();
    this.timerId = setInterval(updateTimer, 1000);
  }

  clearTimer(): void {
    if (this.timerId) {
       clearInterval(this.timerId);
       this.timerId = null;
    }
  }

  getShortId(id: string | null): string {
    if (!id) return '';
    return id.split('-')[0] || id.substring(0, 8);
  }

  copyReservationId(): void {
    if (!this.reservationId) return;
    navigator.clipboard.writeText(this.reservationId).then(() => {
      this.toastService.show('Código de reserva copiado al portapapeles', 'success');
    }).catch(err => {
      console.error('Could not copy text: ', err);
    });
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

      this.reservationsApi.uploadReceiptReservation(this.reservationId, request).subscribe({
        next: () => {
          alert('Comprobante subido exitosamente. Tu orden está siendo procesada.');
          this.router.navigate([this.flow === 'renewal' ? '/customer-panel' : '/platforms']);
        },
        error: (err: any) => {
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
