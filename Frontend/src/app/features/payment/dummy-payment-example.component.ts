import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PaymentService } from '../../core/services/payment.service';

/**
 * Example component demonstrating dummy Razorpay payment integration
 * 
 * This is a sample implementation showing how to:
 * 1. Call the dummy payment API
 * 2. Handle success/error responses
 * 3. Navigate to success page
 * 
 * To use in your checkout/order component:
 * 1. Inject PaymentService
 * 2. Call processDummyPayment(orderId, amount)
 * 3. Handle the response
 */
@Component({
  selector: 'app-dummy-payment-example',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="payment-container">
      <h2>Dummy Payment Example</h2>
      
      <div class="payment-form">
        <button 
          (click)="handlePayment()" 
          [disabled]="isProcessing()"
          class="pay-button">
          {{ isProcessing() ? 'Processing...' : 'Pay Now' }}
        </button>
      </div>

      @if (errorMessage()) {
        <div class="error-message">
          {{ errorMessage() }}
        </div>
      }

      @if (successMessage()) {
        <div class="success-message">
          {{ successMessage() }}
        </div>
      }
    </div>
  `,
  styles: [`
    .payment-container {
      max-width: 500px;
      margin: 2rem auto;
      padding: 2rem;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
    }

    .pay-button {
      width: 100%;
      padding: 12px 24px;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      font-size: 16px;
      cursor: pointer;
    }

    .pay-button:disabled {
      background-color: #ccc;
      cursor: not-allowed;
    }

    .error-message {
      margin-top: 1rem;
      padding: 12px;
      background-color: #fee;
      border: 1px solid #fcc;
      border-radius: 4px;
      color: #c33;
    }

    .success-message {
      margin-top: 1rem;
      padding: 12px;
      background-color: #efe;
      border: 1px solid #cfc;
      border-radius: 4px;
      color: #3c3;
    }
  `]
})
export class DummyPaymentExampleComponent {
  private paymentService = inject(PaymentService);
  private router = inject(Router);

  // Example order data - replace with actual order data from your checkout
  private orderId = 123; // Replace with actual order ID
  private orderAmount = 1500.00; // Replace with actual order amount

  isProcessing = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  /**
   * Handle payment button click
   * This method demonstrates the complete payment flow
   */
  handlePayment(): void {
    // Reset messages
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.isProcessing.set(true);

    // Call the dummy payment API
    this.paymentService.processDummyPayment(this.orderId, this.orderAmount)
      .subscribe({
        next: (response) => {
          this.isProcessing.set(false);

          if (response.success && response.data) {
            const paymentData = response.data;

            // Show success message
            this.successMessage.set(
              `Payment successful! Payment ID: ${paymentData.paymentId}`
            );

            // Navigate to payment success page after a short delay
            setTimeout(() => {
              this.router.navigate(['/payment/success'], {
                queryParams: {
                  paymentId: paymentData.paymentId,
                  orderId: this.orderId,
                  amount: this.orderAmount
                }
              });
            }, 2000);
          } else {
            this.errorMessage.set(response.message || 'Payment failed');
          }
        },
        error: (error) => {
          this.isProcessing.set(false);

          // Handle different error scenarios
          if (error.status === 0) {
            this.errorMessage.set('Unable to connect to server. Please check your connection.');
          } else if (error.status === 400) {
            this.errorMessage.set(error.error?.message || 'Invalid payment request');
          } else if (error.status === 404) {
            this.errorMessage.set('Order not found');
          } else {
            this.errorMessage.set(
              error.error?.message || 'Payment failed. Please try again.'
            );
          }
        }
      });
  }
}

/**
 * USAGE SNIPPET FOR YOUR CHECKOUT COMPONENT:
 * 
 * 1. Import the PaymentService:
 *    import { PaymentService } from '../../core/services/payment.service';
 * 
 * 2. Inject it in your component:
 *    private paymentService = inject(PaymentService);
 * 
 * 3. After order is placed, call the payment API:
 * 
 *    processPayment(orderId: number, amount: number): void {
 *      this.isProcessing.set(true);
 *      
 *      this.paymentService.processDummyPayment(orderId, amount)
 *        .subscribe({
 *          next: (response) => {
 *            if (response.success && response.data) {
 *              // Payment successful
 *              this.router.navigate(['/payment/success'], {
 *                queryParams: {
 *                  paymentId: response.data.paymentId,
 *                  orderId: orderId
 *                }
 *              });
 *            }
 *          },
 *          error: (error) => {
 *            this.errorMessage.set(
 *              error.error?.message || 'Payment failed'
 *            );
 *            this.isProcessing.set(false);
 *          }
 *        });
 *    }
 * 
 * 4. In your template, trigger payment on button click:
 * 
 *    <button 
 *      (click)="processPayment(order.id, order.totalAmount)"
 *      [disabled]="isProcessing()">
 *      Pay Now
 *    </button>
 */



