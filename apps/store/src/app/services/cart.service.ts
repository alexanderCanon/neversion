import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ServiceResponse } from '@neversion/api-client';

export interface CartItem {
  service: ServiceResponse;
  quantity: number;
  type: 'PROFILE' | 'COMPLETE';
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private itemsSubject = new BehaviorSubject<CartItem[]>([]);
  items$ = this.itemsSubject.asObservable();

  addToCart(service: ServiceResponse, type: 'PROFILE' | 'COMPLETE' = 'PROFILE'): void {
    const currentItems = this.itemsSubject.value;
    const existingItem = currentItems.find(item => item.service.id === service.id && item.type === type);

    if (existingItem) {
      existingItem.quantity += 1;
      this.itemsSubject.next([...currentItems]);
    } else {
      this.itemsSubject.next([...currentItems, { service, quantity: 1, type }]);
    }
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
      this.itemsSubject.next([...currentItems]);
    }
  }

  removeFromCart(serviceId: string, type: 'PROFILE' | 'COMPLETE'): void {
    const currentItems = this.itemsSubject.value;
    this.itemsSubject.next(currentItems.filter(item => !(item.service.id === serviceId && item.type === type)));
  }

  clearCart(): void {
    this.itemsSubject.next([]);
  }

  getTotal(): number {
    return this.itemsSubject.value.reduce((acc, item) => {
      const price = item.type === 'PROFILE' ? (item.service.priceProfile || 0) : (item.service.priceComplete || 0);
      return acc + (price * item.quantity);
    }, 0);
  }

  getCartCount(): number {
    return this.itemsSubject.value.reduce((acc, item) => acc + item.quantity, 0);
  }

  getItems(): CartItem[] {
    return this.itemsSubject.value;
  }

  /**
   * Returns the combo discount percentage based on BR-13 tiers.
   * 2-3 items → 5%, 4+ items → 10%, otherwise 0%.
   * These tiers match the backend's discount_cfg for the vendor.
   */
  getComboDiscountPercent(): number {
    const totalItems = this.getCartCount();
    if (totalItems >= 4) return 10;
    if (totalItems >= 2) return 5;
    return 0;
  }

  getComboDiscountAmount(): number {
    const percent = this.getComboDiscountPercent();
    if (percent === 0) return 0;
    return Math.round(this.getTotal() * percent) / 100;
  }

  getDiscountedTotal(): number {
    return this.getTotal() - this.getComboDiscountAmount();
  }
}
