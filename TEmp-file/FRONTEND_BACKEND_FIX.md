# Frontend-Backend Integration Fix

## Root Cause Analysis

### 1. **Products API Issue**
- **Backend Returns**: `ApiResponse<List<ProductDto>>` with nested structure
- **Frontend Expects**: Direct array or needs to unwrap `response.data`
- **Error**: `Cannot read properties of undefined (reading 'map')` - frontend tries to map undefined

### 2. **Login API Issue (400 Bad Request)**
- **Backend Expects**: `{ "email": "...", "password": "..." }`
- **Frontend May Send**: Different field names or structure
- **Validation**: Backend uses `@Valid` with `@NotBlank` and `@Email`

### 3. **CORS Configuration**
- Gateway CORS may not be properly configured for preflight OPTIONS requests

---

## Backend Response Structures

### Products API Response
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Fresh Tomatoes",
      "description": "Organic tomatoes",
      "price": 50.0,
      "imageUrl": "https://...",
      "sku": "SKU-001",
      "brand": "FreshFarm",
      "highlights": "Organic, Fresh",
      "active": true,
      "category": {
        "id": 1,
        "name": "Vegetables",
        "slug": "vegetables",
        "description": "Fresh vegetables",
        "imageUrl": "https://..."
      },
      "stockQuantity": 100
    }
  ]
}
```

### Login API Request/Response
**Request:**
```json
{
  "email": "admin@revcart.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGc...",
    "userId": 1,
    "email": "admin@revcart.com",
    "name": "Admin User",
    "role": "ADMIN"
  }
}
```

### Register API Request
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe",
  "phone": "1234567890"
}
```

---

## Fixed Code

### 1. Gateway CORS Configuration (application.yml)

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:4200"
            allowedHeaders: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowCredentials: true
            maxAge: 3600
