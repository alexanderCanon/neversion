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
}
