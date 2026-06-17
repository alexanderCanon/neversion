import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CartService, CartItem } from '../../services/cart.service';
import { User } from '@neversion/models';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.css']
})
export class ToolbarComponent {
  private readonly authService = inject(AuthService);
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);

  currentUser$: Observable<User | null> = this.authService.currentUser$;
  cartItems$: Observable<CartItem[]> = this.cartService.items$;
  cartItemCount$: Observable<number> = this.cartService.items$.pipe(
    map(items => items.reduce((acc, item) => acc + item.quantity, 0))
  );

  searchQuery = '';

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }

  getTotal(): number {
    return this.cartService.getTotal();
  }

  removeItem(item: CartItem): void {
    this.cartService.removeFromCart(item.service.id!, item.type);
  }

  onSearch(): void {
    const query = this.searchQuery.trim();
    if (query) {
      this.router.navigate(['/platforms'], { queryParams: { q: query } });
    }
  }
}
