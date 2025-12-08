import { Injectable, signal, computed, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { CartItem } from '../models/cart.model';
import { Product } from '../models/product.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private itemsSignal = signal<CartItem[]>([]);
  private apiUrl = `${environment.apiUrl}/cart`;

  // ---------- Cart Observables ----------
  items = this.itemsSignal.asReadonly();
  total = computed(() =>
    this.itemsSignal().reduce((sum, item) => sum + item.price * item.quantity, 0)
  );
  itemCount = computed(() =>
    this.itemsSignal().reduce((sum, item) => sum + item.quantity, 0)
  );

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private httpClient: HttpClient
  ) {
    this.loadCartFromStorage();

    // Load backend cart only if logged-in
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('revcart_token');
      if (token) {
        this.loadCartFromBackend();
      }
    }
  }

  // ======================================================
  //                 LOCAL STORAGE
  // ======================================================

  private loadCartFromStorage(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    const stored = localStorage.getItem('revcart_cart');
    if (stored) {
      try {
        this.itemsSignal.set(JSON.parse(stored));
      } catch {
        localStorage.removeItem('revcart_cart');
      }
    }
  }

  private saveCartToStorage(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem('revcart_cart', JSON.stringify(this.itemsSignal()));
  }

  // ======================================================
  //          ADD & UPDATE FRONTEND CART ONLY
  // ======================================================
  addToCart(product: Product, quantity: number = 1): void {
    const currentItems = this.itemsSignal();
    const existing = currentItems.find(i => i.id === product.id);
    
    if (existing) {
      // Increment existing item
      this.itemsSignal.update(items =>
        items.map(i =>
          i.id === product.id ? { ...i, quantity: i.quantity + quantity } : i
        )
      );
    } else {
      // Add new item
      this.itemsSignal.update(items => [
        ...items,
        {
          id: product.id,
          name: product.name,
          price: product.price,
          quantity,
          image: product.image,
          unit: product.unit,
          availableQuantity: product.availableQuantity
        } satisfies CartItem
      ]);
    }

    this.saveCartToStorage();
    this.sendCartItemToBackend(product.id, quantity);
  }

  updateQuantity(productId: string, quantity: number): void {
    if (quantity <= 0) return this.removeFromCart(productId);

    this.itemsSignal.update(items =>
      items.map(i => (i.id === productId ? { ...i, quantity } : i))
    );

    this.saveCartToStorage();
    // Send absolute quantity to backend
    this.updateCartItemBackend(productId, quantity);
  }

  removeFromCart(productId: string): void {
    this.itemsSignal.update(items => items.filter(i => i.id !== productId));
    this.saveCartToStorage();
    this.removeItemFromBackend(productId);
  }

  clearCart(): void {
    this.itemsSignal.set([]);
    this.saveCartToStorage();
    // Backend will clear after order finish
  }

  // ======================================================
  //                 BACKEND SYNC HELPERS
  // ======================================================
  private sendCartItemToBackend(productId: string, quantity: number) {
    console.log('🔑 Sending to backend:', { productId: parseInt(productId), quantity });

    this.httpClient.post(
      `${this.apiUrl}/items`,
      {
        productId: parseInt(productId),
        quantity: Number(quantity)
      }
    ).subscribe({
      next: (response) => {
        console.log('✅ Cart synced to backend:', response);
        // ❗ DO NOT reload cart (Fixes abnormal increments)
      },
      error: (err) => {
        console.error('❌ Cart sync failed:', err);
      }
    });
  }

  private updateCartItemBackend(productId: string, quantity: number) {
    const item = this.itemsSignal().find(i => i.id === productId);
    if (!item) return;

    this.httpClient.put(
      `${this.apiUrl}/items/${item.id}`,
      null,
      { params: { quantity: quantity.toString() } }
    ).subscribe({
      next: () => console.log('✅ Cart quantity updated backend'),
      error: (err) => console.error('❌ Backend update failed:', err)
    });
  }

  private removeItemFromBackend(productId: string) {
    const item = this.itemsSignal().find(i => i.id === productId);
    if (!item) return;

    this.httpClient.delete(`${this.apiUrl}/items/${item.id}`).subscribe({
      next: () => console.log('✔️ Item removed backend'),
      error: (err) => console.error('❌ Backend remove failed:', err)
    });
  }

  // ======================================================
  //              LOAD FROM BACKEND (Login/Refresh)
  // ======================================================
  loadCartFromBackend(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const token = localStorage.getItem('revcart_token');
    if (!token) return;

    this.httpClient.get<any>(`${this.apiUrl}`).subscribe({
      next: (response) => {
        console.log("📦 Cart from backend:", response);

        const cart = response.data || response;
        if (!cart || !cart.items) return;

        const items: CartItem[] = cart.items.map((i: any) => ({
          id: i.productId.toString(),
          name: i.productName || i.name,
          price: i.price,
          quantity: i.quantity,
          image: i.imageUrl || '',
          unit: i.unit || 'unit',
          availableQuantity: i.availableQuantity ?? 0
        }));

        this.itemsSignal.set(items);
        this.saveCartToStorage();
      },
      error: (err) => {
        if (err.status !== 400 && err.status !== 401) {
          console.warn('❌ Failed backend cart load:', err);
        }
      }
    });
  }

  // ======================================================
  //                 STOCK UPDATE
  // ======================================================
  updateItemsStock(stockMap: Map<string, number>): void {
    this.itemsSignal.update(items =>
      items.map(item => ({
        ...item,
        availableQuantity: stockMap.get(item.id) ?? item.availableQuantity
      }))
    );
    this.saveCartToStorage();
  }
}
