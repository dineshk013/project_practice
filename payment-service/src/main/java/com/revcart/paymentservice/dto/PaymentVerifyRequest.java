package com.revcart.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private String transactionId;

    private Boolean success;
}
