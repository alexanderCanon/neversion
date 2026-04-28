import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CartService, CartItem } from '../../services/cart.service';
import { ReservationsApiService, ReservationRequest, ReservationItemRequest } from '@neversion/api-client';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  private readonly cartService = inject(CartService);
  private readonly reservationsApi = inject(ReservationsApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  cartItems$: Observable<CartItem[]> = this.cartService.items$;
  total$: Observable<number> = this.cartItems$.pipe(
    map(() => this.cartService.getTotal())
  );

  paymentMethods = [
    { id: 'TRANSFERENCIA', label: 'Transferencia Bancaria' },
    { id: 'DEPOSITO', label: 'Depósito Bancario' }
  ];
  selectedPaymentMethod = 'TRANSFERENCIA';
  isSubmitting = false;

  ngOnInit(): void {
    // If cart is empty, redirect to platforms after a short delay or just show empty state
  }

  updateQuantity(item: CartItem, delta: number): void {
    this.cartService.updateQuantity(item.service.id!, item.type, item.quantity + delta);
  }

  removeItem(item: CartItem): void {
    this.cartService.removeFromCart(item.service.id!, item.type);
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

    const reservationRequest: ReservationRequest = {
      clientId: user.id,
      items: items.map(item => ({
        serviceUuid: item.service.id!,
        qty: item.quantity
      } as ReservationItemRequest)),
      paymentMethod: this.selectedPaymentMethod
    };

    // The backend uses VENDOR_UUID = '00000000-0000-0000-0000-000000000000' for the main store
    // This might need adjustment if multi-vendor store is implemented, 
    // but US-033 says it links to client_id and vendor_id.
    // The ReservationRequest in model doesn't have vendorId, usually it's inferred from the services or a header.
    
    this.reservationsApi.createReservation(reservationRequest).subscribe({
      next: (response) => {
        this.cartService.clearCart();
        this.router.navigate(['/payment-page'], { queryParams: { reservationId: response.id } });
      },
      error: (err) => {
        console.error('Error creating reservation:', err);
        alert('Error al crear la reservación. Por favor intente de nuevo.');
        this.isSubmitting = false;
      }
    });
  }
}
