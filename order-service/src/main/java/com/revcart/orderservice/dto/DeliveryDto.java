package com.revcart.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private Long agentId;
    private String status;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private LocalDateTime createdAt;
}
