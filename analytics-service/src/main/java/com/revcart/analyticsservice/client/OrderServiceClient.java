package com.revcart.analyticsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "order-service", url = "${services.order-service.url}")
public interface OrderServiceClient {
    
    @GetMapping("/api/orders/all")
    List<Map<String, Object>> getAllOrders();
}
