import { Injectable, inject } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ServiceResponse } from '@neversion/api-client';
import { VendorService } from './vendor.service';

export interface CartItem {
  service: ServiceResponse;
  quantity: number;
  type: 'PROFILE' | 'COMPLETE';
  /** Only relevant for Spotify BY_PROFILE items. Captured at checkout. */
  spotifyAccountPreference?: 'CUENTA_NUEVA' | 'CUENTA_PROPIA';
}

export type CartValidationError =
  | 'DUPLICATE_SERVICE'
  | 'MAX_PROFILES_EXCEEDED'
  | 'FULL_ACCOUNT_EXCLUSIVE';

export interface CartValidationResult {
  ok: boolean;
  error?: CartValidationError;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private readonly vendorService = inject(VendorService);

  private itemsSubject = new BehaviorSubject<CartItem[]>([]);
  items$ = this.itemsSubject.asObservable();

  private static readonly MAX_PROFILES = 4;
  private static readonly STORAGE_KEY = 'neversion_store_cart';

  constructor() {
    this.loadFromStorage();
  }

  private loadFromStorage(): void {
    try {
      const stored = localStorage.getItem(CartService.STORAGE_KEY);
      if (stored) {
        const items: CartItem[] = JSON.parse(stored);
        if (Array.isArray(items)) {
          this.itemsSubject.next(items);
        }
      }
    } catch (err) {
      console.error('Failed to load cart from localStorage:', err);
    }
  }

  private updateItems(items: CartItem[]): void {
    this.itemsSubject.next(items);
    try {
      localStorage.setItem(CartService.STORAGE_KEY, JSON.stringify(items));
    } catch (err) {
      console.error('Failed to save cart to localStorage:', err);
    }
  }

  addToCart(
    service: ServiceResponse,
    type: 'PROFILE' | 'COMPLETE' = 'PROFILE'
  ): CartValidationResult {
    const currentItems = this.itemsSubject.value;

    // BR-13 v2: FULL_ACCOUNT must be the only item
    if (type === 'COMPLETE') {
      if (currentItems.length > 0) {
        return {
          ok: false,
          error: 'FULL_ACCOUNT_EXCLUSIVE',
          message: 'Una cuenta completa no puede combinarse con otros servicios en el mismo pedido.'
        };
      }
      this.updateItems([{ service, quantity: 1, type }]);
      return { ok: true };
    }

    // BR-13 v2: if there's a FULL_ACCOUNT in cart, can't add more
    if (currentItems.some(i => i.type === 'COMPLETE')) {
      return {
        ok: false,
        error: 'FULL_ACCOUNT_EXCLUSIVE',
        message: 'Ya hay una cuenta completa en el carrito. No se pueden agregar más servicios.'
      };
    }

    // BR-13 v2: no duplicate services
    if (currentItems.some(i => i.service.id === service.id)) {
      return {
        ok: false,
        error: 'DUPLICATE_SERVICE',
        message: `El servicio "${service.name}" ya está en el carrito. Cada servicio solo puede aparecer una vez.`
      };
    }

    // BR-13 v2: max 4 profile services
    const profileCount = currentItems.filter(i => i.type === 'PROFILE').length;
    if (profileCount >= this.getMaxProfiles()) {
      return {
        ok: false,
        error: 'MAX_PROFILES_EXCEEDED',
        message: `No se pueden agregar más de ${this.getMaxProfiles()} servicios de perfil en un solo pedido. Para 5 o más, contacta al vendedor por WhatsApp.`
      };
    }

    this.updateItems([...currentItems, { service, quantity: 1, type }]);
    return { ok: true };
  }

  updateQuantity(serviceId: string, type: 'PROFILE' | 'COMPLETE', quantity: number): void {
    if (quantity <= 0) {
      this.removeFromCart(serviceId, type);
      return;
    }
    const currentItems = this.itemsSubject.value;
    const item = currentItems.find(i => i.service.id === serviceId && i.type === type);
    if (item) {
      item.quantity = quantity;
      this.updateItems([...currentItems]);
    }
  }

