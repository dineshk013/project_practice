import { Injectable, signal, computed, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, of, delay, tap, map, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, LoginCredentials, SignupData } from '../models/user.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface BackendAuthResponse {
  token: string;
  user: {
    id: number;
    email: string;
    name: string;
    phone: string;
    role: string;
    createdAt: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userSignal = signal<User | null>(null);
  private loadingSignal = signal<boolean>(true);

  // Computed signals
  user = this.userSignal.asReadonly();
  isAuthenticated = computed(() => this.userSignal() !== null);
  isLoading = this.loadingSignal.asReadonly();

  private apiUrl = `${environment.apiUrl}/users`;

  constructor(
    private router: Router,
    private httpClient: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.initializeAuth();
  }

  private initializeAuth(): void {
    if (!isPlatformBrowser(this.platformId)) {
      this.loadingSignal.set(false);
      return;
    }

    const storedUser = localStorage.getItem('revcart_user');
    if (storedUser) {
      try {
        const user = JSON.parse(storedUser);
        this.userSignal.set(user);
      } catch (error) {
        console.error('Error parsing stored user:', error);
        localStorage.removeItem('revcart_user');
        localStorage.removeItem('revcart_token');
      }
    }

    this.loadingSignal.set(false);
  }

  login(credentials: LoginCredentials): Observable<User> {
    // Trim credentials to avoid whitespace issues
    const trimmedCredentials = {
      email: credentials.email.trim(),
      password: credentials.password.trim()
    };
    
    return this.httpClient.post<ApiResponse<BackendAuthResponse>>(`${this.apiUrl}/login`, trimmedCredentials).pipe(
      map(response => {
        console.log('Raw backend response:', response);
        
        if (!response.success || !response.data) {
          throw new Error(response.message || 'Login failed');
        }

        const authData = response.data;
        console.log('Auth data:', authData);
        
        const user: User = {
          id: authData.user.id,
          email: authData.user.email,
          name: authData.user.name,
          role: this.mapRole(authData.user.role)
        };
        
        console.log('Mapped user:', user);

        this.userSignal.set(user);

        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('revcart_user', JSON.stringify(user));
          localStorage.setItem('revcart_token', authData.token);
        }

        return user;
      }),
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  signup(data: SignupData): Observable<User> {
    return this.httpClient.post<ApiResponse<BackendAuthResponse>>(`${this.apiUrl}/register`, {
      email: data.email,
      password: data.password,
      name: data.name,
      phone: data.phone || ''
    }).pipe(
      map(response => {
        if (!response.success || !response.data) {
          throw new Error(response.message || 'Registration failed');
        }

        const authData = response.data;
        const user: User = {
          id: authData.user.id,
          email: authData.user.email,
          name: authData.user.name,
          role: this.mapRole(authData.user.role)
        };

        this.userSignal.set(user);

        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('revcart_user', JSON.stringify(user));
          localStorage.setItem('revcart_token', authData.token);
        }

        return user;
      }),
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    this.userSignal.set(null);

    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('revcart_user');
      localStorage.removeItem('revcart_token');
    }

    this.router.navigate(['/auth/login']);
  }

  hasRole(role: string): boolean {
    return this.userSignal()?.role === role;
  }

  verifyOtp(email: string, otp: string): Observable<void> {
    return this.httpClient.post<ApiResponse<string>>(`${this.apiUrl}/verify-otp`, {
      email,
      otp
    }).pipe(
      map(response => {
        if (!response.success) {
          throw new Error(response.message || 'OTP verification failed');
        }
      }),
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  resendOtp(email: string): Observable<void> {
    return this.httpClient.post<ApiResponse<string>>(`${this.apiUrl}/resend-otp`, null, {
      params: { email }
    }).pipe(
      map(response => {
        if (!response.success) {
          throw new Error(response.message || 'Failed to resend OTP');
        }
      }),
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  private mapRole(backendRole: string | undefined | null): 'customer' | 'admin' | 'delivery_agent' {
    console.log('mapRole input:', backendRole);
    
    if (!backendRole) {
      console.log('mapRole: No role provided, defaulting to customer');
      return 'customer';
    }
    
    const roleMap: { [key: string]: 'customer' | 'admin' | 'delivery_agent' } = {
      'CUSTOMER': 'customer',
      'USER': 'customer',
      'ADMIN': 'admin',
      'DELIVERY_AGENT': 'delivery_agent'
    };
    
    const mappedRole = roleMap[backendRole.toUpperCase()] || 'customer';
    console.log('mapRole output:', mappedRole);
    return mappedRole;
  }
}

