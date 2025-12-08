package com.revcart.deliveryservice.dto;

import com.revcart.deliveryservice.entity.Delivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDto {
    private String id;
    private Long orderId;
    private Long userId;
    private Long agentId;
    private String status;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DeliveryDto fromEntity(Delivery delivery) {
        return DeliveryDto.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .userId(delivery.getUserId())
                .agentId(delivery.getAgentId())
                .status(delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .estimatedDeliveryDate(delivery.getEstimatedDeliveryDate())
                .actualDeliveryDate(delivery.getActualDeliveryDate())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
