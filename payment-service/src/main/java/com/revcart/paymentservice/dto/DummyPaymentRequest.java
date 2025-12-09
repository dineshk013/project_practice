package com.revcart.paymentservice.dto;

import lombok.Data;

@Data
public class DummyPaymentRequest {
    private Long orderId;
    private Long userId;
    private Double amount;
    private String paymentMethod;
    private String upiId;
}
