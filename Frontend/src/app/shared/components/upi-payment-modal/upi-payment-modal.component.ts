import { Component, signal, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, X, Smartphone } from 'lucide-angular';

export interface UpiDetails {
  upiId: string;
}

interface UpiApp {
  name: string;
  icon: string;
  suffix: string;
}

@Component({
  selector: 'app-upi-payment-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  template: `
    @if (isOpen()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50" 
           (click)="onBackdropClick($event)">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4"
             (click)="$event.stopPropagation()">
          <!-- Razorpay Header -->
          <div class="bg-[#528FF0] text-white p-6 rounded-t-lg">
            <div class="flex items-center justify-between">
              <div>
                <h2 class="text-xl font-bold">RevCart</h2>
                <p class="text-sm opacity-90">Powered by Razorpay</p>
              </div>
              <button (click)="closeModal()" class="text-white hover:bg-white/20 rounded p-1 transition-colors">
                <lucide-icon [img]="X" class="h-5 w-5"></lucide-icon>
              </button>
            </div>
            @if (amount()) {
              <div class="mt-4">
                <p class="text-sm opacity-90">Amount to pay</p>
                <p class="text-2xl font-bold">₹{{ amount()?.toFixed(2) }}</p>
              </div>
            }
          </div>

          <form (ngSubmit)="onSubmit()" class="p-6 space-y-4">
            <!-- Popular UPI Apps -->
            <div>
              <label class="block text-sm font-medium mb-2">Choose UPI App</label>
              <div class="grid grid-cols-3 gap-3 mb-4">
                @for (app of upiApps; track app.name) {
                  <button
                    type="button"
                    (click)="selectUpiApp(app)"
                    class="p-3 border rounded-lg hover:border-[#528FF0] hover:bg-blue-50 transition-colors text-center">
                    <div class="text-2xl mb-1">{{ app.icon }}</div>
                    <div class="text-xs font-medium">{{ app.name }}</div>
                  </button>
                }
              </div>
            </div>

            <div class="relative">
              <div class="absolute inset-0 flex items-center">
                <div class="w-full border-t border-gray-300"></div>
              </div>
              <div class="relative flex justify-center text-sm">
                <span class="px-2 bg-white text-gray-500">or enter UPI ID</span>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium mb-1">UPI ID *</label>
              <input
                type="text"
                [(ngModel)]="upiDetails().upiId"
                name="upiId"
                required
                placeholder="yourname@paytm"
                class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-[#528FF0]"
                [class.border-red-500]="error()"
              />
              @if (error()) {
                <p class="text-red-500 text-xs mt-1">{{ error() }}</p>
              }
              <p class="text-gray-500 text-xs mt-1">Example: success&#64;razorpay</p>
            </div>

            @if (externalError()) {
              <div class="p-3 bg-red-50 border border-red-200 text-red-700 rounded-md text-sm">
                {{ externalError() }}
              </div>
            }

            <div class="pt-4">
              <button type="submit" [disabled]="isProcessing()" class="w-full px-4 py-3 bg-[#528FF0] text-white rounded-md hover:bg-[#3d7ad6] disabled:opacity-50 transition-colors font-medium">
                {{ isProcessing() ? 'Processing Payment...' : 'Pay ₹' + (amount()?.toFixed(2) || '0.00') }}
              </button>
              <p class="text-xs text-center text-gray-500 mt-3">
                🔒 Secured by Razorpay (Test Mode)
              </p>
            </div>
          </form>
        </div>
      </div>
    }
  `
})
export class UpiPaymentModalComponent {
  isOpen = input.required<boolean>();
  amount = input<number | null>(null);
  externalError = input<string | null>(null);
  
  paymentSubmitted = output<UpiDetails>();
  modalClosed = output<void>();

  readonly Smartphone = Smartphone;
  readonly X = X;

  upiDetails = signal<UpiDetails>({ upiId: '' });
  error = signal<string | null>(null);
  isProcessing = signal(false);

  upiApps: UpiApp[] = [
    { name: 'PhonePe', icon: '🟣', suffix: '@ybl' },
    { name: 'Google Pay', icon: '🔵', suffix: '@okaxis' },
    { name: 'Paytm', icon: '🔵', suffix: '@paytm' }
  ];

  selectUpiApp(app: UpiApp): void {
    const currentId = this.upiDetails().upiId;
    const username = currentId.split('@')[0] || 'yourname';
    this.upiDetails.set({ upiId: `${username}${app.suffix}` });
  }

  validateForm(): boolean {
    const upiId = this.upiDetails().upiId.trim();
    
    if (!upiId) {
      this.error.set('UPI ID is required');
      return false;
    }
    
    if (upiId.length < 3) {
      this.error.set('UPI ID must be at least 3 characters');
      return false;
    }
    
    this.error.set(null);
    return true;
  }

  onSubmit(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isProcessing.set(true);
    this.paymentSubmitted.emit(this.upiDetails());
    
    setTimeout(() => {
      if (this.isProcessing()) {
        this.isProcessing.set(false);
      }
    }, 5000);
  }

  closeModal(): void {
    if (this.isProcessing()) {
      return;
    }
    
    this.upiDetails.set({ upiId: '' });
    this.error.set(null);
    this.isProcessing.set(false);
    this.modalClosed.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (!this.isProcessing()) {
      this.closeModal();
    }
  }
}
