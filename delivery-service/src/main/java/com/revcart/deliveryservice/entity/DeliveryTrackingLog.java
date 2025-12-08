package com.revcart.deliveryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("delivery_tracking_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTrackingLog {
    @Id
    private String id;
    private Delivery delivery;
    private Delivery.DeliveryStatus status;
    private String location;
    private String message;
    private LocalDateTime timestamp;
}
