import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
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
  cartItemCount$: Observable<number> = this.cartService.items$.pipe(
    map(items => items.reduce((acc, item) => acc + item.quantity, 0))
  );

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
