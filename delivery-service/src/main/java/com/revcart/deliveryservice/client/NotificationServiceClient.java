package com.revcart.deliveryservice.client;

import com.revcart.deliveryservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", url = "${services.notification-service.url}")
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications/order/{orderId}")
    ApiResponse<Void> notifyOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestParam String eventType
    );
}
