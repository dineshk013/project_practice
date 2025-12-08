# 🔧 COMPLETE FIXED CODE - All Files

## Frontend Files

### 1. payment.service.ts (COMPLETE)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface DummyPaymentRequest {
  orderId: number;
  userId: number;
  amount: number;
  paymentMethod: string;
}

interface PaymentResponse {
  status: string;
  paymentId?: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = `${environment.apiUrl}/payments`;

  constructor(private httpClient: HttpClient) { }

  processDummyPayment(orderId: number, userId: number, amount: number, paymentMethod: string): Observable<ApiResponse<PaymentResponse>> {
    const request: DummyPaymentRequest = {
      orderId,
      userId,
      amount,
      paymentMethod
    };

    console.log('Processing payment:', request);
    
    return this.httpClient.post<ApiResponse<PaymentResponse>>(
      `${this.apiUrl}/dummy`,
      request
    );
  }
}
```

---

### 2. checkout.component.ts (KEY CHANGES)

```typescript
// Add this property
isSubmitting = signal(false);

// Remove cardDetails signal - not needed anymore

// Update onSubmit method
onSubmit(): void {
    if (this.isSubmitting()) {
        return; // Prevent duplicate submissions
    }

    if (this.cartService.items().length === 0) {
        this.errorMessage.set('Your cart is empty');
        return;
    }

    // ... rest of validation code ...

    this.isLoading.set(true);
    this.isSubmitting.set(true);  // ADD THIS
    this.errorMessage.set('');

    if (this.selectedAddressId === 'new') {
        this.createAddressAndPlaceOrder();
    } else {
        this.placeOrder(this.selectedAddressId as number);
    }
}

// Update createAddressAndPlaceOrder error handlers
private createAddressAndPlaceOrder(): void {
    // ... existing code ...
    .subscribe({
        next: (response) => {
            if (response.success && response.data?.id) {
                this.placeOrder(response.data.id);
            } else {
                this.isLoading.set(false);
                this.isSubmitting.set(false);  // ADD THIS
                this.errorMessage.set('Failed to save address. Please try again.');
            }
        },
        error: (err) => {
            this.isLoading.set(false);
            this.isSubmitting.set(false);  // ADD THIS
            this.errorMessage.set(err.error?.message || 'Failed to save address. Please try again.');
            console.error('Address creation failed:', err);
        }
    });
}

// Update placeOrder method
private placeOrder(addressId: number): void {
    // ... existing code ...
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
                this.isSubmitting.set(false);  // ADD THIS
                this.cartService.clearCart();
                this.router.navigate(['/orders']);
            }
        },
        error: (err) => {
            this.isLoading.set(false);
            this.isSubmitting.set(false);  // ADD THIS
            console.error('Order creation failed:', err);
            this.errorMessage.set(err.error?.message || 'Failed to place order. Please try again.');
        }
    });
}

// REPLACE onPaymentSubmitted method completely
onPaymentSubmitted(cardDetails: CardDetails): void {
    const orderId = this.currentOrderId();
    const amount = this.currentOrderAmount();
    const user = this.authService.user();

    if (!orderId || !amount) {
        this.paymentError.set('Order information is missing. Please try again.');
        return;
    }

    if (!user || !user.id) {
        this.paymentError.set('User not authenticated. Please login again.');
        return;
    }

    this.paymentError.set(null);

    this.paymentService.processDummyPayment(orderId, user.id, amount, 'RAZORPAY')
        .subscribe({
            next: (response) => {
                if (response.success && response.data?.status === 'SUCCESS') {
                    // Clear cart and close modal
                    this.cartService.clearCart();
                    this.showPaymentModal.set(false);
                    this.paymentError.set(null);
                    this.currentOrderId.set(null);
                    this.currentOrderAmount.set(null);
                    this.isSubmitting.set(false);
                    
                    // Redirect to My Orders
                    this.router.navigate(['/orders']);
                } else {
                    this.paymentError.set(response.data?.message || response.message || 'Payment failed');
                }
            },
            error: (error) => {
                console.error('Payment processing failed:', error);
                this.paymentError.set(error.error?.message || 'Payment processing failed. Please try again.');
            }
        });
}

