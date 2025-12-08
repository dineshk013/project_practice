import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LucideAngularModule, CheckCircle, ArrowRight, Home } from 'lucide-angular';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-green-50 to-green-100 flex items-center justify-center p-4">
      <div class="w-full max-w-md">
        <div class="bg-white rounded-lg shadow-xl p-8 text-center">
          <!-- Success Icon -->
          <div class="flex justify-center mb-6">
            <div class="rounded-full bg-green-100 p-4">
              <lucide-icon [img]="CheckCircle" class="h-16 w-16 text-green-600"></lucide-icon>
            </div>
          </div>

          <!-- Success Message -->
          <h1 class="text-3xl font-bold text-gray-800 mb-2">Payment Successful!</h1>
          <p class="text-gray-600 mb-6">Your payment has been processed successfully.</p>

          <!-- Payment Details -->
          <div class="bg-gray-50 rounded-lg p-6 mb-6 text-left space-y-3">
            @if (paymentId) {
              <div class="flex justify-between">
                <span class="text-gray-600">Payment ID:</span>
                <span class="font-medium text-gray-800">{{ paymentId }}</span>
              </div>
            }
            @if (orderId) {
              <div class="flex justify-between">
                <span class="text-gray-600">Order ID:</span>
                <span class="font-medium text-gray-800">#{{ orderId }}</span>
              </div>
            }
            @if (amount) {
              <div class="flex justify-between border-t pt-3">
                <span class="text-gray-600 font-medium">Amount Paid:</span>
                <span class="font-bold text-primary text-lg">₹{{ amount.toFixed(2) }}</span>
              </div>
            }
          </div>

          <!-- Info Message -->
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <p class="text-sm text-blue-800">
              Your order has been confirmed. You will receive an email confirmation shortly.
            </p>
          </div>

          <!-- Actions -->
          <div class="flex flex-col sm:flex-row gap-3">
            <button
              (click)="viewOrders()"
              class="flex-1 flex items-center justify-center gap-2 px-6 py-3 bg-primary text-white rounded-md hover:bg-primary/90 transition-colors">
              View Orders
              <lucide-icon [img]="ArrowRight" class="h-4 w-4"></lucide-icon>
            </button>
            <button
              (click)="goHome()"
              class="flex-1 flex items-center justify-center gap-2 px-6 py-3 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
              <lucide-icon [img]="Home" class="h-4 w-4"></lucide-icon>
              Back to Home
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
  `]
})
export class PaymentSuccessComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly CheckCircle = CheckCircle;
  readonly ArrowRight = ArrowRight;
  readonly Home = Home;

  paymentId: string | null = null;
  orderId: string | null = null;
  amount: number | null = null;

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.paymentId = params['paymentId'] || null;
      this.orderId = params['orderId'] || null;
      this.amount = params['amount'] ? parseFloat(params['amount']) : null;
    });
  }

  viewOrders(): void {
    this.router.navigate(['/orders']);
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}



