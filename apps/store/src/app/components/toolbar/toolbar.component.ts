import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CartService, CartItem } from '../../services/cart.service';
import { User } from '@neversion/models';
import { Observable, Subscription } from 'rxjs';
import { map, filter } from 'rxjs/operators';
import { Router, NavigationEnd } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.css']
})
export class ToolbarComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);

  currentUser$: Observable<User | null> = this.authService.currentUser$;
  cartItems$: Observable<CartItem[]> = this.cartService.items$;
  cartItemCount$: Observable<number> = this.cartService.items$.pipe(
    map(items => items.reduce((acc, item) => acc + item.quantity, 0))
  );

  cartBadgeAnimate = false;
  searchQuery = '';
  private lastCount = 0;
  private routerSubscription?: Subscription;
  private cartSubscription?: Subscription;

  ngOnInit(): void {
    // Auto-close mobile navbar collapse menu on navigation end
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.closeNavbarCollapse();
    });

    this.cartSubscription = this.cartItemCount$.subscribe(count => {
      if (count > this.lastCount) {
        this.cartBadgeAnimate = true;
        setTimeout(() => {
          this.cartBadgeAnimate = false;
        }, 600);
      }
      this.lastCount = count;
    });
  }

  ngOnDestroy(): void {
    this.routerSubscription?.unsubscribe();
    this.cartSubscription?.unsubscribe();
  }

  private closeNavbarCollapse(): void {
    const navbar = document.getElementById('navbarContent');
    if (navbar && navbar.classList.contains('show')) {
      navbar.classList.remove('show');
      const toggler = document.querySelector('.navbar-toggler');
      if (toggler) {
        toggler.setAttribute('aria-expanded', 'false');
      }
    }
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }

  getTotal(): number {
    return this.cartService.getTotal();
  }

  getComboDiscountPercent(): number {
    return this.cartService.getComboDiscountPercent();
  }

  getComboDiscountAmount(): number {
    return this.cartService.getComboDiscountAmount();
  }

  getDiscountedTotal(): number {
    return this.cartService.getDiscountedTotal();
  }

  removeItem(item: CartItem): void {
    this.cartService.removeFromCart(item.service.id!, item.type);
  }

  onSearch(): void {
    const query = this.searchQuery.trim();
    if (query) {
      this.router.navigate(['/platforms'], { queryParams: { q: query } });
      this.closeNavbarCollapse();
    }
  }
}

