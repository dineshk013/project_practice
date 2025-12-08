package com.revcart.paymentservice.dto;

import com.revcart.paymentservice.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private Double amount;
    private String paymentMethod;
    private Payment.PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;
}
