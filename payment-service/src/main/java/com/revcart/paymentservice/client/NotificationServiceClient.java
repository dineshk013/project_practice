package com.revcart.paymentservice.client;

import com.revcart.paymentservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", url = "${services.notification-service.url}")
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications/payment/{paymentId}")
    ApiResponse<Void> notifyPayment(
            @PathVariable Long paymentId,
            @RequestParam Long userId,
            @RequestParam Long orderId,
            @RequestParam String status,
            @RequestParam(required = false) String reason
    );
}
