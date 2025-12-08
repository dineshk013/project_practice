package com.revcart.analyticsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service", url = "${services.product-service.url}")
public interface ProductServiceClient {
    
    @GetMapping("/api/products/all")
    List<Map<String, Object>> getAllProducts();
}