  removeFromCart(serviceId: string, type: 'PROFILE' | 'COMPLETE'): void {
    const currentItems = this.itemsSubject.value;
    this.updateItems(currentItems.filter(item => !(item.service.id === serviceId && item.type === type)));
  }

  /**
   * Stores the Spotify account preference for a specific cart item.
   * Only meaningful when the item is a Spotify BY_PROFILE slot.
   */
  setSpotifyPreference(serviceId: string, preference: 'CUENTA_NUEVA' | 'CUENTA_PROPIA'): void {
    const currentItems = this.itemsSubject.value;
    const item = currentItems.find(i => i.service.id === serviceId && i.type === 'PROFILE');
    if (item) {
      item.spotifyAccountPreference = preference;
      this.updateItems([...currentItems]);
    }
  }

  clearCart(): void {
    this.itemsSubject.next([]);
    try {
      localStorage.removeItem(CartService.STORAGE_KEY);
    } catch (err) {
      console.error('Failed to clear cart from localStorage:', err);
    }
  }

  /**
   * Gross total of BY_PROFILE items only (discount applies to this).
   * FULL_ACCOUNT items pay full price and don't participate in discounts.
   */
  getProfileSubtotal(): number {
    return this.itemsSubject.value
      .filter(item => item.type === 'PROFILE')
      .reduce((acc, item) => acc + (item.service.priceProfile || 0) * item.quantity, 0);
  }

  /**
   * Total of FULL_ACCOUNT items (no discount).
   */
  getFullAccountSubtotal(): number {
    return this.itemsSubject.value
      .filter(item => item.type === 'COMPLETE')
      .reduce((acc, item) => acc + (item.service.priceComplete || 0) * item.quantity, 0);
  }

  getTotal(): number {
    return this.getProfileSubtotal() + this.getFullAccountSubtotal();
  }

  /**
   * Number of distinct profile services in the cart (for tier matching).
   */
  getProfileItemCount(): number {
    return this.itemsSubject.value.filter(item => item.type === 'PROFILE').length;
  }

  getCartCount(): number {
    return this.itemsSubject.value.reduce((acc, item) => acc + item.quantity, 0);
  }

  getItems(): CartItem[] {
    return this.itemsSubject.value;
  }

  hasFullAccount(): boolean {
    return this.itemsSubject.value.some(i => i.type === 'COMPLETE');
  }

  /**
   * Returns the combo discount percentage based on dynamic discount_cfg tiers.
   * Returns 0 if below min_items, no config, or no matching tier.
   */
  getComboDiscountPercent(): number {
    const cfg = this.vendorService.getDiscountConfig();
    const profileCount = this.getProfileItemCount();

    if (!cfg || profileCount < cfg.minItems) return 0;

    const tier = cfg.tiers.find(t => t.count === profileCount);
    return tier ? tier.discountPct : 0;
  }

  /**
   * Returns the combo discount amount (rounded to nearest Q5).
   * Only applies to BY_PROFILE items.
   */
  getComboDiscountAmount(): number {
    const percent = this.getComboDiscountPercent();
    if (percent === 0) return 0;

    const profileSubtotal = this.getProfileSubtotal();
    const rawDiscount = Math.round(profileSubtotal * percent) / 100;

    const cfg = this.vendorService.getDiscountConfig();
    const roundTo = cfg?.roundTo ?? 5;
    if (roundTo <= 0) return rawDiscount;

    return Math.round(rawDiscount / roundTo) * roundTo;
  }

  getDiscountedTotal(): number {
    return this.getTotal() - this.getComboDiscountAmount();
  }

  private getMaxProfiles(): number {
    const cfg = this.vendorService.getDiscountConfig();
    return cfg?.maxItems ?? CartService.MAX_PROFILES;
  }
}
