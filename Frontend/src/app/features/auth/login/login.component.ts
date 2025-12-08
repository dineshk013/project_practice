import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { LucideAngularModule, Mail, Lock, ShoppingCart } from 'lucide-angular';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-primary/10 to-primary/5 flex items-center justify-center p-4">
      <div class="w-full max-w-md">
        <div class="bg-white rounded-lg shadow-xl p-8">
          <!-- Logo -->
          <div class="flex items-center justify-center gap-2 mb-6">
            <lucide-icon [img]="ShoppingCart" class="h-8 w-8 text-primary"></lucide-icon>
            <h1 class="text-2xl font-bold">RevCart</h1>
          </div>

          <h2 class="text-2xl font-bold text-center mb-6">Welcome Back</h2>

          <p class="text-center text-sm text-gray-600 mb-6">
            Login to access your account.
          </p>

          @if (errorMessage) {
            <div class="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-md text-sm">
              {{ errorMessage }}
            </div>
          }

          <form (ngSubmit)="onLogin()" class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">Email</label>
              <div class="relative">
                <lucide-icon [img]="Mail" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"></lucide-icon>
                <input
                  type="email"
                  [(ngModel)]="email"
                  name="email"
                  required
                  placeholder="email@example.com"
                  class="w-full pl-10 pr-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium mb-1">Password</label>
              <div class="relative">
                <lucide-icon [img]="Lock" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"></lucide-icon>
                <input
                  type="password"
                  [(ngModel)]="password"
                  name="password"
                  required
                  placeholder="******"
                  class="w-full pl-10 pr-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
            </div>

            <button
              type="submit"
              [disabled]="isLoading"
              class="w-full py-2 bg-primary text-white rounded-md hover:bg-primary/90 disabled:opacity-50"
            >
              {{ isLoading ? 'Logging in...' : 'Login' }}
            </button>
          </form>

          <div class="mt-6 text-center text-sm">
            <p class="text-gray-600">
              Don't have an account?
              <a routerLink="/auth/signup" class="text-primary hover:underline font-medium">Sign up</a>
            </p>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  authService = inject(AuthService);
  router = inject(Router);

  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  readonly ShoppingCart = ShoppingCart;
  readonly Mail = Mail;
  readonly Lock = Lock;

  onLogin(): void {
    // Trim and normalize inputs
    const cleanEmail = this.email.trim().toLowerCase();
    const cleanPassword = this.password.trim();

    if (!cleanEmail || !cleanPassword) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login({ email: cleanEmail, password: cleanPassword })
      .subscribe({
        next: (user) => {
          this.isLoading = false;
          console.log('Login successful:', user);
          console.log('User role:', user.role);
          console.log('Role check (admin):', user.role === 'admin');

          // Navigate based on role
          if (user.role === 'admin') {
            console.log('Navigating to /admin');
            this.router.navigate(['/admin']);
          } else if (user.role === 'delivery_agent') {
            console.log('Navigating to /delivery');
            this.router.navigate(['/delivery']);
          } else {
            console.log('Navigating to /');
            this.router.navigate(['/']);
          }
        },
        error: (err) => {
          this.isLoading = false;
          console.error('Login error:', err);

          if (err.status === 0) {
            this.errorMessage = 'Unable to connect to server. Is backend running?';
            return;
          }

          if (err.status === 400 || err.status === 401) {
            const backendMessage = err.error?.message;
            this.errorMessage = backendMessage || 'Invalid email or password';
            return;
          }

          this.errorMessage = err.error?.message || 'Login failed. Please try again.';
        }
      });
  }
}
