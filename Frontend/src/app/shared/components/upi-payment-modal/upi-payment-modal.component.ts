import { Component, signal, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, X, Smartphone } from 'lucide-angular';

export interface UpiDetails {
  upiId: string;
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
          <div class="flex items-center justify-between p-6 border-b">
            <div class="flex items-center gap-2">
              <lucide-icon [img]="Smartphone" class="h-5 w-5 text-primary"></lucide-icon>
              <h2 class="text-xl font-bold">Enter UPI Details</h2>
            </div>
            <button (click)="closeModal()" class="text-gray-400 hover:text-gray-600 transition-colors">
              <lucide-icon [img]="X" class="h-5 w-5"></lucide-icon>
            </button>
          </div>

          <form (ngSubmit)="onSubmit()" class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">UPI ID *</label>
              <input
                type="text"
                [(ngModel)]="upiDetails().upiId"
                name="upiId"
                required
                placeholder="yourname@paytm or 9876543210@paytm"
                class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                [class.border-red-500]="error()"
              />
              @if (error()) {
                <p class="text-red-500 text-xs mt-1">{{ error() }}</p>
              }
              <p class="text-gray-500 text-xs mt-1">Note: Your UPI ID remains private</p>
            </div>

            @if (externalError()) {
              <div class="p-3 bg-red-50 border border-red-200 text-red-700 rounded-md text-sm">
                {{ externalError() }}
              </div>
            }

            <div class="flex gap-3 pt-4">
              <button type="button" (click)="closeModal()" class="flex-1 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
                Cancel
              </button>
              <button type="submit" [disabled]="isProcessing()" class="flex-1 px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/90 disabled:opacity-50 transition-colors">
                {{ isProcessing() ? 'Processing...' : 'Pay Now' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `
})
export class UpiPaymentModalComponent {
  isOpen = input.required<boolean>();
  externalError = input<string | null>(null);
  
  paymentSubmitted = output<UpiDetails>();
  modalClosed = output<void>();

  readonly Smartphone = Smartphone;
  readonly X = X;

  upiDetails = signal<UpiDetails>({ upiId: '' });
  error = signal<string | null>(null);
  isProcessing = signal(false);

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