// Update onPaymentModalClosed
onPaymentModalClosed(): void {
    this.showPaymentModal.set(false);
    this.currentOrderId.set(null);
    this.currentOrderAmount.set(null);
    this.paymentError.set(null);
    this.isSubmitting.set(false);  // ADD THIS
}
```

---

### 3. checkout.component.html (REMOVE CARD FORM)

Find and DELETE this entire section:
```html
<!-- Card Details Form (shown only when card is selected) -->
<div *ngIf="formData().paymentMethod === 'card'" class="mt-4 space-y-4 p-4 bg-gray-50 rounded-md">
    <div>
        <label class="block text-sm font-medium mb-2">Card Number *</label>
        <input type="text" [(ngModel)]="cardDetails().cardNumber" name="cardNumber" 
            placeholder="1234 5678 9012 3456" maxlength="19" required
            class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary" />
    </div>
    <div class="grid grid-cols-2 gap-4">
        <div>
            <label class="block text-sm font-medium mb-2">Expiry Date *</label>
            <input type="text" [(ngModel)]="cardDetails().expiryDate" name="expiryDate" 
                placeholder="MM/YY" maxlength="5" required
                class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary" />
        </div>
        <div>
            <label class="block text-sm font-medium mb-2">CVV *</label>
            <input type="text" [(ngModel)]="cardDetails().cvv" name="cvv" 
                placeholder="123" maxlength="3" required
                class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary" />
        </div>
    </div>
    <div>
        <label class="block text-sm font-medium mb-2">Cardholder Name *</label>
        <input type="text" [(ngModel)]="cardDetails().cardholderName" name="cardholderName" 
            placeholder="John Doe" required
            class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary" />
    </div>
</div>
```

Keep only the payment method radio buttons.

---

## Backend Files

### 4. PaymentController.java (COMPLETE)

```java
package com.revcart.paymentservice.controller;

