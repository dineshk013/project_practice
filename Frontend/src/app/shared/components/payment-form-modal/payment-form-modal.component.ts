import { Component, inject, signal, input, output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, X, CreditCard, Lock } from 'lucide-angular';

export interface CardDetails {
  cardHolderName: string;
  cardNumber: string;
  expiryMonth: string;
  expiryYear: string;
  cvv: string;
}

@Component({
  selector: 'app-payment-form-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  template: `
    @if (isOpen()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50" 
           (click)="onBackdropClick($event)">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 max-h-[90vh] overflow-y-auto"
             (click)="$event.stopPropagation()">
          <!-- Header -->
          <div class="flex items-center justify-between p-6 border-b">
            <div class="flex items-center gap-2">
              <lucide-icon [img]="CreditCard" class="h-5 w-5 text-primary"></lucide-icon>
              <h2 class="text-xl font-bold">Enter Card Details</h2>
            </div>
            <button 
              (click)="closeModal()" 
              class="text-gray-400 hover:text-gray-600 transition-colors">
              <lucide-icon [img]="X" class="h-5 w-5"></lucide-icon>
            </button>
          </div>

          <!-- Form -->
          <form (ngSubmit)="onSubmit()" class="p-6 space-y-4">
            <!-- Card Holder Name -->
            <div>
              <label class="block text-sm font-medium mb-1">Card Holder Name *</label>
              <input
                type="text"
                [(ngModel)]="cardDetails().cardHolderName"
                name="cardHolderName"
                required
                placeholder="John Doe"
                maxlength="50"
                class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                [class.border-red-500]="errors().cardHolderName"
              />
              @if (errors().cardHolderName) {
                <p class="text-red-500 text-xs mt-1">{{ errors().cardHolderName }}</p>
              }
            </div>

            <!-- Card Number -->
            <div>
              <label class="block text-sm font-medium mb-1">Card Number *</label>
              <input
                type="text"
                [(ngModel)]="cardDetails().cardNumber"
                name="cardNumber"
                required
                placeholder="1234 5678 9012 3456"
                maxlength="19"
                (input)="formatCardNumber($event)"
                class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                [class.border-red-500]="errors().cardNumber"
              />
              @if (errors().cardNumber) {
                <p class="text-red-500 text-xs mt-1">{{ errors().cardNumber }}</p>
              }
              <p class="text-gray-500 text-xs mt-1">16 digits required</p>
            </div>

            <!-- Expiry Date -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium mb-1">Expiry Month *</label>
                <select
                  [(ngModel)]="cardDetails().expiryMonth"
                  name="expiryMonth"
                  required
                  class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                  [class.border-red-500]="errors().expiryMonth"
                >
                  <option value="">Month</option>
                  @for (month of months; track month.value) {
                    <option [value]="month.value">{{ month.label }}</option>
                  }
                </select>
                @if (errors().expiryMonth) {
                  <p class="text-red-500 text-xs mt-1">{{ errors().expiryMonth }}</p>
                }
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">Expiry Year *</label>
                <select
                  [(ngModel)]="cardDetails().expiryYear"
                  name="expiryYear"
                  required
                  class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                  [class.border-red-500]="errors().expiryYear"
                >
                  <option value="">Year</option>
                  @for (year of years; track year) {
                    <option [value]="year">{{ year }}</option>
                  }
                </select>
                @if (errors().expiryYear) {
                  <p class="text-red-500 text-xs mt-1">{{ errors().expiryYear }}</p>
                }
              </div>
            </div>

            <!-- CVV -->
            <div>
              <label class="block text-sm font-medium mb-1">CVV *</label>
              <div class="relative">
                <lucide-icon [img]="Lock" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"></lucide-icon>
                <input
                  type="text"
                  [(ngModel)]="cardDetails().cvv"
                  name="cvv"
                  required
                  placeholder="123"
                  maxlength="3"
                  (input)="formatCVV($event)"
                  class="w-full pl-10 pr-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                  [class.border-red-500]="errors().cvv"
                />
              </div>
              @if (errors().cvv) {
                <p class="text-red-500 text-xs mt-1">{{ errors().cvv }}</p>
              }
            </div>

            <!-- Error Message -->
            @if (errorMessage() || externalError()) {
              <div class="p-3 bg-red-50 border border-red-200 text-red-700 rounded-md text-sm">
                {{ externalError() || errorMessage() }}
              </div>
            }

            <!-- Actions -->
            <div class="flex gap-3 pt-4">
              <button
                type="button"
                (click)="closeModal()"
                class="flex-1 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                Cancel
              </button>
              <button
                type="submit"
                [disabled]="isProcessing()"
                class="flex-1 px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/90 disabled:opacity-50 transition-colors">
                {{ isProcessing() ? 'Processing...' : 'Pay Now' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
  styles: [`
    :host {
      display: block;
    }
  `]
})
export class PaymentFormModalComponent {
  isOpen = input.required<boolean>();
  orderId = input<number | null>(null);
  amount = input<number | null>(null);
  externalError = input<string | null>(null);
  
  paymentSubmitted = output<CardDetails>();
  modalClosed = output<void>();

  readonly CreditCard = CreditCard;
  readonly X = X;
  readonly Lock = Lock;

  cardDetails = signal<CardDetails>({
    cardHolderName: '',
    cardNumber: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: ''
  });

  errors = signal<Partial<Record<keyof CardDetails, string>>>({});
  errorMessage = signal<string | null>(null);
  isProcessing = signal(false);

  months = Array.from({ length: 12 }, (_, i) => ({
    value: String(i + 1).padStart(2, '0'),
    label: String(i + 1).padStart(2, '0')
  }));

  years = Array.from({ length: 20 }, (_, i) => {
    const year = new Date().getFullYear() + i;
    return String(year);
  });

  formatCardNumber(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\s/g, ''); // Remove all spaces
    value = value.replace(/\D/g, ''); // Remove non-digits
    value = value.slice(0, 16); // Max 16 digits
    
    // Format with spaces every 4 digits
    const formatted = value.match(/.{1,4}/g)?.join(' ') || value;
    
    this.cardDetails.update(details => ({
      ...details,
      cardNumber: formatted
    }));
  }

  formatCVV(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, ''); // Remove non-digits
    value = value.slice(0, 3); // Max 3 digits
    
    this.cardDetails.update(details => ({
      ...details,
      cvv: value
    }));
  }

  validateForm(): boolean {
    const details = this.cardDetails();
    const newErrors: Partial<Record<keyof CardDetails, string>> = {};

    // Card Holder Name - Alphabets only
    if (!details.cardHolderName.trim()) {
      newErrors.cardHolderName = 'Card holder name is required';
    } else if (details.cardHolderName.trim().length < 2) {
      newErrors.cardHolderName = 'Name must be at least 2 characters';
    } else if (!/^[a-zA-Z\s]+$/.test(details.cardHolderName)) {
      newErrors.cardHolderName = 'Name must contain only letters';
    }

    // Card Number - Must be 16 digits
    const cardNumberDigits = details.cardNumber.replace(/\s/g, '');
    if (!cardNumberDigits) {
      newErrors.cardNumber = 'Card number is required';
    } else if (!/^\d+$/.test(cardNumberDigits)) {
      newErrors.cardNumber = 'Card number must contain only digits';
    } else if (cardNumberDigits.length !== 16) {
      newErrors.cardNumber = 'Card number must be exactly 16 digits';
    }

    // Expiry Month - Must be 01-12
    if (!details.expiryMonth) {
      newErrors.expiryMonth = 'Expiry month is required';
    } else {
      const month = parseInt(details.expiryMonth, 10);
      if (month < 1 || month > 12) {
        newErrors.expiryMonth = 'Invalid month (01-12)';
      }
    }

    // Expiry Year - Must be current year or future
    const currentYear = new Date().getFullYear();
    if (!details.expiryYear) {
      newErrors.expiryYear = 'Expiry year is required';
    } else {
      const year = parseInt(details.expiryYear, 10);
      if (year < currentYear) {
        newErrors.expiryYear = 'Card has expired';
      } else if (year > currentYear + 20) {
        newErrors.expiryYear = 'Invalid expiry year';
      }
    }

    // Check if card is expired (month + year)
    if (details.expiryMonth && details.expiryYear && !newErrors.expiryMonth && !newErrors.expiryYear) {
      const expMonth = parseInt(details.expiryMonth, 10);
      const expYear = parseInt(details.expiryYear, 10);
      const currentMonth = new Date().getMonth() + 1;
      
      if (expYear === currentYear && expMonth < currentMonth) {
        newErrors.expiryMonth = 'Card has expired';
      }
    }

    // CVV - Must be 3 digits
    if (!details.cvv) {
      newErrors.cvv = 'CVV is required';
    } else if (!/^\d+$/.test(details.cvv)) {
      newErrors.cvv = 'CVV must contain only digits';
    } else if (details.cvv.length !== 3) {
      newErrors.cvv = 'CVV must be exactly 3 digits';
    }

    this.errors.set(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  onSubmit(): void {
    this.errorMessage.set(null);
    this.errors.set({});

    if (!this.validateForm()) {
      return;
    }

    this.isProcessing.set(true);
    
    // Emit the card details to parent component
    this.paymentSubmitted.emit(this.cardDetails());
    
    // Reset processing state after a delay to allow parent to handle
    setTimeout(() => {
      if (this.isProcessing()) {
        this.isProcessing.set(false);
      }
    }, 5000);
  }

  closeModal(): void {
    if (this.isProcessing()) {
      return; // Prevent closing while processing
    }
    
    this.resetModal();
    this.modalClosed.emit();
  }

  private resetModal(): void {
    this.cardDetails.set({
      cardHolderName: '',
      cardNumber: '',
      expiryMonth: '',
      expiryYear: '',
      cvv: ''
    });
    this.errors.set({});
    this.errorMessage.set(null);
    this.isProcessing.set(false);
  }

  onBackdropClick(event: MouseEvent): void {
    if (!this.isProcessing()) {
      this.closeModal();
    }
  }

  setError(message: string): void {
    this.errorMessage.set(message);
    this.isProcessing.set(false);
  }
}

