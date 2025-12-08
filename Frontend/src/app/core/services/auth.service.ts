import { Injectable, signal, computed, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
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
        const user = JSON.parse(storedUser) as User;
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
    const trimmedCredentials = {
      email: credentials.email.trim(),
      password: credentials.password.trim()
    };

    return this.httpClient
      .post<ApiResponse<BackendAuthResponse>>(`${this.apiUrl}/login`, trimmedCredentials)
      .pipe(
        // no need for tap/map split; single map is fine
        (source) =>
          new Observable<User>((subscriber) => {
            source.subscribe({
              next: (response) => {
                console.log('Raw backend response:', response);

                if (!response.success || !response.data) {
                  subscriber.error(
                    new Error(response.message || 'Login failed')
                  );
                  return;
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

                subscriber.next(user);
                subscriber.complete();
              },
              error: (err) => subscriber.error(err)
            });
          })
      );
  }

  signup(data: SignupData): Observable<User> {
    return this.httpClient
      .post<ApiResponse<BackendAuthResponse>>(`${this.apiUrl}/register`, {
        email: data.email,
        password: data.password,
        name: data.name,
        phone: data.phone || ''
      })
      .pipe(
        (source) =>
          new Observable<User>((subscriber) => {
            source.subscribe({
              next: (response) => {
                if (!response.success || !response.data) {
                  subscriber.error(
                    new Error(response.message || 'Registration failed')
                  );
                  return;
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

                subscriber.next(user);
                subscriber.complete();
              },
              error: (err) => subscriber.error(err)
            });
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
    return this.httpClient
      .post<ApiResponse<string>>(`${this.apiUrl}/verify-otp`, {
        email,
        otp
      })
      .pipe((source) =>
        new Observable<void>((subscriber) => {
          source.subscribe({
            next: (response) => {
              if (!response.success) {
                subscriber.error(
                  new Error(response.message || 'OTP verification failed')
                );
                return;
              }
              subscriber.next();
              subscriber.complete();
            },
            error: (err) => subscriber.error(err)
          });
        })
      );
  }

  resendOtp(email: string): Observable<void> {
    return this.httpClient
      .post<ApiResponse<string>>(`${this.apiUrl}/resend-otp`, null, {
        params: { email }
      })
      .pipe((source) =>
        new Observable<void>((subscriber) => {
          source.subscribe({
            next: (response) => {
              if (!response.success) {
                subscriber.error(
                  new Error(response.message || 'Failed to resend OTP')
                );
                return;
              }
              subscriber.next();
              subscriber.complete();
            },
            error: (err) => subscriber.error(err)
          });
        })
      );
  }

  private mapRole(
    backendRole: string | undefined | null
  ): 'customer' | 'admin' | 'delivery_agent' {
    console.log('mapRole input:', backendRole);

    if (!backendRole) {
      console.log('mapRole: No role provided, defaulting to customer');
      return 'customer';
    }

    const key = backendRole.toUpperCase().trim();

    const roleMap: { [k: string]: 'customer' | 'admin' | 'delivery_agent' } = {
      // customer-ish
      CUSTOMER: 'customer',
      ROLE_CUSTOMER: 'customer',
      USER: 'customer',
      ROLE_USER: 'customer',

      // admin
      ADMIN: 'admin',
      ROLE_ADMIN: 'admin',

      // delivery
      DELIVERY_AGENT: 'delivery_agent',
      ROLE_DELIVERY_AGENT: 'delivery_agent',
      DELIVERY: 'delivery_agent'
    };

    const mappedRole = roleMap[key] ?? 'customer';
    console.log('mapRole output:', mappedRole);
    return mappedRole;
  }
}