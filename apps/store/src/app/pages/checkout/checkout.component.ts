import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CartService, CartItem } from '../../services/cart.service';
import { LoyaltyPointsApiService } from '@neversion/api-client';
import { ReservationsApiService, ReservationRequest, ReservationItemRequest } from '@neversion/api-client';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { map, take } from 'rxjs/operators';

@Component({
  standalone: false,
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  private readonly cartService = inject(CartService);
  private readonly reservationsApi = inject(ReservationsApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  cartItems$: Observable<CartItem[]> = this.cartService.items$;
  total$: Observable<number> = this.cartItems$.pipe(
    map(() => this.cartService.getTotal())
  );
  discountPercent$: Observable<number> = this.cartItems$.pipe(
    map(() => this.cartService.getComboDiscountPercent())
  );
  discountAmount$: Observable<number> = this.cartItems$.pipe(
    map(() => this.cartService.getComboDiscountAmount())
  );
  discountedTotal$: Observable<number> = this.cartItems$.pipe(
    map(() => this.cartService.getDiscountedTotal())
  );

  private readonly loyaltyApi = inject(LoyaltyPointsApiService);
  availablePoints = 0;
  readonly pointsToRedeemSubject = new BehaviorSubject<number>(0);
  pointsToRedeem$ = this.pointsToRedeemSubject.asObservable();
  finalTotalAfterPoints$: Observable<number> = combineLatest([
    this.discountedTotal$,
    this.pointsToRedeem$
  ]).pipe(
    map(([total, points]) => Math.max(0, total - points))
  );
  currentDiscountedTotal = 0;

  paymentMethods = [
    { id: 'TRANSFERENCIA', label: 'Transferencia Bancaria' },
    { id: 'DEPOSITO', label: 'Depósito Bancario' }
  ];
  selectedPaymentMethod = 'TRANSFERENCIA';
  isSubmitting = false;

  /** Returns true when a cart item is a Spotify Family (BY_PROFILE) slot. */
  isSpotifyProfile(item: CartItem): boolean {
    return item.type === 'PROFILE' && item.service.name?.toLowerCase() === 'spotify';
  }

  setSpotifyPreference(item: CartItem, preference: 'CUENTA_NUEVA' | 'CUENTA_PROPIA'): void {
    this.cartService.setSpotifyPreference(item.service.id!, preference);
  }

  ngOnInit(): void {
    this.discountedTotal$.subscribe(t => (this.currentDiscountedTotal = t));
    this.authService.currentUser$.pipe(take(1)).subscribe(user => {
      if (user) {
        this.loyaltyApi.getMySummaryClientPoints().subscribe({
          next: (s: any) => (this.availablePoints = s.available ?? 0),
          error: (err: any) => console.error('Error loading points balance', err)
        });
      }
    });
  }

  updateQuantity(item: CartItem, delta: number): void {
    this.cartService.updateQuantity(item.service.id!, item.type, item.quantity + delta);
  }

  removeItem(item: CartItem): void {
    this.cartService.removeFromCart(item.service.id!, item.type);
  }

  setPointsToRedeem(value: number, maxTotal: number): void {
    const clamped = Math.max(0, Math.min(value, this.availablePoints, Math.floor(maxTotal)));
    this.pointsToRedeemSubject.next(clamped);
  }

  useMaxPoints(maxTotal: number): void {
    this.setPointsToRedeem(this.availablePoints, maxTotal);
  }

  async placeOrder(): Promise<void> {
    const user = await this.authService.currentUser$.pipe(take(1)).toPromise();

    if (!user) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }

    const items = this.cartService.getItems();
    if (items.length === 0) return;

    this.isSubmitting = true;

    const pointsToRedeem = this.pointsToRedeemSubject.value;

    // Build optional order notes from Spotify preferences.
    const spotifyNotes = items
      .filter(i => this.isSpotifyProfile(i) && i.spotifyAccountPreference)
      .map(i => `${i.service.name}: ${i.spotifyAccountPreference === 'CUENTA_NUEVA' ? 'Cuenta nueva' : 'Cuenta propia'}`)
      .join('; ');

    const reservationRequest = {
      clientId: user.id,
      items: items.map(item => ({
        serviceUuid: item.service.id!,
        qty: item.quantity,
        saleMode: item.type === 'COMPLETE' ? 'FULL_ACCOUNT' : 'BY_PROFILE'
      } as ReservationItemRequest)),
      paymentMethod: this.selectedPaymentMethod,
      ...(spotifyNotes ? { notes: spotifyNotes } : {}),
      ...(pointsToRedeem > 0 ? { pointsToRedeem } : {})
    } as ReservationRequest;

    this.reservationsApi.createReservationReservation(reservationRequest).subscribe({
      next: (response: any) => {
        this.cartService.clearCart();
        this.router.navigate(['/payment-page'], { queryParams: { reservationId: response.id } });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error creating reservation:', err);
        
        let title = 'Error';
        let friendlyMessage = 'Error al crear la reservación. Por favor intente de nuevo.';
        
        if (err.status === 409) {
          const message = err.error?.message || '';
          if (message.includes('Not enough available profiles for service')) {
            const match = /Not enough available profiles for service '([^']+)'\. Available: (\d+), requested: (\d+)/.exec(message);
            if (match) {
              const serviceName = match[1];
              const available = parseInt(match[2], 10);
              const requested = parseInt(match[3], 10);
              title = 'Sin Disponibilidad';
              if (available === 0) {
                friendlyMessage = `El servicio '${serviceName}' no tiene disponibilidad en este momento. Por favor remuévalo del carrito.`;
              } else {
                friendlyMessage = `Solo quedan ${available} perfiles disponibles para '${serviceName}' (solicitó ${requested}). Por favor reduzca la cantidad.`;
              }
            } else {
              title = 'Sin Disponibilidad';
              friendlyMessage = 'No hay suficientes perfiles disponibles para uno de los servicios solicitados.';
            }
          } else if (message.includes('Client is not linked to any vendor')) {
            title = 'Error de Cuenta';
            friendlyMessage = 'Su cuenta de cliente no está vinculada a ningún vendedor. Por favor contáctenos.';
          } else if (message.includes('Points redeemed cannot exceed')) {
            title = 'Puntos Inválidos';
            friendlyMessage = 'La cantidad de puntos a redimir excede el total de tu compra.';
          } else if (message.includes('Insufficient points balance')) {
            title = 'Saldo Insuficiente';
            friendlyMessage = 'No cuentas con suficientes puntos disponibles.';
          }
        }
        
        this.toastService.show(friendlyMessage, 'danger', title);
        this.isSubmitting = false;
      }
    });
  }
}
