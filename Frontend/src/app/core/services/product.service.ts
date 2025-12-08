import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, delay, catchError, map } from 'rxjs';
import { Product, Category } from '../models/product.model';
import { MOCK_PRODUCTS, MOCK_CATEGORIES } from '../../../assets/data/mock-data';
import { environment } from '../../../environments/environment';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface BackendCategoryDto {
  id: number;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
}

interface BackendProductDto {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  sku?: string;
  brand?: string;
  highlights?: string;
  active: boolean;
  category: BackendCategoryDto;
  stockQuantity: number;
  availableQuantity: number;
  categoryId?: number;
  categoryName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private categoriesUrl = `${environment.apiUrl}/categories`;
  private productsUrl = `${environment.apiUrl}/products`;

  constructor(private httpClient: HttpClient) {}

  getProducts(filters?: {
    category?: string;
    search?: string;
    minPrice?: number;
    maxPrice?: number;
  }): Observable<Product[]> {
    let params = new HttpParams().set('page', '0').set('size', '100');

    if (filters?.search) {
      params = params.set('keyword', filters.search);
    }

    return this.httpClient
      .get<ApiResponse<BackendProductDto[]>>(this.productsUrl, { params })
      .pipe(
        map((response) => {
          let products = (response.data || []).map(this.mapBackendProductToFrontend);

          // Client-side filtering (apply on mapped data)
          if (filters) {
            if (filters.category) {
              products = products.filter((p) => p.categoryId === filters.category);
            }
            if (filters.minPrice !== undefined) {
              products = products.filter((p) => p.price >= filters.minPrice!);
            }
            if (filters.maxPrice !== undefined) {
              products = products.filter((p) => p.price <= filters.maxPrice!);
            }
            if (filters.search) {
              const search = filters.search.toLowerCase();
              products = products.filter(
                (p) =>
                  p.name.toLowerCase().includes(search) ||
                  p.description.toLowerCase().includes(search)
              );
            }
          }

          return products;
        }),
        catchError((error) => {
          console.warn('Backend products unavailable, using mock:', error);
          // Fallback only if backend fails completely
          return of(MOCK_PRODUCTS).pipe(delay(300));
        })
      );
  }

  private mapBackendProductToFrontend = (backendProduct: BackendProductDto): Product => {
    const quantity = backendProduct.availableQuantity ?? backendProduct.stockQuantity ?? 0;
    return {
      id: String(backendProduct.id),
      name: backendProduct.name,
      price: Number(backendProduct.price),
      category: backendProduct.category?.name || backendProduct.categoryName || 'Uncategorized',
      categoryId: String(backendProduct.category?.id || backendProduct.categoryId || ''),
      image: backendProduct.imageUrl || '',
      unit: 'unit',
      description: backendProduct.description || '',
      inStock: backendProduct.active !== false && quantity > 0,
      rating: 4.5,
      reviews: 0,
      availableQuantity: quantity
    };
  };

  getProductById(id: string): Observable<Product | undefined> {
    return this.httpClient
      .get<ApiResponse<BackendProductDto>>(`${this.productsUrl}/${id}`)
      .pipe(
        map((response) => {
          const backendProduct = response.data;
          return this.mapBackendProductToFrontend(backendProduct);
        }),
        catchError((error) => {
          console.warn('Backend product unavailable, using mock:', error);
          const product = MOCK_PRODUCTS.find((p) => p.id === id);
          return of(product).pipe(delay(200));
        })
      );
  }

  getCategories(): Observable<Category[]> {
    return this.httpClient.get<ApiResponse<BackendCategoryDto[]>>(this.categoriesUrl).pipe(
      map((response) => {
        const categories = response.data || [];
        return categories.map((c) => ({
          id: String(c.id),
          name: c.name,
          icon: '🥕',
          image: c.imageUrl || ''
        }));
      }),
      catchError((error) => {
        console.warn('Backend categories unavailable, using mock:', error);
        return of(MOCK_CATEGORIES);
      })
    );
  }

  getBestSellers(limit?: number): Observable<Product[]> {
    return this.getProducts().pipe(
      map((products) => (limit ? products.slice(0, limit) : products)),
      catchError(() => {
        const sorted = [...MOCK_PRODUCTS].sort((a, b) => {
          const scoreA = a.rating * a.reviews;
          const scoreB = b.rating * b.reviews;
          return scoreB - scoreA;
        });
        return of(limit ? sorted.slice(0, limit) : sorted);
      })
    );
  }

  getNewArrivals(limit?: number): Observable<Product[]> {
    return this.getProducts().pipe(
      map((products) => {
        const newProducts = products.slice(0, limit || products.length);
        return newProducts;
      }),
      catchError((error) => {
        console.warn('Backend new arrivals unavailable, using mock:', error);
        const newProducts = [...MOCK_PRODUCTS].reverse();
        return of(limit ? newProducts.slice(0, limit) : newProducts);
      })
    );
  }
}
