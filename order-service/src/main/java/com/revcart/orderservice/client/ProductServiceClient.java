package com.revcart.orderservice.client;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.StockReservationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "product-service", url = "${services.product-service.url}")
public interface ProductServiceClient {
    
    @PutMapping("/api/products/stock/reserve")
    ApiResponse<Void> reserveStock(@RequestBody StockReservationRequest request);
    
    @PutMapping("/api/products/stock/release")
    ApiResponse<Void> releaseStock(@RequestBody StockReservationRequest request);
    
    @GetMapping("/api/products")
    ApiResponse<Object> getAllProducts();
}