```

### 2. Product Service - Fixed (product.service.ts)

```typescript
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
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private categoriesUrl = `${environment.apiUrl}/categories`;
  private productsUrl = `${environment.apiUrl}/products`;

  constructor(private httpClient: HttpClient) { }

  getProducts(filters?: {
    category?: string;
    search?: string;
    minPrice?: number;
    maxPrice?: number;
  }): Observable<Product[]> {
    let params = new HttpParams();

    if (filters?.search) {
      params = params.set('keyword', filters.search);
    }

    return this.httpClient.get<ApiResponse<BackendProductDto[]>>(this.productsUrl, { params }).pipe(
      map(response => {
        // Handle ApiResponse wrapper
        const products = response.data || [];
        let mappedProducts = products.map(this.mapBackendProductToFrontend);

        // Apply client-side filters
        if (filters) {
          if (filters.category) {
            mappedProducts = mappedProducts.filter((p) => p.categoryId === filters.category);
          }
          if (filters.minPrice !== undefined) {
            mappedProducts = mappedProducts.filter((p) => p.price >= filters.minPrice!);
          }
          if (filters.maxPrice !== undefined) {
            mappedProducts = mappedProducts.filter((p) => p.price <= filters.maxPrice!);
          }
        }

        return mappedProducts;
      }),
      catchError((error) => {
        console.warn('Backend products unavailable, using mock:', error);
        let products = [...MOCK_PRODUCTS];
        if (filters) {
          if (filters.category) {
            products = products.filter((p) => p.categoryId === filters.category);
          }
          if (filters.search) {
            const search = filters.search.toLowerCase();
            products = products.filter(
              (p) =>
                p.name.toLowerCase().includes(search) ||
                p.description.toLowerCase().includes(search)
            );
          }
          if (filters.minPrice !== undefined) {
            products = products.filter((p) => p.price >= filters.minPrice!);
          }
          if (filters.maxPrice !== undefined) {
            products = products.filter((p) => p.price <= filters.maxPrice!);
          }
        }
        return of(products).pipe(delay(300));
      })
    );
  }

  private mapBackendProductToFrontend = (backendProduct: BackendProductDto): Product => {
    return {
      id: String(backendProduct.id),
      name: backendProduct.name,
      price: Number(backendProduct.price),
      category: backendProduct.category?.name || 'Uncategorized',
      categoryId: String(backendProduct.category?.id || ''),
      image: backendProduct.imageUrl || '',
      unit: 'unit',
      description: backendProduct.description || '',
      inStock: (backendProduct.stockQuantity || 0) > 0,
      rating: 4.5,
      reviews: 0,
      availableQuantity: backendProduct.stockQuantity || 0
    };
  };

  getProductById(id: string): Observable<Product | undefined> {
    return this.httpClient.get<ApiResponse<BackendProductDto>>(`${this.productsUrl}/${id}`).pipe(
      map(response => {
        const backendProduct = response.data;
        return this.mapBackendProductToFrontend(backendProduct);
      }),
      catchError((error) => {
        console.warn('Backend product unavailable, using mock:', error);
        const product = MOCK_PRODUCTS.find(p => p.id === id);
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
      map(products => limit ? products.slice(0, limit) : products),
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
      map(products => {
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
```

### 3. Auth Service - Fixed (auth.service.ts)

```typescript
login(credentials: LoginCredentials): Observable<User> {
  // Ensure correct field names
  const loginRequest = {
    email: credentials.email,
    password: credentials.password
  };

  return this.httpClient.post<ApiResponse<BackendAuthResponse>>(
    `${this.apiUrl}/login`, 
    loginRequest
  ).pipe(
    map(response => {
      if (!response.success || !response.data) {
        throw new Error(response.message || 'Login failed');
      }

      const authData = response.data;
      const user: User = {
        id: String(authData.userId),
        email: authData.email,
        name: authData.name,
        role: this.mapRole(authData.role)
      };

      this.userSignal.set(user);

      if (isPlatformBrowser(this.platformId)) {
        localStorage.setItem('revcart_user', JSON.stringify(user));
        localStorage.setItem('revcart_token', authData.token);
      }

      return user;
    }),
    catchError(error => {
      console.error('Login error:', error);
      return throwError(() => new Error(error.error?.message || 'Login failed'));
    })
  );
}

signup(data: SignupData): Observable<User> {
  // Ensure correct field names matching backend RegisterRequest
  const registerRequest = {
    email: data.email,
    password: data.password,
    name: data.name,
    phone: data.phone || ''
  };

  return this.httpClient.post<ApiResponse<BackendAuthResponse>>(
    `${this.apiUrl}/register`, 
    registerRequest
  ).pipe(
    map(response => {
      if (!response.success || !response.data) {
        throw new Error(response.message || 'Registration failed');
      }

      const authData = response.data;
      const user: User = {
        id: String(authData.userId),
        email: authData.email,
        name: authData.name,
        role: this.mapRole(authData.role)
      };

      this.userSignal.set(user);

      if (isPlatformBrowser(this.platformId)) {
        localStorage.setItem('revcart_user', JSON.stringify(user));
        localStorage.setItem('revcart_token', authData.token);
      }

      return user;
    }),
    catchError(error => {
      console.error('Registration error:', error);
      return throwError(() => new Error(error.error?.message || 'Registration failed'));
    })
  );
}
```

---

## Testing Commands

### Test Products API
```powershell
# Should return products with ApiResponse wrapper
Invoke-RestMethod -Uri "http://localhost:8080/api/products"
```

### Test Login API
```powershell
$loginBody = @{
    email = "admin@revcart.com"
    password = "admin123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody
```

### Test Register API
```powershell
$registerBody = @{
    email = "test@example.com"
    password = "test123"
    name = "Test User"
    phone = "1234567890"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $registerBody
```

---

## Summary of Fixes

1. ✅ **Product Service**: Handle `ApiResponse<T>` wrapper, check for `response.data`
2. ✅ **Auth Service**: Correct field names (`name` not `fullName`, no `role` in register)
3. ✅ **CORS**: Proper configuration with `allowCredentials` and `maxAge`
4. ✅ **Error Handling**: Graceful fallback to mock data with proper error logging
5. ✅ **Type Safety**: Proper TypeScript interfaces matching backend DTOs

## Next Steps

1. Replace the files with fixed versions
2. Restart gateway service (for CORS changes)
3. Refresh frontend (Ctrl+F5)
4. Test login with admin@revcart.com / admin123
5. Verify products load from database
