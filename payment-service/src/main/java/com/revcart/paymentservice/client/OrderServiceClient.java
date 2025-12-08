package com.revcart.paymentservice.client;

import com.revcart.paymentservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", url = "${services.order-service.url}")
public interface OrderServiceClient {
    
    @GetMapping("/api/orders/{id}")
    ApiResponse<Object> getOrderById(@PathVariable Long id);
    
    @PutMapping("/api/orders/{id}/payment-status")
    ApiResponse<Void> updatePaymentStatus(@PathVariable Long id, @RequestParam String status);
}
