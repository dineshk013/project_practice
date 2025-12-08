package com.revcart.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {
    private Long orderId;
    private Long userId;
    private Double amount;
    private String paymentMethod;
}
