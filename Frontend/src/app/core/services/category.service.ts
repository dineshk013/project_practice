import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  // Public: Get all categories
  getCategories(): Observable<Category[]> {
    return this.http.get<ApiResponse<Category[]>>(`${this.apiUrl}/categories`).pipe(
      map(response => response.data)
    );
  }

  // Admin: Get all categories
  getAllCategories(): Observable<Category[]> {
    return this.http.get<ApiResponse<Category[]>>(`${this.apiUrl}/categories`).pipe(
      map(response => response.data)
    );
  }

  // Admin: Get category by ID
  getCategoryById(id: number): Observable<Category> {
    return this.http.get<ApiResponse<Category>>(`${this.apiUrl}/categories/${id}`).pipe(
      map(response => response.data)
    );
  }

  // Admin: Create category
  createCategory(category: { name: string; slug: string; description?: string }): Observable<Category> {
    return this.http.post<ApiResponse<Category>>(`${this.apiUrl}/categories`, category).pipe(
      map(response => response.data)
    );
  }

  // Admin: Update category
  updateCategory(id: number, category: { name: string; slug: string; description?: string }): Observable<Category> {
    return this.http.put<ApiResponse<Category>>(`${this.apiUrl}/categories/${id}`, category).pipe(
      map(response => response.data)
    );
  }

  // Admin: Delete category
  deleteCategory(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/categories/${id}`).pipe(
      map(() => undefined)
    );
  }
}

