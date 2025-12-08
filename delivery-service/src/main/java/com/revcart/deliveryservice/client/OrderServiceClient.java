package com.revcart.deliveryservice.client;

import com.revcart.deliveryservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", url = "${services.order-service.url}")
public interface OrderServiceClient {
    
    @PutMapping("/api/orders/{id}/status")
    ApiResponse<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status
    );
}
