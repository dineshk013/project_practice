package com.revcart.paymentservice.service;

import com.revcart.paymentservice.client.NotificationServiceClient;
import com.revcart.paymentservice.client.OrderServiceClient;
import com.revcart.paymentservice.client.UserServiceClient;
import com.revcart.paymentservice.dto.*;
import com.revcart.paymentservice.entity.Payment;
import com.revcart.paymentservice.exception.BadRequestException;
import com.revcart.paymentservice.exception.ResourceNotFoundException;
import com.revcart.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Transactional
    public PaymentDto initiatePayment(PaymentInitiateRequest request) {
        // Check if payment already exists for this order
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new BadRequestException("Payment already initiated for this order");
        }

        // Verify order exists
        try {
            orderServiceClient.getOrderById(request.getOrderId());
        } catch (Exception e) {
            throw new BadRequestException("Order not found: " + request.getOrderId());
        }

        // Verify user exists
        try {
            userServiceClient.getUserById(request.getUserId());
            log.debug("User validated for payment: {}", request.getUserId());
        } catch (Exception e) {
            log.warn("Failed to validate user for payment: {}", request.getUserId());
            // Continue with payment initiation even if user validation fails
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString());

        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated: {} for order: {}", saved.getId(), request.getOrderId());
        return toDto(saved);
    }

    @Transactional
    public PaymentDto verifyPayment(PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + request.getOrderId()));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new BadRequestException("Payment already processed");
        }

        // Update payment status
        if (Boolean.TRUE.equals(request.getSuccess())) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setTransactionId(request.getTransactionId());
            log.info("Payment successful: {} for order: {}", payment.getId(), request.getOrderId());

            // Notify order service
            try {
                orderServiceClient.updatePaymentStatus(request.getOrderId(), "COMPLETED");
            } catch (Exception e) {
                log.error("Failed to update order payment status: {}", e.getMessage());
            }
            
            // Send payment success notification
            sendPaymentNotification(payment.getId(), payment.getUserId(), request.getOrderId(), "SUCCESS", null);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Payment verification failed");
            log.warn("Payment failed: {} for order: {}", payment.getId(), request.getOrderId());

            // Notify order service
            try {
                orderServiceClient.updatePaymentStatus(request.getOrderId(), "FAILED");
            } catch (Exception e) {
                log.error("Failed to update order payment status: {}", e.getMessage());
            }
            
            // Send payment failed notification
            sendPaymentNotification(payment.getId(), payment.getUserId(), request.getOrderId(), "FAILED", "Payment verification failed");
        }

        Payment updated = paymentRepository.save(payment);
        return toDto(updated);
    }

    @Transactional
    public PaymentDto refundPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only successful payments can be refunded");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        Payment updated = paymentRepository.save(payment);
        log.info("Payment refunded: {} for order: {}", payment.getId(), orderId);

        // Notify order service
        try {
            orderServiceClient.updatePaymentStatus(orderId, "REFUNDED");
        } catch (Exception e) {
            log.error("Failed to update order payment status: {}", e.getMessage());
        }
        
        // Send payment refunded notification
        sendPaymentNotification(payment.getId(), payment.getUserId(), orderId, "REFUNDED", null);

        return toDto(updated);
    }

    public List<PaymentDto> getUserPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PaymentDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        return toDto(payment);
    }

    private PaymentDto toDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setUserId(payment.getUserId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setFailureReason(payment.getFailureReason());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }

    @Transactional(noRollbackFor = Exception.class)
    public DummyPaymentResponse processDummyPayment(DummyPaymentRequest request) {
        log.info("Processing dummy payment for orderId: {}, userId: {}, amount: {}", 
                request.getOrderId(), request.getUserId(), request.getAmount());

        try {
            // Check if payment already exists for this order
            Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                    .orElse(null);

            if (payment != null) {
                // Update existing payment
                log.info("Updating existing payment for order: {}", request.getOrderId());
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setTransactionId("TXN-" + UUID.randomUUID().toString());
                payment.setUpdatedAt(LocalDateTime.now());
                if (request.getUpiId() != null) {
                    payment.setUpiId(request.getUpiId());
                }
            } else {
                // Create new payment record
                log.info("Creating new payment for order: {}", request.getOrderId());
                payment = new Payment();
                payment.setOrderId(request.getOrderId());
                payment.setUserId(request.getUserId());
                payment.setAmount(request.getAmount());
                payment.setPaymentMethod(request.getPaymentMethod());
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setTransactionId("TXN-" + UUID.randomUUID().toString());
                if (request.getUpiId() != null) {
                    payment.setUpiId(request.getUpiId());
                }
            }

            Payment saved = paymentRepository.save(payment);
            log.info("Dummy payment successful: {} for order: {}", saved.getId(), request.getOrderId());

            // Notify order service about payment success - mark as PAYMENT_SUCCESS
            try {
                ApiResponse<Object> response = orderServiceClient.updatePaymentStatus(request.getOrderId(), "PAYMENT_SUCCESS");
                if (response.isSuccess()) {
                    log.info("✅ Order marked as PAYMENT_SUCCESS for order: {}", request.getOrderId());
                } else {
                    log.warn("⚠️ Order payment status update returned failure: {}", response.getMessage());
                }
            } catch (Exception e) {
                log.error("❌ Failed to update order payment status for order: {}, error: {}", request.getOrderId(), e.getMessage());
            }

            // Send payment success notification
            sendPaymentNotification(saved.getId(), request.getUserId(), request.getOrderId(), "SUCCESS", null);

            return new DummyPaymentResponse(
                    "SUCCESS",
                    saved.getTransactionId(),
                    "Payment processed successfully"
            );
        } catch (Exception ex) {
            log.error("Dummy payment failed", ex);
            return new DummyPaymentResponse(
                    "FAILED",
                    null,
                    "Payment processing failed: " + ex.getMessage()
            );
        }
    }

    private void sendPaymentNotification(Long paymentId, Long userId, Long orderId, String status, String reason) {
        try {
            notificationServiceClient.notifyPayment(paymentId, userId, orderId, status, reason);
            log.info("Notification sent for payment: {}, status: {}", paymentId, status);
        } catch (Exception e) {
            log.error("Failed to send notification for payment: {}, status: {}. Error: {}", 
                    paymentId, status, e.getMessage());
        }
    }
}