import com.revcart.paymentservice.dto.ApiResponse;
import com.revcart.paymentservice.dto.PaymentDto;
import com.revcart.paymentservice.dto.PaymentInitiateRequest;
import com.revcart.paymentservice.dto.PaymentVerifyRequest;
import com.revcart.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentDto>> initiatePayment(@Valid @RequestBody PaymentInitiateRequest request) {
        PaymentDto payment = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment, "Payment initiated successfully"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentDto>> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        PaymentDto payment = paymentService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment verified successfully"));
    }

    @PostMapping("/refund/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDto>> refundPayment(@PathVariable Long orderId) {
        PaymentDto payment = paymentService.refundPayment(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment refunded successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getUserPayments(@PathVariable Long userId) {
        List<PaymentDto> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(ApiResponse.success(payments, "Payment history retrieved successfully"));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }

    @PostMapping("/dummy")
    public ResponseEntity<ApiResponse<DummyPaymentResponse>> processDummyPayment(@Valid @RequestBody DummyPaymentRequest request) {
        DummyPaymentResponse response = paymentService.processDummyPayment(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment processed successfully"));
    }

    public static class DummyPaymentRequest {
        private Long orderId;
        private Long userId;
        private Double amount;
        private String paymentMethod;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class DummyPaymentResponse {
        private String status;
        private String paymentId;
        private String message;

        public DummyPaymentResponse(String status, String paymentId, String message) {
            this.status = status;
            this.paymentId = paymentId;
            this.message = message;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
```

---

### 5. PaymentService.java (ADD THIS METHOD)

Add this method at the end of PaymentService class (before the last closing brace):

```java
@Transactional
public PaymentController.DummyPaymentResponse processDummyPayment(PaymentController.DummyPaymentRequest request) {
    log.info("Processing dummy payment for orderId: {}, userId: {}, amount: {}", 
            request.getOrderId(), request.getUserId(), request.getAmount());

    try {
        // Create payment record without checking order existence
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString());

        Payment saved = paymentRepository.save(payment);
        log.info("Dummy payment successful: {} for order: {}", saved.getId(), request.getOrderId());

        // Notify order service about payment success
        try {
            orderServiceClient.updatePaymentStatus(request.getOrderId(), "COMPLETED");
        } catch (Exception e) {
            log.warn("Failed to update order payment status: {}", e.getMessage());
        }

        // Send notification
        sendPaymentNotification(saved.getId(), request.getUserId(), request.getOrderId(), "SUCCESS", null);

        return new PaymentController.DummyPaymentResponse(
                "SUCCESS",
                saved.getTransactionId(),
                "Payment processed successfully"
        );
    } catch (Exception e) {
        log.error("Dummy payment failed for order: {}, error: {}", request.getOrderId(), e.getMessage());
        return new PaymentController.DummyPaymentResponse(
                "FAILED",
                null,
                "Payment processing failed: " + e.getMessage()
        );
    }
}
```

---

### 6. OrderController.java (ADD THIS ENDPOINT)

Add this method at the end of OrderController class (before the last closing brace):

```java
@PostMapping("/{orderId}/payment-status")
public ResponseEntity<ApiResponse<Void>> updatePaymentStatus(
        @PathVariable Long orderId,
        @RequestParam String status) {
    orderService.updatePaymentStatus(orderId, status);
    return ResponseEntity.ok(ApiResponse.success(null, "Payment status updated"));
}
```

---

### 7. OrderService.java (REPLACE performPostOrderOperations AND ADD NEW METHODS)

Replace the `performPostOrderOperations` method with:

```java
private void performPostOrderOperations(Order order, Long userId, String paymentMethod) {
    // Initiate payment (non-blocking)
    if (!"COD".equalsIgnoreCase(paymentMethod)) {
        try {
            PaymentInitiateRequest paymentRequest = new PaymentInitiateRequest(
                    order.getId(),
                    userId,
                    order.getTotalAmount(),
                    paymentMethod
            );
            ApiResponse<PaymentDto> paymentResponse = paymentServiceClient.initiatePayment(paymentRequest);
            log.info("Payment initiated for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Payment initiation failed for order: {}, error: {}", order.getId(), e.getMessage());
        }
    } else {
        // For COD, assign delivery and send notification immediately
        assignDeliveryAndNotify(order.getId(), userId);
        
        // Clear cart for COD orders
        try {
            cartServiceClient.clearCart(userId);
            log.info("✅ Cart cleared for userId: {} after COD order", userId);
        } catch (Exception e) {
            log.warn("⚠️ Failed to clear cart for userId: {}, error: {}", userId, e.getMessage());
        }
    }
}
```

Add these NEW methods to OrderService:

```java
@Transactional
public void updatePaymentStatus(Long orderId, String paymentStatus) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if ("COMPLETED".equalsIgnoreCase(paymentStatus)) {
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderRepository.save(order);
        log.info("Payment status updated to PAID for order: {}", orderId);

        // Assign delivery and send notification ONLY after payment success
        assignDeliveryAndNotify(orderId, order.getUserId());
        
        // Clear cart after successful payment
        try {
            cartServiceClient.clearCart(order.getUserId());
            log.info("✅ Cart cleared for userId: {} after successful payment", order.getUserId());
        } catch (Exception e) {
            log.warn("⚠️ Failed to clear cart for userId: {}, error: {}", order.getUserId(), e.getMessage());
        }
    } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {
        order.setPaymentStatus(Order.PaymentStatus.FAILED);
        orderRepository.save(order);
        log.warn("Payment failed for order: {}", orderId);
        // Do NOT assign delivery or send notifications for failed payments
    } else if ("REFUNDED".equalsIgnoreCase(paymentStatus)) {
        order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        orderRepository.save(order);
        log.info("Payment refunded for order: {}", orderId);
    }
}

private void assignDeliveryAndNotify(Long orderId, Long userId) {
    // Assign delivery
    try {
        AssignDeliveryRequest deliveryRequest = new AssignDeliveryRequest(
                orderId,
                userId,
                null,
                java.time.LocalDateTime.now().plusDays(3)
        );
        deliveryServiceClient.assignDelivery(deliveryRequest);
        log.info("Delivery assigned for order: {}", orderId);
    } catch (Exception e) {
        log.error("Delivery assignment failed for order: {}, error: {}", orderId, e.getMessage());
    }

    // Send notification
    try {
        sendOrderNotification(orderId, userId, "PLACED");
    } catch (Exception e) {
        log.error("Notification failed for order: {}, error: {}", orderId, e.getMessage());
    }
}
```

---

## Summary of Changes

### Frontend:
1. ✅ Removed card form from checkout page
2. ✅ Added duplicate submission prevention
3. ✅ Updated payment service to send userId and paymentMethod
4. ✅ Modal validation already in place

### Backend:
1. ✅ Added `/api/payments/dummy` endpoint
2. ✅ Payment service does NOT check order DB
3. ✅ Added `/api/orders/{orderId}/payment-status` endpoint
4. ✅ Delivery assigned ONLY after payment success
5. ✅ Notifications sent ONLY after payment success
6. ✅ Cart cleared ONLY after payment success

**All fixes complete! Ready to test! 🚀**
