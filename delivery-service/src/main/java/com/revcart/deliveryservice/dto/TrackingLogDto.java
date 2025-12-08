package com.revcart.deliveryservice.dto;

import com.revcart.deliveryservice.entity.DeliveryTrackingLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingLogDto {
    private String id;
    private String status;
    private String location;
    private String message;
    private LocalDateTime timestamp;

    public static TrackingLogDto fromEntity(DeliveryTrackingLog log) {
        return TrackingLogDto.builder()
                .id(log.getId())
                .status(log.getStatus() != null ? log.getStatus().name() : null)
                .location(log.getLocation())
                .message(log.getMessage())
                .timestamp(log.getTimestamp())
                .build();
    }
}
