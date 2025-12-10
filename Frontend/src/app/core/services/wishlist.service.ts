import { Injectable, signal, computed, Inject, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Product } from '../models/product.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WishlistService {
  private itemsSignal = signal<Product[]>([]);
  private http = inject(HttpClient);

  items = this.itemsSignal.asReadonly();
  itemCount = computed(() => this.itemsSignal().length);

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.loadFromStorage();
  }

  private loadFromStorage(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    const stored = localStorage.getItem('revcart_wishlist');
    if (stored) {
      try {
        this.itemsSignal.set(JSON.parse(stored));
      } catch (error) {
        console.error('Error loading wishlist:', error);
      }
    }
  }

  private saveToStorage(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem('revcart_wishlist', JSON.stringify(this.itemsSignal()));
  }

  addToWishlist(product: Product): void {
    if (!this.isInWishlist(product.id)) {
      this.itemsSignal.update(items => [...items, product]);
      this.saveToStorage();
      this.sendWishlistNotification(product.name);
    }
  }

  removeFromWishlist(productId: string): void {
    this.itemsSignal.update(items =>
      items.filter(item => item.id !== productId)
    );
    this.saveToStorage();
  }

  isInWishlist(productId: string): boolean {
    return this.itemsSignal().some(item => item.id === productId);
  }

  clearWishlist(): void {
    this.itemsSignal.set([]);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('revcart_wishlist');
    }
  }

  private sendWishlistNotification(productName: string): void {
    if (!isPlatformBrowser(this.platformId)) return;
    
    const userStr = localStorage.getItem('revcart_user');
    if (!userStr) return;

    try {
      const user = JSON.parse(userStr);
      const userId = user.id;
      if (!userId) return;

      this.http.post(`${environment.apiUrl}/notifications`, {
        userId: userId,
        title: 'Wishlist Updated',
        message: `${productName} added to wishlist successfully`,
        type: 'WISHLIST'
      }).subscribe({
        next: () => console.log('✅ Wishlist notification sent'),
        error: (err) => console.error('❌ Wishlist notification failed:', err)
      });
    } catch (error) {
      console.error('Error parsing user data:', error);
    }
  }
}
