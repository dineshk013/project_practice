package com.revcart.paymentservice.controller;

import com.revcart.paymentservice.dto.*;
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
}
