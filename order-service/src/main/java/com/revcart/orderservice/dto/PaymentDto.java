package com.revcart.orderservice.dto;

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
    private String status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;
}
