import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';
import { PaymentFormModalComponent, CardDetails } from '../../shared/components/payment-form-modal/payment-form-modal.component';
import { environment } from '../../../environments/environment';
import { LucideAngularModule, CreditCard, MapPin } from 'lucide-angular';

interface AddressDto {
    id?: number;
    line1: string;
    line2?: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    primaryAddress: boolean;
}

interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

@Component({
    selector: 'app-checkout',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule, PaymentFormModalComponent],
    templateUrl: './checkout.component.html',
    styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {
    cartService = inject(CartService);
    authService = inject(AuthService);
    router = inject(Router);
    http = inject(HttpClient);
    orderService = inject(OrderService);
    paymentService = inject(PaymentService);

    readonly CreditCard = CreditCard;
    readonly MapPin = MapPin;

    formData = signal({
        fullName: this.authService.user()?.name || '',
        phone: this.authService.user()?.phone || '',
        address: '',
        city: '',
        state: '',
        postalCode: '',
        country: 'India',
        paymentMethod: 'card'
    });

    isLoading = signal(false);
    errorMessage = signal('');
    addresses = signal<AddressDto[]>([]);
    selectedAddressId: number | 'new' = 'new';
    showPaymentModal = signal(false);
    currentOrderId = signal<number | null>(null);
    currentOrderAmount = signal<number | null>(null);
    paymentError = signal<string | null>(null);

    // Card details
    cardDetails = signal({
        cardNumber: '',
        expiryDate: '',
        cvv: '',
        cardholderName: ''
    });

    ngOnInit(): void {
        this.fetchSavedAddresses();
    }

    fetchSavedAddresses(): void {
        this.http.get<ApiResponse<AddressDto[]>>(`${environment.apiUrl}/profile/addresses`).subscribe({
            next: (res) => {
                if (res.success && Array.isArray(res.data)) {
                    this.addresses.set(res.data);
                    const primary = res.data.find((a) => a.primaryAddress && a.id);
                    const fallback = res.data.find((a) => !!a.id);
                    if (primary?.id) {
                        this.setSelectedAddress(primary.id);
                    } else if (fallback?.id) {
                        this.setSelectedAddress(fallback.id);
                    } else {
                        this.setSelectedAddress('new');
                    }
                } else {
                    this.setSelectedAddress('new');
                }
            },
            error: (err) => {
                console.error('Failed to fetch addresses', err);
                this.setSelectedAddress('new');
            }
        });
    }

    onAddressSelectionChange(addressId: number | 'new'): void {
        this.setSelectedAddress(addressId);
    }

    private setSelectedAddress(addressId: number | 'new'): void {
        this.selectedAddressId = addressId;
        const user = this.authService.user();
        const baseForm = {
            ...this.formData(),
            fullName: user?.name || '',
            phone: user?.phone || ''
        };

        if (addressId === 'new') {
            this.formData.set({
                ...baseForm,
                address: '',
                city: '',
                state: '',
                postalCode: '',
                country: 'India'
            });
            return;
        }

        const address = this.addresses().find((a) => a.id === addressId);
        if (address) {
            this.formData.set({
                ...baseForm,
                address: address.line1,
                city: address.city,
                state: address.state,
                postalCode: address.postalCode,
                country: address.country
            });
        }
    }

    get deliveryFee(): number {
        return this.cartService.total() > 0 ? 5.99 : 0;
    }

    get grandTotal(): number {
        return this.cartService.total() + this.deliveryFee;
    }

    onSubmit(): void {
        if (this.cartService.items().length === 0) {
            this.errorMessage.set('Your cart is empty');
            return;
        }

        // Validate only if adding new address
        if (this.selectedAddressId === 'new') {
            const data = this.formData();
            if (!data.address || !data.city || !data.postalCode || !data.state) {
                this.errorMessage.set('Please fill in all required address fields');
                return;
            }
        }

        this.isLoading.set(true);
        this.errorMessage.set('');

        this.syncCartToBackend().then(() => {
            if (this.selectedAddressId === 'new') {
                // Create new address then place order
                this.createAddressAndPlaceOrder();
            } else {
                // Use existing address
                this.placeOrder(this.selectedAddressId as number);
            }
        }).catch((err) => {
            this.isLoading.set(false);
            this.errorMessage.set('Failed to sync cart. Please try again.');
            console.error('Cart sync failed:', err);
        });
    }

    private createAddressAndPlaceOrder(): void {
        const data = this.formData();
        const addressData: AddressDto = {
            line1: data.address,
            city: data.city,
            state: data.state,
            postalCode: data.postalCode,
            country: data.country || 'India',
            primaryAddress: true
        };

        this.http.post<ApiResponse<AddressDto>>(`${environment.apiUrl}/profile/addresses`, addressData)
            .subscribe({
                next: (response) => {
                    if (response.success && response.data?.id) {
                        this.placeOrder(response.data.id);
                    } else {
                        this.isLoading.set(false);
                        this.errorMessage.set('Failed to save address. Please try again.');
                    }
                },
                error: (err) => {
                    this.isLoading.set(false);
                    this.errorMessage.set(err.error?.message || 'Failed to save address. Please try again.');
                    console.error('Address creation failed:', err);
                }
            });
    }

    private syncCartToBackend(): Promise<void> {
        return new Promise((resolve, reject) => {
            const items = this.cartService.items();
            if (items.length === 0) {
                resolve();
                return;
            }

            const validItems = items.filter(item => {
                const productId = parseInt(item.id, 10);
                return !isNaN(productId) && productId > 0;
            });

            if (validItems.length === 0) {
                this.errorMessage.set('No valid products found in cart.');
                reject(new Error('No valid products'));
                return;
            }

            this.http.delete(`${environment.apiUrl}/cart/clear`).subscribe({
                next: () => this.addItemsSequentially(validItems, 0, resolve, reject),
                error: () => this.addItemsSequentially(validItems, 0, resolve, reject)
            });
        });
    }

    private addItemsSequentially(items: any[], index: number, resolve: () => void, reject: (err: any) => void): void {
        if (index >= items.length) {
            resolve();
            return;
        }

        const item = items[index];
        const productId = parseInt(item.id, 10);
        const cartItemRequest = {
            productId: productId,
            quantity: item.quantity || 1
        };

        this.http.post(`${environment.apiUrl}/cart`, cartItemRequest).subscribe({
            next: () => this.addItemsSequentially(items, index + 1, resolve, reject),
            error: (err) => {
                if (err.status === 404 || err.status === 400 || err.status === 500) {
                    this.addItemsSequentially(items, index + 1, resolve, reject);
                } else {
                    this.addItemsSequentially(items, index + 1, resolve, reject);
                }
            }
        });
    }

    private placeOrder(addressId: number): void {
        const paymentMethodMap: { [key: string]: string } = {
            'card': 'RAZORPAY',
            'upi': 'UPI',
            'cod': 'COD'
        };

        const paymentMethod = paymentMethodMap[this.formData().paymentMethod] || 'COD';

        const checkoutRequest: any = {
            addressId: addressId,
            paymentMethod: paymentMethod
        };

        this.http.post<ApiResponse<any>>(`${environment.apiUrl}/orders/checkout`, checkoutRequest)
            .subscribe({
                next: (response) => {
                    const order = response.data || response;
                    const orderId = order.id || order.orderId;
                    const orderAmount = order.totalAmount || this.grandTotal;

                    if (this.formData().paymentMethod === 'card') {
                        this.isLoading.set(false);
                        this.currentOrderId.set(orderId);
                        this.currentOrderAmount.set(orderAmount);
                        this.showPaymentModal.set(true);
                    } else {
                        this.isLoading.set(false);
                        this.cartService.clearCart();
                        this.router.navigate(['/orders']);
                    }
                },
                error: (err) => {
                    this.isLoading.set(false);
                    console.error('Order creation failed:', err);
                    this.errorMessage.set(err.error?.message || 'Failed to place order. Please try again.');
                }
            });
    }

    onPaymentSubmitted(cardDetails: CardDetails): void {
        const orderId = this.currentOrderId();
        const amount = this.currentOrderAmount();

        if (!orderId || !amount) {
            this.paymentError.set('Order information is missing. Please try again.');
            return;
        }

        this.paymentError.set(null);

        this.paymentService.processDummyPayment(orderId, amount)
            .subscribe({
                next: (response) => {
                    if (response.success && response.data) {
                        this.cartService.clearCart();
                        this.showPaymentModal.set(false);
                        this.paymentError.set(null);
                        this.router.navigate(['/payment/success'], {
                            queryParams: {
                                paymentId: response.data.paymentId,
                                orderId: orderId,
                                amount: amount
                            }
                        });
                    } else {
                        this.paymentError.set(response.message || 'Payment failed');
                    }
                },
                error: (error) => {
                    console.error('Payment processing failed:', error);
                    this.paymentError.set(error.error?.message || 'Payment processing failed. Please try again.');
                }
            });
    }

    onPaymentModalClosed(): void {
        this.showPaymentModal.set(false);
        this.currentOrderId.set(null);
        this.currentOrderAmount.set(null);
        this.paymentError.set(null);
    }
}
