package com.revcart.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDeliveryRequest {
    private Long orderId;
    private Long userId;
    private Long agentId;
    private LocalDateTime estimatedDeliveryDate;
}
